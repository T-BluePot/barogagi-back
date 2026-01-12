package com.barogagi.member.login.service;

import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.login.dto.*;
import com.barogagi.member.login.exception.LoginException;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginService {

    private final Validator validator;
    private final InputValidate inputValidate;
    private final EncryptUtil encryptUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final UserMembershipRepository userMembershipRepository;

    public ApiResponse findUserIdByTel(String apiSecretKey, String tel) {

        String resultCode = "";
        String message = "";
        List<UserIdDTO> userIdList = null;

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new LoginException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(tel)) {
            throw new LoginException(ErrorCode.EMPTY_DATA);
        }

        List<UserIdDTO> searchIdList = userMembershipRepository.findByTel(encryptUtil.encrypt(tel.replaceAll("[^0-9]", "")));

        if(searchIdList.isEmpty()) {
            resultCode = ErrorCode.NOT_FOUND_ACCOUNT.getCode();
            message = ErrorCode.NOT_FOUND_ACCOUNT.getMessage();
        } else {
            resultCode = ErrorCode.FOUND_ACCOUNT.getCode();
            message = ErrorCode.FOUND_ACCOUNT.getMessage();
            userIdList = searchIdList;
        }

        return ApiResponse.resultData(userIdList, resultCode, message);
    }

    public ApiResponse resetPassword(LoginDTO loginDTO) {
        String resultCode = "";
        String message = "";

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(loginDTO.getApiSecretKey())) {
            throw new LoginException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if (inputValidate.isEmpty(loginDTO.getUserId()) || inputValidate.isEmpty(loginDTO.getPassword())) {
            throw new LoginException(ErrorCode.EMPTY_DATA);
        }

        // 3. 회원 정보 존재 여부
        UserMembershipInfo userInfo = userMembershipRepository.findByUserId(loginDTO.getUserId());

        if(null == userInfo) {
            throw new LoginException(ErrorCode.NOT_FOUND_USER_ID);
        }

        // 3. 비밀번호 암호화
        loginDTO.setPassword(passwordEncoder.encode(loginDTO.getPassword()));

        // 4. 비밀번호 update
        userInfo.changePassword(loginDTO.getPassword());

        return ApiResponse.result(ErrorCode.SUCCESS_UPDATE_PASSWORD.getCode(), ErrorCode.SUCCESS_UPDATE_PASSWORD.getMessage());
    }

    public ApiResponse login(LoginDTO loginDTO) {

        String resultCode = "";
        String message = "";
        Map<String, Object> dataMap = new HashMap<>();

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(loginDTO.getApiSecretKey())) {
            throw new LoginException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(loginDTO.getUserId()) || inputValidate.isEmpty(loginDTO.getPassword())) {
            throw new LoginException(ErrorCode.EMPTY_DATA);
        }

        // 3. 아이디로 회원정보 조회
        UserMembershipInfo userInfo = userMembershipRepository.findByUserId(loginDTO.getUserId());
        if (null == userInfo) {
            throw new LoginException(ErrorCode.NOT_FOUND_USER_INFO);
        }

        // 4. 비밀번호 일치 여부
        boolean ok = passwordEncoder.matches(loginDTO.getPassword(), userInfo.getPassword());
        if(!ok) {
            throw new LoginException(ErrorCode.FAIL_LOGIN);
        }

        // 5. ACCESS, REFRESH TOKEN 생성 & REFRESH TOKEN 저장
        LoginResponse loginResponse = authService.loginAfterSignup(userInfo.getUserId(), "web-basic");

        resultCode = loginResponse.tokens().resultCode();
        message = loginResponse.tokens().message();

        dataMap = Map.of(
                "accessToken", loginResponse.tokens().accessToken(),
                "accessTokenExpiresIn", loginResponse.tokens().accessTokenExpiresIn(),
                "userId", userInfo.getUserId(),
                "membershipNo", loginResponse.membershipNo(),
                "refreshToken", loginResponse.tokens().refreshToken(),
                "refreshTokenExpiresIn", loginResponse.tokens().refreshTokenExpiresIn()
        );

        return ApiResponse.resultData(dataMap, resultCode, message);
    }

    public ApiResponse refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {

        String resultCode = "";
        String message = "";
        Map<String, Object> data = new HashMap<>();

        // 1. 필수 입력값 확인
        if (inputValidate.isEmpty(refreshTokenRequestDTO.getRefreshToken())) {
            throw new LoginException(ErrorCode.EMPTY_DATA);
        }

        // 2. ACCESS, REFRESH TOKEN 재생성
        TokenPair pair = authService.rotate(refreshTokenRequestDTO.getRefreshToken());

        resultCode = pair.resultCode();
        message = pair.message();

        if(!resultCode.equals("R200")) {
            return ApiResponse.error(resultCode, message);
        }

        data.put("accessToken", pair.accessToken());
        data.put("accessTokenExpiresIn", pair.accessTokenExpiresIn());
        data.put("refreshToken", pair.refreshToken());
        data.put("refreshTokenExpiresIn", pair.refreshTokenExpiresIn());

        return ApiResponse.resultData(data, resultCode, message);
    }

    public ApiResponse logout(RefreshTokenRequestDTO refreshTokenRequestDTO) {

        try {

            // 1. 필수 입력값 확인
            if(inputValidate.isEmpty(refreshTokenRequestDTO.getRefreshToken())) {
                throw new LoginException(ErrorCode.EMPTY_DATA);
            }

            // 2. 로그아웃
            authService.logout(refreshTokenRequestDTO.getRefreshToken()); // DB REVOKE
            return ApiResponse.result(ErrorCode.SUCCESS_LOGOUT);

        } catch (Exception e) {
            return ApiResponse.result(ErrorCode.FAIL_LOGOUT);
        }
    }
}