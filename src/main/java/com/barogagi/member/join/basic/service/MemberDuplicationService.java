package com.barogagi.member.join.basic.service;

import com.barogagi.member.join.basic.exception.JoinException;
import com.barogagi.member.repository.DeletedMembershipRepository;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberDuplicationService {

    private final Validator validator;
    private final InputValidate inputValidate;
    private final EncryptUtil encryptUtil;
    private final UserMembershipRepository userMembershipRepository;
    private final DeletedMembershipRepository deletedMembershipRepository;

    private static final int REJOIN_BLOCK_DAYS = 90;

    // 아이디 중복 체크
    @Transactional(readOnly = true)
    public ApiResponse existsByUserId(String apiSecretKey, String userId) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new JoinException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(userId)) {
            throw new JoinException(ErrorCode.EMPTY_DATA);
        }

        // 3. 적합한 아이디인지 확인
        if(!validator.isValidId(userId)) {
            throw new JoinException(ErrorCode.INVALID_USER_ID);
        }

        // 4. 아이디 중복 체크
        boolean existsByUserId = userMembershipRepository.existsByUserId(userId.trim());
        if(existsByUserId) {
            throw new JoinException(ErrorCode.UNAVAILABLE_USER_ID);
        }

        // 5. 일정 기간 동안 동일한 아이디로 회원가입 금지
        LocalDateTime limitDate = LocalDateTime.now().minusDays(REJOIN_BLOCK_DAYS);
        boolean blocked = deletedMembershipRepository.existsRecentlyWithdrawnUser(userId.trim(), limitDate);
        if(blocked) {
            throw new JoinException(ErrorCode.UNAVAILABLE_USER_ID);
        }

        return ApiResponse.result(ErrorCode.AVAILABLE_USER_ID);
    }

    // 닉네임 중복 체크
    @Transactional(readOnly = true)
    public ApiResponse existsByNickname(String apiSecretKey, String nickname) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new JoinException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(nickname)) {
            throw new JoinException(ErrorCode.EMPTY_DATA);
        }

        // 3. 적합한 닉네임인지 확인
        if(!validator.isValidNickname(nickname)) {
            throw new JoinException(ErrorCode.INVALID_NICKNAME);
        }

        // 4. 닉네임 중복 체크
        boolean existsNickname = userMembershipRepository.existsByNickName(nickname);
        if(existsNickname) {
            throw new JoinException(ErrorCode.UNAVAILABLE_NICKNAME);
        }

        return ApiResponse.result(ErrorCode.AVAILABLE_NICKNAME);
    }

    @Transactional(readOnly = true)
    public ApiResponse existsByTel(String apiSecretKey, String tel) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new JoinException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(tel)) {
            throw new JoinException(ErrorCode.EMPTY_DATA);
        }

        // 3. 암호화
        String encTel = encryptUtil.encrypt(tel.replaceAll("[^0-9]", ""));


        // 3. 전화번호 중복 여부 확인
        boolean existsTel = userMembershipRepository.existsByTel(encTel);

        if(existsTel) {
            throw new JoinException(ErrorCode.FAIL_DUPLICATE_PHONE_NUMBER);
        }

        return ApiResponse.result(ErrorCode.SUCCESS_DUPLICATE_PHONE_NUMBER);
    }
}
