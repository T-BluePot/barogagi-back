package com.barogagi.member.login.controller;

import com.barogagi.config.PasswordConfig;
import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.service.MemberService;
import com.barogagi.member.login.dto.*;
import com.barogagi.member.login.exception.LoginException;
import com.barogagi.member.login.service.AuthService;
import com.barogagi.member.login.service.LoginService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "일반 로그인", description = "일반 로그인 관련 API")
@RestController
@RequestMapping("/login")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final InputValidate inputValidate;
    private final PasswordConfig passwordConfig;

    private final EncryptUtil encryptUtil;
    private final JwtUtil jwtUtil;

    private final LoginService loginService;
    private final MemberService memberService;
    private final AuthService authService;

    private final PasswordEncoder passwordEncoder;

    private final String API_SECRET_KEY;

    @Autowired
    public LoginController(Environment environment,
                           InputValidate inputValidate,
                           EncryptUtil encryptUtil,
                           LoginService loginService,
                           MemberService memberService,
                           AuthService authService,
                           JwtUtil jwtUtil,
                           PasswordConfig passwordConfig,
                           PasswordEncoder passwordEncoder){
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");

        this.inputValidate = inputValidate;
        this.encryptUtil = encryptUtil;
        this.loginService = loginService;
        this.memberService = memberService;
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.passwordConfig = passwordConfig;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "로그인", description = "로그인 기능입니다. apiSecretKey, userId와 password 값만 보내주세요.")
    @PostMapping("/basic/membership/login")
    public ApiResponse basicMemberLogin(@RequestBody LoginDTO loginRequestDTO){

        logger.info("CALL /login/basic/membership/login");
        logger.info("[input] API_SECRET_KEY={}", loginRequestDTO.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if (loginRequestDTO.getApiSecretKey().equals(API_SECRET_KEY)) {

                if (inputValidate.isEmpty(loginRequestDTO.getUserId()) || inputValidate.isEmpty(loginRequestDTO.getPassword())) {
                    throw new LoginException("101", "로그인이 불가능합니다.");
                } else {

                    logger.info("@@@ userId={}", loginRequestDTO.getUserId());
                    logger.info("@@@ password={}", loginRequestDTO.getPassword());

                    // 로그인 성공 -> 사용자 정보 조회(membershipNo, userId 등 토큰에 넣을 값)
                    Member member = memberService.selectUserMembershipInfo(loginRequestDTO.getUserId());
                    if (null == member) {
                        throw new LoginException("102", "회원 정보가 존재하지 않습니다.");
                    }

                    boolean ok = passwordEncoder.matches(loginRequestDTO.getPassword(), member.getPassword());

                    logger.info("@@ ok={}", ok);
                    if(!ok) {
                        throw new LoginException("103", "로그인에 실패하였습니다");
                    }

                    String userId = member.getUserId();

                    // ACCESS, REFRESH TOKEN 생싱 & REFRESH TOKEN 저장
                    LoginResponse loginResponse = authService.loginAfterSignup(userId, "web-basic");
                    Map<String, Object> loginResponseMap = Map.of(
                            "accessToken", loginResponse.tokens().accessToken(),
                            "accessTokenExpiresIn", loginResponse.tokens().accessTokenExpiresIn(),
                            "userId", userId,
                            "membershipNo", loginResponse.membershipNo(),
                            "refreshToken", loginResponse.tokens().refreshToken(),
                            "refreshTokenExpiresIn", loginResponse.tokens().refreshTokenExpiresIn()
                    );

                    resultCode = "200";
                    message = "로그인에 성공하였습니다.";
                    apiResponse.setData(loginResponseMap);
                }

            } else {
                throw new LoginException("100", "잘못된 접근입니다.");
            }
        } catch (LoginException ex) {
            resultCode = ex.getCode();
            message = ex.getMessage();

        } catch (Exception e) {
            resultCode = "400";
            message = "오류가 발생하였습니다.";
        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }
        return  apiResponse;
    }

    @Operation(summary = "아이디 찾기 기능", description = "아이디 찾기 기능입니다. apiSecretKey, tel 값만 보내주시면 됩니다.")
    @PostMapping("/basic/membership/userId/search")
    public ApiResponse searchUserId(@RequestBody SearchUserIdDTO searchUserIdRequestDTO){

        logger.info("CALL /login/basic/membership/userId/search");
        logger.info("[input] API_SECRET_KEY={}", searchUserIdRequestDTO.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if(searchUserIdRequestDTO.getApiSecretKey().equals(API_SECRET_KEY)){

                if(inputValidate.isEmpty(searchUserIdRequestDTO.getTel())){
                    resultCode = "101";
                    message = "전화번호가 존재하지 않습니다.";

                } else {

                    searchUserIdRequestDTO.setTel(encryptUtil.encrypt(searchUserIdRequestDTO.getTel()));
                    logger.info("tel={}", searchUserIdRequestDTO.getTel());
                    List<UserIdDTO> myUserIdList = loginService.myUserIdList(searchUserIdRequestDTO);

                    int userIdCnt = myUserIdList.size();
                    if(userIdCnt > 0){
                        resultCode = "200";
                        message = "해당 전화번호로 가입된 아이디입니다.";

                        List<Map<String, Object>> userIdList = new ArrayList<>();
                        for(UserIdDTO vo : myUserIdList){
                            Map<String, Object> map = new HashMap<>();
                            map.put("userId", vo.getUserId());
                            userIdList.add(map);
                        }
                        apiResponse.setData(userIdList);

                    } else {
                        resultCode = "201";
                        message = "해당 전화번호로 가입된 계정이 존재하지 않습니다.";
                    }
                }

            } else{
                resultCode = "100";
                message = "잘못된 접근입니다.";
            }

        } catch (Exception e) {
            resultCode = "400";
            message = "오류가 발생하였습니다.";
            throw new RuntimeException(e);
        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }
        return  apiResponse;
    }

    @Operation(summary = "비밀번호 재설정 기능", description = "비밀번호 재설정 기능입니다. apiSecretKey, userId, password값만 보내주시면 됩니다.")
    @PostMapping("/basic/membership/password/update")
    public ApiResponse updatePassword(@RequestBody LoginDTO vo){

        logger.info("CALL /login/basic/membership/password/update");
        logger.info("[input] API_SECRET_KEY={}", vo.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if(vo.getApiSecretKey().equals(API_SECRET_KEY)){

                if(inputValidate.isEmpty(vo.getUserId()) || inputValidate.isEmpty(vo.getPassword())){
                    resultCode = "101";
                    message = "아이디, 비밀번호 값이 없습니다.";

                } else {
                    // 비밀번호 암호화
                    vo.setPassword(encryptUtil.hashEncodeString(vo.getPassword()));

                    // 비밀번호 update
                    int updatePassword = loginService.updatePassword(vo);
                    logger.info("@@ updatePassword={}", updatePassword);

                    if(updatePassword > 0){
                        resultCode = "200";
                        message = "비밀번호 재설정에 성공하였습니다.";
                    } else{
                        resultCode = "300";
                        message = "비밀번호 재설정에 실패하였습니다.";
                    }
                }

            } else{
                resultCode = "100";
                message = "잘못된 접근입니다.";
            }

        } catch (Exception e) {
            resultCode = "400";
            message = "오류가 발생하였습니다.";
            throw new RuntimeException(e);
        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }
        return  apiResponse;
    }
}
