package com.barogagi.member.login.controller;

import com.barogagi.config.PasswordConfig;
import com.barogagi.member.info.dto.Member;
import com.barogagi.member.info.service.MemberService;
import com.barogagi.member.login.dto.*;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.member.login.exception.LoginException;
import com.barogagi.member.login.service.AuthService;
import com.barogagi.member.login.service.LoginService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "일반 로그인 & 토큰 재발급", description = "일반 로그인, 토큰 재발급 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final InputValidate inputValidate;

    private final EncryptUtil encryptUtil;

    private final LoginService loginService;
    private final MemberService memberService;
    private final AuthService authService;

    private final PasswordEncoder passwordEncoder;

    private final PasswordConfig passwordConfig;

    private final String API_SECRET_KEY;

    @Autowired
    public LoginController(Environment environment,
                           InputValidate inputValidate,
                           EncryptUtil encryptUtil,
                           LoginService loginService,
                           MemberService memberService,
                           AuthService authService,
                           PasswordEncoder passwordEncoder,
                           PasswordConfig passwordConfig){
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");

        this.inputValidate = inputValidate;
        this.encryptUtil = encryptUtil;
        this.loginService = loginService;
        this.memberService = memberService;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.passwordConfig = passwordConfig;
    }

    @Operation(summary = "로그인", description = "로그인 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인에 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "로그인이 불가능합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "102", description = "회원 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "103", description = "로그인에 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/login")
    public ApiResponse basicMemberLogin(@RequestBody LoginDTO loginRequestDTO){

        logger.info("CALL /api/v1/auth/login");
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
                        throw new LoginException("103", "로그인에 실패하였습니다.");
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

    @Operation(summary = "아이디 찾기 기능", description = "아이디 찾기 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "해당 전화번호로 가입된 아이디입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "전화번호가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "해당 전화번호로 가입된 계정이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/find-user")
    public ApiResponse searchUserId(@RequestBody SearchUserIdDTO searchUserIdRequestDTO){

        logger.info("CALL /api/v1/auth/find-user");
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

    @Operation(summary = "비밀번호 재설정 기능", description = "비밀번호 재설정 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 재설정에 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "아이디, 비밀번호 값이 없습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "300", description = "비밀번호 재설정에 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/password-reset/confirm")
    public ApiResponse updatePassword(@RequestBody LoginDTO vo){

        logger.info("CALL /api/v1/auth/password-reset/confirm");
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
                    String encodedPassword = passwordConfig.passwordEncoder().encode(vo.getPassword());
                    vo.setPassword(encodedPassword);

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

    @Operation(summary = "토큰 재발급", description = "Access 토큰 만료 시, Refresh 토큰으로 Access 토큰 재발급")
    @PostMapping("/token/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @RequestHeader(value = "Refresh-Token", required = false) String refreshHeader,
            @RequestBody(required = false) Map<String, String> body
    ) {

        logger.info("CALL /api/v1/auth/token/refresh");

        try {
            String rt = Optional.ofNullable(refreshHeader)
                    .or(() -> Optional.ofNullable(body == null ? null : body.get("refreshToken")))
                    .orElse(null);

            if (rt == null || rt.isBlank()) {
                return ResponseEntity.status(401).body(Map.of("error", "refresh_required"));
            }

            TokenPair pair = authService.rotate(rt); // ❗️핵심 로직 (아래 2) 참조)

            return ResponseEntity.ok(Map.of(
                    "accessToken", pair.accessToken(),
                    "accessTokenExpiresIn", pair.accessTokenExpiresIn(),
                    "refreshToken", pair.refreshToken(),
                    "refreshTokenExpiresIn", pair.refreshTokenExpiresIn()
            ));

        } catch (InvalidRefreshTokenException e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(Map.of(
                            "resultCode", "400",
                            "errorCode", e.getCode(),
                            "message", e.getMessage(),
                            "needLogin", true
                    ));
        }
    }

    /**
     * 현재 기기 로그아웃: 전달된 refreshToken이 속한 (membershipNo, deviceId)의 VALID 토큰들을 REVOKE
     * 입력 경로:
     *  - 헤더: Refresh-Token: <refresh>
     *  - 바디: { "refreshToken": "<refresh>" }
     */
    @Operation(summary = "현재 기기 로그아웃", description = "현재 기기 로그아웃: 전달된 refreshToken이 속한 (membershipNo, deviceId)의 VALID 토큰들을 REVOKE")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Refresh-Token", required = false) String refreshHeader,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String refresh = refreshHeader;
        if ((refresh == null || refresh.isBlank()) && body != null) {
            refresh = body.get("refreshToken");
        }
        if (refresh != null && !refresh.isBlank()) {
            authService.logout(refresh); // DB REVOKE
        }
        return ResponseEntity.ok(Map.of("resultCode", "200", "message", "로그아웃 되었습니다."));
    }
}
