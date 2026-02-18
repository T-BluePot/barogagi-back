package com.barogagi.terms.service;

import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.login.service.LoginService;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.response.ApiResponse;
import com.barogagi.terms.domain.TermsAgree;
import com.barogagi.terms.domain.TermsId;
import com.barogagi.terms.dto.*;
import com.barogagi.terms.domain.Terms;
import com.barogagi.terms.exception.TermsException;
import com.barogagi.terms.repository.TermsAgreeRepository;
import com.barogagi.terms.repository.TermsRepository;
import com.barogagi.terms.repository.spec.TermsSpec;
import com.barogagi.util.InputValidate;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TermsService {
    private final TermsRepository termsRepository;
    private final TermsAgreeRepository termsAgreeRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final Validator validator;
    private final InputValidate inputValidate;
    private final LoginService loginService;

    public ApiResponse termsListProcess(String apiSecretKey, String termsType) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new TermsException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(termsType)) {
            throw new TermsException(ErrorCode.EMPTY_DATA);
        }

        // 3. 약관 조회
        List<Terms> termsList = this.findActiveTermsByType(termsType);

        if(termsList.isEmpty()) {
            throw new TermsException(ErrorCode.NOT_FOUND_TERMS);

        }

        return ApiResponse.resultData(
                termsList,
                ErrorCode.FOUND_TERMS.getCode(),
                ErrorCode.FOUND_TERMS.getMessage()
        );
    }

    public ApiResponse termsAgreementsProcess(String apiSecretKey, TermsDTO termsDTO) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new TermsException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(termsDTO.getUserId()) ||
                termsDTO.getTermsAgreeList() == null ||
                termsDTO.getTermsAgreeList().isEmpty()) {
            throw new TermsException(ErrorCode.EMPTY_DATA);
        }

        UserMembershipInfo userMembershipInfo = userMembershipRepository.findByUserId(termsDTO.getUserId());
        if(null == userMembershipInfo) {
            throw new TermsException(ErrorCode.NOT_FOUND_USER_INFO);
        }

        List<TermsAgreeDTO> termsAgreeDTOList = new ArrayList<>();
        List<TermsProcessDTO> termsAgreeList = termsDTO.getTermsAgreeList();

        for(TermsProcessDTO termsProcessDTO : termsAgreeList) {
            TermsAgreeDTO termsAgreeDTO = new TermsAgreeDTO();
            termsAgreeDTO.setMembershipNo(userMembershipInfo.getMembershipNo());
            termsAgreeDTO.setTermsNum(termsProcessDTO.getTermsNum());
            termsAgreeDTO.setAgreeYn(termsProcessDTO.getAgreeYn());
            termsAgreeDTOList.add(termsAgreeDTO);
        }
        String resCode = this.insertTermsAgreeList(termsAgreeDTOList);

        if(!resCode.equals("200")) {
            throw new TermsException(ErrorCode.FAIL_INSERT_TERMS);
        }

        return ApiResponse.result(
                ErrorCode.SUCCESS_INSERT_TERMS.getCode(),
                ErrorCode.SUCCESS_INSERT_TERMS.getMessage()
        );
    }

    // 사용중인 약관 목록 조회
    @Transactional(readOnly = true)
    public List<Terms> findActiveTermsByType(String termsType) {
        Specification<Terms> spec = TermsSpec.useYnY()
                .and(TermsSpec.termsTypeEq(termsType));

        return termsRepository.findAll(
                spec,
                Sort.by(Sort.Direction.ASC, "sort")
        );
    }

    // 약관 동의 여부 저장
    @Transactional
    public boolean insertTermsAgreeInfo(TermsAgreeDTO vo) {

        try {
            TermsId termsId = new TermsId();
            termsId.setTermsNum(vo.getTermsNum());
            termsId.setMembershipNo(vo.getMembershipNo());

            TermsAgree termsAgree = new TermsAgree();
            termsAgree.setId(termsId);
            termsAgree.setAgreeYn(vo.getAgreeYn());

            termsAgreeRepository.save(termsAgree);
            return true;

        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    public String insertTermsAgreeList(List<TermsAgreeDTO> termsList) {
        for(TermsAgreeDTO vo : termsList) {
            boolean insertResult = this.insertTermsAgreeInfo(vo);
            if(!insertResult){
                return "400";
            }
        }
        return "200";
    }
}
