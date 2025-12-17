package com.barogagi.member.basic.join.service;

import com.barogagi.config.PasswordConfig;
import com.barogagi.member.MemberResultCode;
import com.barogagi.member.basic.join.dto.DeleteAccountRequestDTO;
import com.barogagi.member.basic.join.dto.JoinDTO;
import com.barogagi.member.basic.join.dto.JoinRequestDTO;
import com.barogagi.member.basic.join.dto.NickNameDTO;
import com.barogagi.member.basic.join.exception.JoinException;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.member.login.service.AccountService;
import com.barogagi.member.login.service.AuthService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.ResultCode;
import com.barogagi.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BasicJoinService {

    private static final Logger logger = LoggerFactory.getLogger(BasicJoinService.class);

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

        String resultCode = "";
        String message = "";

        try {
            // 1. API SECRET KEY 일치 여부 확인
            if(!validator.apiSecretKeyCheck(apiSecretKey)) {
                throw new JoinException(
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getResultCode(),
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getMessage()
                );
            }

            // 2. 필수 입력값 확인
            if(inputValidate.isEmpty(nickname)) {
                throw new JoinException(
                        MemberResultCode.EMPTY_NICKNAME.getResultCode(),
                        MemberResultCode.EMPTY_NICKNAME.getMessage()
                );
            }

            // 3. 적합한 닉네임인지 확인
            if(!validator.isValidNickname(nickname)) {
                throw new JoinException(
                        MemberResultCode.INVALID_NICKNAME.getResultCode(),
                        MemberResultCode.INVALID_NICKNAME.getMessage()
                );
            }

            // 4. 닉네임 중복 체크
            NickNameDTO nickNameDTO = new NickNameDTO();
            nickNameDTO.setNickName(nickname);

            int nickNameCnt = joinService.selectNicknameCnt(nickNameDTO);
            if(nickNameCnt > 0) {
                resultCode = MemberResultCode.UNAVAILABLE_NICKNAME.getResultCode();
                message = MemberResultCode.UNAVAILABLE_NICKNAME.getMessage();
            } else {
                resultCode = MemberResultCode.AVAILABLE_NICKNAME.getResultCode();
                message = MemberResultCode.AVAILABLE_NICKNAME.getMessage();
            }

        } catch (JoinException ex) {
            resultCode = ex.getResultCode();
            message = ex.getMessage();

        } catch (Exception e) {
            resultCode = ResultCode.ERROR.getResultCode();
            message = ResultCode.ERROR.getMessage();

        }

        return ApiResponse.result(resultCode, message);
    }

    // 아이디 중복 체크 service
    public ApiResponse checkUserId(String apiSecretKey, String userId) {

        String resultCode = "";
        String message = "";

        try {

            // 1. API SECRET KEY 일치 여부 확인
            if(!validator.apiSecretKeyCheck(apiSecretKey)) {
                throw new JoinException(
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getResultCode(),
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getMessage()
                );
            }

            // 2. 필수 입력값 확인
            if(inputValidate.isEmpty(userId)) {
                throw new JoinException(
                        MemberResultCode.EMPTY_USER_ID.getResultCode(),
                        MemberResultCode.EMPTY_USER_ID.getMessage()
                );
            }

            // 3. 적합한 아이디인지 확인
            if(!validator.isValidId(userId)) {
                throw new JoinException(
                        MemberResultCode.INVALID_USER_ID.getResultCode(),
                        MemberResultCode.INVALID_USER_ID.getMessage()
                );
            }

            // 4. 아이디 중복 체크
            JoinDTO joinDTO = new JoinDTO();
            joinDTO.setUserId(userId);

            int checkUserId = joinService.selectUserIdCnt(joinDTO);

            if(checkUserId > 0){
                resultCode = MemberResultCode.UNAVAILABLE_USER_ID.getResultCode();
                message = MemberResultCode.UNAVAILABLE_USER_ID.getMessage();

            } else{
                resultCode = MemberResultCode.AVAILABLE_USER_ID.getResultCode();
                message = MemberResultCode.AVAILABLE_USER_ID.getMessage();
            }

        } catch (JoinException ex) {
            resultCode = ex.getResultCode();
            message = ex.getMessage();

        } catch (Exception e) {
            resultCode = ResultCode.ERROR.getResultCode();
            message = ResultCode.ERROR.getMessage();
        }

        return ApiResponse.result(resultCode, message);
    }

    public ApiResponse signUp(JoinRequestDTO joinRequestDTO) {

        String resultCode = "";
        String message = "";

        try {

            // 1. API SECRET KEY 일치 여부 확인
            if(!validator.apiSecretKeyCheck(joinRequestDTO.getApiSecretKey())) {
                throw new JoinException(
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getResultCode(),
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getMessage()
                );
            }

            // 2. 필수 입력값 확인
            // 필수 입력값(아이디, 비밀번호, 휴대전화번호 값이 빈 값이 아닌지 확인)
            // 선택 입력값(이메일, 생년월일, 성별, 닉네임)
            if(inputValidate.isEmpty(joinRequestDTO.getUserId())
                    || inputValidate.isEmpty(joinRequestDTO.getPassword())
                    || inputValidate.isEmpty(joinRequestDTO.getTel()))
            {
                throw new JoinException(
                        MemberResultCode.EMPTY_SIGN_UP.getResultCode(),
                        MemberResultCode.EMPTY_SIGN_UP.getMessage()
                );
            }

            // 3. 적합한 아이디인지 확인
            // 아이디, 비밀번호 적합성 검사
            if(!(
                    validator.isValidId(joinRequestDTO.getUserId())
                            && validator.isValidPassword(joinRequestDTO.getPassword()))
            ) {
                throw new JoinException(
                        MemberResultCode.INVALID_SIGN_UP.getResultCode(),
                        MemberResultCode.INVALID_SIGN_UP.getMessage()
                );
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
            joinDTO.setBirth(joinRequestDTO.getBirth().replaceAll("[^0-9]", ""));
            joinDTO.setTel(joinRequestDTO.getTel());
            joinDTO.setGender(joinRequestDTO.getGender());
            joinDTO.setNickName(joinRequestDTO.getNickName());
            joinDTO.setJoinType("BASIC");

            // 아이디 중복 검증
            int duplicateUserId = joinService.selectUserIdCnt(joinDTO);

            // 5. 아이디 중복 검증
            if(duplicateUserId > 0) {
                throw new JoinException(
                        MemberResultCode.UNAVAILABLE_USER_ID.getResultCode(),
                        MemberResultCode.UNAVAILABLE_USER_ID.getMessage()
                );
            }

            // 닉네임 값이 넘어올 경우 중복 검사
            if(!inputValidate.isEmpty(joinDTO.getNickName())) {

                // 닉네임 적합성 검사
                if(!validator.isValidNickname(joinRequestDTO.getNickName())) {
                    throw new JoinException(
                            MemberResultCode.INVALID_NICKNAME.getResultCode(),
                            MemberResultCode.INVALID_NICKNAME.getMessage()
                    );
                }

                NickNameDTO nickNameDTO = new NickNameDTO();
                nickNameDTO.setNickName(joinDTO.getNickName());
                int selectNicknameCnt = joinService.selectNicknameCnt(nickNameDTO);

                if(selectNicknameCnt > 0) {
                    throw new JoinException(
                            MemberResultCode.UNAVAILABLE_NICKNAME.getResultCode(),
                            MemberResultCode.UNAVAILABLE_NICKNAME.getMessage()
                    );
                }
            }

            // 6. 회원 정보 저장
            int insertResult = joinService.insertMembershipInfo(joinDTO);
            if(insertResult > 0){
                resultCode = MemberResultCode.SUCCESS_SIGN_UP.getResultCode();
                message = MemberResultCode.SUCCESS_SIGN_UP.getMessage();

            } else {
                resultCode = MemberResultCode.FAIL_SIGN_UP.getResultCode();
                message = MemberResultCode.FAIL_SIGN_UP.getMessage();
            }

        } catch (JoinException ex) {
            resultCode = ex.getResultCode();
            message = ex.getMessage();

        } catch (Exception e) {
            resultCode = ResultCode.ERROR.getResultCode();
            message = ResultCode.ERROR.getMessage();
        }

        return ApiResponse.result(resultCode, message);
    }

    public ApiResponse deleteAccount(DeleteAccountRequestDTO deleteAccountRequestDTO) {

        String resultCode = "";
        String message = "";

        try {

            // 1. refresh token이 공백 또는 null인지 확인
            if(deleteAccountRequestDTO.getRefreshToken().isEmpty()) {
                throw new JoinException(
                        MemberResultCode.EMPTY_REFRESH_TOKEN.getResultCode(),
                        MemberResultCode.EMPTY_REFRESH_TOKEN.getMessage());
            }

            // 2. refresh token을 이용해서 membershipNo 구하기
            Map<String, String> resultMap = authService.selectUserInfoByToken(deleteAccountRequestDTO.getRefreshToken());
            if(!resultMap.get("resultCode").equals("200")) {
                throw new InvalidRefreshTokenException(
                        resultMap.get("resultCode"),
                        resultMap.get("message")
                );
            }

            int deleteResult = accountService.deleteMyAccount(resultMap.get("membershipNo"));
            if(deleteResult > 0) {
                resultCode = MemberResultCode.SUCCESS_DELETE_ACCOUNT.getResultCode();
                message = MemberResultCode.SUCCESS_DELETE_ACCOUNT.getMessage();
            } else {
                resultCode = MemberResultCode.FAIL_DELETE_ACCOUNT.getResultCode();
                message = MemberResultCode.FAIL_DELETE_ACCOUNT.getMessage();
            }

        } catch (JoinException ex) {
            resultCode = ex.getResultCode();
            message = ex.getMessage();
        } catch (Exception e) {
            resultCode = ResultCode.ERROR.getResultCode();
            message = ResultCode.ERROR.getMessage();
        }

        return ApiResponse.result(resultCode, message);
    }
}


