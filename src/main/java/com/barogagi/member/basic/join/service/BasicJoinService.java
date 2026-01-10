package com.barogagi.member.basic.join.service;

import com.barogagi.config.PasswordConfig;
import com.barogagi.member.basic.join.dto.JoinDTO;
import com.barogagi.member.basic.join.dto.JoinRequestDTO;
import com.barogagi.member.basic.join.dto.NickNameDTO;
import com.barogagi.member.basic.join.exception.JoinException;
import com.barogagi.member.login.dto.RefreshTokenRequestDTO;
import com.barogagi.member.login.service.AccountService;
import com.barogagi.member.login.service.AuthService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BasicJoinService {

    private final Validator validator;
    private final InputValidate inputValidate;
    private final JoinService joinService;
    private final AuthService authService;
    private final AccountService accountService;
    private final EncryptUtil encryptUtil;
    private final PasswordConfig passwordConfig;

    @Autowired
    public BasicJoinService( Validator validator
                            , InputValidate inputValidate
                            , JoinService joinService
                            , AuthService authService
                            , AccountService accountService
                            , EncryptUtil encryptUtil
                            , PasswordConfig passwordConfig) {

        this.validator = validator;
        this.inputValidate = inputValidate;
        this.joinService = joinService;
        this.authService = authService;
        this.accountService = accountService;
        this.encryptUtil = encryptUtil;
        this.passwordConfig = passwordConfig;
    }

    // 닉네임 중복 체크 service
    public ApiResponse checkNickname(String apiSecretKey, String nickname) {

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
        NickNameDTO nickNameDTO = new NickNameDTO();
        nickNameDTO.setNickName(nickname);

        int nickNameCnt = joinService.selectNicknameCnt(nickNameDTO);
        if(nickNameCnt > 0) {
            throw new JoinException(ErrorCode.UNAVAILABLE_NICKNAME);
        }

        return ApiResponse.result(ErrorCode.AVAILABLE_NICKNAME);
    }

    // 아이디 중복 체크 service
    public ApiResponse checkUserId(String apiSecretKey, String userId) {

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
        JoinDTO joinDTO = new JoinDTO();
        joinDTO.setUserId(userId);

        int checkUserId = joinService.selectUserIdCnt(joinDTO);

        if(checkUserId > 0) {
            throw new JoinException(ErrorCode.UNAVAILABLE_USER_ID);
        }

        return ApiResponse.result(ErrorCode.AVAILABLE_USER_ID);
    }

    public ApiResponse signUp(JoinRequestDTO joinRequestDTO) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(joinRequestDTO.getApiSecretKey())) {
            throw new JoinException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        // 필수 입력값(아이디, 비밀번호, 휴대전화번호 값이 빈 값이 아닌지 확인)
        // 선택 입력값(이메일, 생년월일, 성별, 닉네임)
        if(inputValidate.isEmpty(joinRequestDTO.getUserId())
                || inputValidate.isEmpty(joinRequestDTO.getPassword())
                || inputValidate.isEmpty(joinRequestDTO.getTel()))
        {
            throw new JoinException(ErrorCode.EMPTY_DATA);
        }

        // 3. 적합한 아이디인지 확인
        // 아이디, 비밀번호 적합성 검사
        if(!(
                validator.isValidId(joinRequestDTO.getUserId())
                        && validator.isValidPassword(joinRequestDTO.getPassword()))
        ) {
            throw new JoinException(ErrorCode.INVALID_SIGN_UP);
        }

        // 4. 암호화
        // 휴대전화번호, 비밀번호 암호화
        joinRequestDTO.setTel(encryptUtil.encrypt(joinRequestDTO.getTel().replaceAll("[^0-9]", "")));
        String encodedPassword = passwordConfig.passwordEncoder().encode(joinRequestDTO.getPassword());
        joinRequestDTO.setPassword(encodedPassword);

        // 이메일 값이 넘어오면 암호화
        if(!inputValidate.isEmpty(joinRequestDTO.getEmail())){
            joinRequestDTO.setEmail(encryptUtil.encrypt(joinRequestDTO.getEmail()));
        }

        JoinDTO joinDTO = new JoinDTO();
        joinDTO.setUserId(joinRequestDTO.getUserId());
        joinDTO.setPassword(joinRequestDTO.getPassword());
        joinDTO.setEmail(joinRequestDTO.getEmail());

        if(null != joinRequestDTO.getBirth()) {
            joinRequestDTO.setBirth(joinRequestDTO.getBirth().replaceAll("[^0-9]", ""));
        }
        joinDTO.setBirth(joinRequestDTO.getBirth());
        joinDTO.setTel(joinRequestDTO.getTel());
        joinDTO.setGender(joinRequestDTO.getGender());
        joinDTO.setNickName(joinRequestDTO.getNickName());
        joinDTO.setJoinType("BASIC");

        // 아이디 중복 검증
        int duplicateUserId = joinService.selectUserIdCnt(joinDTO);

        // 5. 아이디 중복 검증
        if(duplicateUserId > 0) {
            throw new JoinException(ErrorCode.UNAVAILABLE_USER_ID);
        }

        // 닉네임 값이 넘어올 경우 중복 검사
        if(!inputValidate.isEmpty(joinDTO.getNickName())) {

            // 닉네임 적합성 검사
            if(!validator.isValidNickname(joinRequestDTO.getNickName())) {
                throw new JoinException(ErrorCode.INVALID_NICKNAME);
            }

            NickNameDTO nickNameDTO = new NickNameDTO();
            nickNameDTO.setNickName(joinDTO.getNickName());
            int selectNicknameCnt = joinService.selectNicknameCnt(nickNameDTO);

            if(selectNicknameCnt > 0) {
                throw new JoinException(ErrorCode.UNAVAILABLE_NICKNAME);
            }
        }

        // 6. 회원 정보 저장
        int insertResult = joinService.insertMembershipInfo(joinDTO);
        if(insertResult <= 0){
            throw new JoinException(ErrorCode.FAIL_SIGN_UP);
        }

        return ApiResponse.result(ErrorCode.SUCCESS_SIGN_UP);
    }

    public ApiResponse deleteAccount(RefreshTokenRequestDTO refreshTokenRequestDTO) {

        // 1. refresh token이 공백 또는 null인지 확인
        if(inputValidate.isEmpty(refreshTokenRequestDTO.getRefreshToken())) {
            throw new JoinException(ErrorCode.EMPTY_DATA);
        }

        // 2. refresh token을 이용해서 membershipNo 구하기
        Map<String, String> resultMap = authService.selectUserInfoByToken(refreshTokenRequestDTO.getRefreshToken());
        if(!resultMap.get("resultCode").equals("200")) {

            return ApiResponse.error(
                    resultMap.get("resultCode"),
                    resultMap.get("message")
            );
        }

        int deleteResult = accountService.deleteMyAccount(resultMap.get("membershipNo"));
        if(deleteResult <= 0) {
            throw new JoinException(ErrorCode.FAIL_DELETE_ACCOUNT);
        }

        return ApiResponse.result(ErrorCode.SUCCESS_DELETE_ACCOUNT);
    }
}


