package com.barogagi.member.login.service;

import com.barogagi.config.PasswordConfig;
import com.barogagi.member.MemberResultCode;
import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.service.MemberService;
import com.barogagi.member.login.dto.*;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.member.login.exception.LoginException;
import com.barogagi.member.login.mapper.LoginMapper;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.ResultCode;
import com.barogagi.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoginService {

    private final LoginMapper loginMapper;
    private final Validator validator;
    private final InputValidate inputValidate;
    private final EncryptUtil encryptUtil;
    private final PasswordConfig passwordConfig;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Autowired
    public LoginService(
                        LoginMapper loginMapper,
                        Validator validator,
                        InputValidate inputValidate,
                        EncryptUtil encryptUtil,
                        PasswordConfig passwordConfig,
                        MemberService memberService,
                        PasswordEncoder passwordEncoder,
                        AuthService authService
                        )
    {
        this.loginMapper = loginMapper;
        this.validator = validator;
        this.inputValidate = inputValidate;
        this.encryptUtil = encryptUtil;
        this.passwordConfig = passwordConfig;
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    public ApiResponse findUser(SearchUserIdDTO searchUserIdDTO) {

        String resultCode = "";
        String message = "";
        List<UserIdDTO> userIdList = null;

        try {

            // 1. API SECRET KEY 일치 여부 확인
            if(!validator.apiSecretKeyCheck(searchUserIdDTO.getApiSecretKey())) {
                throw new LoginException(
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getResultCode(),
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getMessage()
                );
            }

            // 2. 필수 입력값 확인
            if(inputValidate.isEmpty(searchUserIdDTO.getTel())) {
                throw new LoginException(
                        MemberResultCode.EMPTY_DATA.getResultCode(),
                        MemberResultCode.EMPTY_DATA.getMessage()
                );
            }

            searchUserIdDTO.setTel(encryptUtil.encrypt(searchUserIdDTO.getTel()));
            List<UserIdDTO> searchIdList = this.myUserIdList(searchUserIdDTO);

            if(searchIdList.isEmpty()) {
                resultCode = MemberResultCode.NOT_FOUND_ACCOUNT.getResultCode();
                message = MemberResultCode.NOT_FOUND_ACCOUNT.getMessage();
            } else {
                resultCode = MemberResultCode.FOUND_ACCOUNT.getResultCode();
                message = MemberResultCode.FOUND_ACCOUNT.getMessage();
                userIdList = searchIdList;
            }

        } catch (LoginException ex) {
            resultCode = ex.getCode();
            message = ex.getMessage();

        } catch (Exception e) {
            resultCode = ResultCode.ERROR.getResultCode();
            message = ResultCode.ERROR.getMessage();
        }

        return ApiResponse.resultData(userIdList, resultCode, message);
    }

    public ApiResponse updatePasswordProcess(LoginDTO loginDTO) {
        String resultCode = "";
        String message = "";

        try {

            // 1. API SECRET KEY 일치 여부 확인
            if(!validator.apiSecretKeyCheck(loginDTO.getApiSecretKey())) {
                throw new LoginException(
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getResultCode(),
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getMessage()
                );
            }

            // 2. 필수 입력값 확인
            if (
                    inputValidate.isEmpty(loginDTO.getUserId())
                            || inputValidate.isEmpty(loginDTO.getPassword())
            ) {
                throw new LoginException(
                        MemberResultCode.EMPTY_DATA.getResultCode(),
                        MemberResultCode.EMPTY_DATA.getMessage()
                );
            }

            // 3. 비밀번호 암호화
            loginDTO.setPassword(passwordConfig.passwordEncoder().encode(loginDTO.getPassword()));

            // 4. 비밀번호 update
            int updatePassword = this.updatePassword(loginDTO);
            if(updatePassword > 0) {
                resultCode = MemberResultCode.SUCCESS_UPDATE_PASSWORD.getResultCode();
                message = MemberResultCode.SUCCESS_UPDATE_PASSWORD.getMessage();
            } else {
                resultCode = MemberResultCode.FAIL_UPDATE_PASSWORD.getResultCode();
                message = MemberResultCode.FAIL_UPDATE_PASSWORD.getMessage();
            }

        } catch (LoginException ex) {
            resultCode = ex.getCode();
            message = ex.getMessage();
        } catch (Exception e) {
            resultCode = ResultCode.ERROR.getResultCode();
            message = ResultCode.ERROR.getMessage();
        }

        return ApiResponse.result(resultCode, message);
    }

    public ApiResponse login(LoginDTO loginDTO) {

        String resultCode = "";
        String message = "";
        Map<String, Object> dataMap = new HashMap<>();

        try {

            // 1. API SECRET KEY 일치 여부 확인
            if(!validator.apiSecretKeyCheck(loginDTO.getApiSecretKey())) {
                throw new LoginException(
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getResultCode(),
                        ResultCode.NOT_EQUAL_API_SECRET_KEY.getMessage()
                );
            }

            // 2. 필수 입력값 확인
            if(
                    inputValidate.isEmpty(loginDTO.getUserId())
                    || inputValidate.isEmpty(loginDTO.getPassword())
            )
            {
                throw new LoginException(
                        MemberResultCode.EMPTY_DATA.getResultCode(),
                        MemberResultCode.EMPTY_DATA.getMessage()
                );
            }

            // 3. 아이디로 회원정보 조회
            Member member = memberService.selectUserMembershipInfo(loginDTO.getUserId());
            if (null == member) {
                throw new LoginException(
                        MemberResultCode.NOT_FOUND_USER_INFO.getResultCode(),
                        MemberResultCode.NOT_FOUND_USER_INFO.getMessage()
                );
            }

            // 4. 비밀번호 일치 여부
            boolean ok = passwordEncoder.matches(loginDTO.getPassword(), member.getPassword());
            if(!ok) {
                throw new LoginException(
                        MemberResultCode.FAIL_LOGIN.getResultCode(),
                        MemberResultCode.FAIL_LOGIN.getMessage()
                );
            }

            // 5. ACCESS, REFRESH TOKEN 생성 & REFRESH TOKEN 저장
            LoginResponse loginResponse = authService.loginAfterSignup(member.getUserId(), "web-basic");

            resultCode = MemberResultCode.SUCCESS_LOGIN.getResultCode();
            message = MemberResultCode.SUCCESS_LOGIN.getMessage();

            dataMap = Map.of(
                    "accessToken", loginResponse.tokens().accessToken(),
                    "accessTokenExpiresIn", loginResponse.tokens().accessTokenExpiresIn(),
                    "userId", member.getUserId(),
                    "membershipNo", loginResponse.membershipNo(),
                    "refreshToken", loginResponse.tokens().refreshToken(),
                    "refreshTokenExpiresIn", loginResponse.tokens().refreshTokenExpiresIn()
            );

        } catch (LoginException ex) {
            resultCode = ex.getCode();
            message = ex.getMessage();
        } catch (Exception e) {
            resultCode = MemberResultCode.FAIL_LOGIN.getResultCode();
            message = MemberResultCode.FAIL_LOGIN.getMessage();
        }

        return ApiResponse.resultData(dataMap, resultCode, message);
    }

    public ApiResponse refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {

        String resultCode = "";
        String message = "";
        Map<String, Object> data = new HashMap<>();

        try {

            // 1. 필수 입력값 확인
            if (inputValidate.isEmpty(refreshTokenRequestDTO.getRefreshToken())) {
                throw new LoginException(
                        MemberResultCode.EMPTY_DATA.getResultCode(),
                        MemberResultCode.EMPTY_DATA.getMessage()
                );
            }

            // 2. ACCESS, REFRESH TOKEN 재생성
            TokenPair pair = authService.rotate(refreshTokenRequestDTO.getRefreshToken());
            if(null != pair) {
                data.put("accessToken", pair.accessToken());
                data.put("accessTokenExpiresIn", pair.accessTokenExpiresIn());
                data.put("refreshToken", pair.refreshToken());
                data.put("refreshTokenExpiresIn", pair.refreshTokenExpiresIn());

                resultCode = MemberResultCode.SUCCESS_REFRESH_TOKEN.getResultCode();
                message = MemberResultCode.SUCCESS_REFRESH_TOKEN.getMessage();
            }

        } catch (InvalidRefreshTokenException e) {
            resultCode = e.getCode();
            message = e.getMessage();
        } catch (LoginException ex) {
            resultCode = ex.getCode();
            message = ex.getMessage();
        } catch (Exception e) {
            resultCode = ResultCode.ERROR.getResultCode();
            message = ResultCode.ERROR.getMessage();
        }

        return ApiResponse.resultData(data, resultCode, message);
    }

    public ApiResponse logout(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        String resultCode = "";
        String message = "";

        try {

            // 1. 필수 입력값 확인
            if(inputValidate.isEmpty(refreshTokenRequestDTO.getRefreshToken())) {
                throw new LoginException(
                        MemberResultCode.EMPTY_DATA.getResultCode(),
                        MemberResultCode.EMPTY_DATA.getMessage()
                );
            }

            // 2. 로그아웃
            authService.logout(refreshTokenRequestDTO.getRefreshToken()); // DB REVOKE

            resultCode = MemberResultCode.SUCCESS_LOGOUT.getResultCode();
            message = MemberResultCode.SUCCESS_LOGOUT.getMessage();

        } catch (LoginException ex) {
            resultCode = ex.getCode();
            message = ex.getMessage();
        } catch (Exception e) {
            resultCode = ResultCode.ERROR.getResultCode();
            message = ResultCode.ERROR.getMessage();
        }

        return ApiResponse.result(resultCode, message);
    }

    public int selectMemberCnt(LoginDTO loginDTO){
        return loginMapper.selectMemberCnt(loginDTO);
    }

    public LoginVO findByUserId(LoginDTO loginDTO) {
        return loginMapper.findByUserId(loginDTO);
    }

    public List<UserIdDTO> myUserIdList(SearchUserIdDTO searchUserIdDTO){ return loginMapper.myUserIdList(searchUserIdDTO);}

    public int updatePassword(LoginDTO loginDTO){
        return loginMapper.updatePassword(loginDTO);
    }

    public LoginVO findMembershipNo(LoginVO vo) {
        return loginMapper.findMembershipNo(vo);
    }
}