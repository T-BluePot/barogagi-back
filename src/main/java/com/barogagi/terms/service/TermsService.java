package com.barogagi.terms.service;

import com.barogagi.config.resultCode.ProcessResultCode;
import com.barogagi.member.login.dto.LoginVO;
import com.barogagi.member.login.service.LoginService;
import com.barogagi.response.ApiResponse;
import com.barogagi.terms.dto.*;
import com.barogagi.terms.exception.TermsException;
import com.barogagi.terms.mapper.TermsMapper;
import com.barogagi.util.InputValidate;
import com.barogagi.config.resultCode.ResultCode;
import com.barogagi.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.List;

@Service
public class TermsService {

    private final TermsMapper termsMapper;
    private final Validator validator;
    private final InputValidate inputValidate;
    private final LoginService loginService;

    @Autowired
    public TermsService(
                        TermsMapper termsMapper,
                        Validator validator,
                        InputValidate inputValidate,
                        LoginService loginService
                        )
    {
        this.termsMapper = termsMapper;
        this.validator = validator;
        this.inputValidate = inputValidate;
        this.loginService = loginService;
    }

    public ApiResponse termsListProcess(String apiSecretKey, String termsType) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new TermsException(
                    ResultCode.NOT_EQUAL_API_SECRET_KEY.getResultCode(),
                    ResultCode.NOT_EQUAL_API_SECRET_KEY.getMessage()
            );
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(termsType)) {
            throw new TermsException(
                    ProcessResultCode.EMPTY_DATA.getResultCode(),
                    ProcessResultCode.EMPTY_DATA.getMessage()
            );
        }

        // 3. 약관 조회
        TermsInputDTO termsInputDTO = new TermsInputDTO();
        termsInputDTO.setTermsType(termsType);
        List<TermsOutputDTO> termsList = this.selectTermsList(termsInputDTO);

        if(termsList.isEmpty()) {
            throw new TermsException(
                    ProcessResultCode.NOT_FOUND_TERMS.getResultCode(),
                    ProcessResultCode.NOT_FOUND_TERMS.getMessage()
            );

        }

        return ApiResponse.resultData(
                termsList,
                ProcessResultCode.FOUND_TERMS.getResultCode(),
                ProcessResultCode.FOUND_TERMS.getMessage()
        );
    }

    public ApiResponse termsAgreementsProcess(TermsDTO termsDTO) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(termsDTO.getApiSecretKey())) {
            throw new TermsException(
                    ResultCode.NOT_EQUAL_API_SECRET_KEY.getResultCode(),
                    ResultCode.NOT_EQUAL_API_SECRET_KEY.getMessage()
            );
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(termsDTO.getUserId()) ||
                termsDTO.getTermsAgreeList() == null ||
                termsDTO.getTermsAgreeList().isEmpty()) {
            throw new TermsException(
                    ProcessResultCode.EMPTY_DATA.getResultCode(),
                    ProcessResultCode.EMPTY_DATA.getMessage()
            );
        }

        LoginVO lvo = new LoginVO();
        lvo.setUserId(termsDTO.getUserId());
        LoginVO loginVO = loginService.findMembershipNo(lvo);
        if(null == loginVO) {
            throw new TermsException(
                    ProcessResultCode.NOT_FOUND_USER_INFO.getResultCode(),
                    ProcessResultCode.NOT_FOUND_USER_INFO.getMessage()
            );
        }

        List<TermsAgreeDTO> termsAgreeDTOList = new ArrayList<>();
        List<TermsProcessDTO> termsAgreeList = termsDTO.getTermsAgreeList();

        for(TermsProcessDTO termsProcessDTO : termsAgreeList) {
            TermsAgreeDTO termsAgreeDTO = new TermsAgreeDTO();
            termsAgreeDTO.setMembershipNo(loginVO.getMembershipNo());
            termsAgreeDTO.setTermsNum(termsProcessDTO.getTermsNum());
            termsAgreeDTO.setAgreeYn(termsProcessDTO.getAgreeYn());
            termsAgreeDTOList.add(termsAgreeDTO);
        }
        String resCode = this.insertTermsAgreeList(termsAgreeDTOList);

        if(!resCode.equals("200")) {
            throw new TermsException(
                    ProcessResultCode.FAIL_INSERT_TERMS.getResultCode(),
                    ProcessResultCode.FAIL_INSERT_TERMS.getMessage()
            );
        }

        return ApiResponse.result(
                ProcessResultCode.SUCCESS_INSERT_TERMS.getResultCode(),
                ProcessResultCode.SUCCESS_INSERT_TERMS.getMessage()
        );
    }

    // 사용중인 약관 목록 조회
    public List<TermsOutputDTO> selectTermsList(TermsInputDTO termsInputDTO) {
        return termsMapper.selectTermsList(termsInputDTO);
    }

    // 약관 동의 여부 저장
    public int insertTermsAgreeInfo(TermsAgreeDTO vo) {
        return termsMapper.insertTermsAgreeInfo(vo);
    }

    @Transactional
    public String insertTermsAgreeList(List<TermsAgreeDTO> termsList) {
        String resultCode = "";
        try {
            for(TermsAgreeDTO vo : termsList) {
                int insertFlag = this.insertTermsAgreeInfo(vo);
                if(insertFlag > 0){
                    resultCode = "200";
                } else {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                }
            }
        } catch (Exception e) {
            resultCode = "400";
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new RuntimeException(e);
        }

        return resultCode;
    }
}
