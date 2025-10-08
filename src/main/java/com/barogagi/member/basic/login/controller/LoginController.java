package com.barogagi.member.basic.login.controller;

import com.barogagi.config.PasswordConfig;
import com.barogagi.member.basic.login.dto.LoginVO;
import com.barogagi.member.basic.login.service.LoginService;
import com.barogagi.member.basic.login.dto.LoginDTO;
import com.barogagi.member.basic.login.dto.SearchUserIdDTO;
import com.barogagi.member.basic.login.dto.UserIdDTO;
import com.barogagi.member.login.entity.RefreshToken;
import com.barogagi.member.login.repository.RefreshTokenRepository;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "일반 로그인", description = "일반 로그인 관련 API")
@RestController
@RequestMapping("/login")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final InputValidate inputValidate;
    private final EncryptUtil encryptUtil;
    private final LoginService loginService;
    private final PasswordConfig passwordConfig;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    private final String API_SECRET_KEY;

    @Autowired
    public LoginController(Environment environment,
                           InputValidate inputValidate,
                           EncryptUtil encryptUtil,
                           LoginService loginService,
                           PasswordConfig passwordConfig,
                           JwtUtil jwtUtil,
                           RefreshTokenRepository refreshTokenRepository) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");

        this.inputValidate = inputValidate;
        this.encryptUtil = encryptUtil;
        this.loginService = loginService;
        this.passwordConfig = passwordConfig;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Operation(summary = "로그인", description = "로그인 기능입니다.")
    @PostMapping("/basic/membership/login")
    public ApiResponse basicMemberLogin(@RequestBody LoginDTO loginDTO,
                                        HttpServletResponse response) {

        logger.info("CALL /login/basic/membership/login");
        logger.info("[input] API_SECRET_KEY={}", loginDTO.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if (!API_SECRET_KEY.equals(loginDTO.getApiSecretKey())) {
                resultCode = "100";
                message = "잘못된 접근입니다.";
            } else if (inputValidate.isEmpty(loginDTO.getUserId()) || inputValidate.isEmpty(loginDTO.getPassword())) {
                resultCode = "101";
                message = "로그인이 불가능합니다.";
            } else {
                // 1) DB에서 사용자 조회 (userId 기준, JOIN_TYPE = BASIC)
                LoginVO user = loginService.findByUserId(loginDTO);
                if (user == null) {
                    resultCode = "300";
                    message = "로그인에 실패하였습니다.";

                } else {
                    Long membershipNo = Long.valueOf(user.getMembershipNo());

                    // 2) 저장된 해시 가져와 matches로 검증 (절대 encode해서 equals 비교 금지)
                    boolean ok = passwordConfig.passwordEncoder().matches(loginDTO.getPassword(), user.getPassword());
                    if (!ok) {
                        resultCode = "300";
                        message = "로그인에 실패하였습니다.";
                    } else {
                        // 3) 토큰 발급
                        String deviceId = "web-basic";
                        String accessToken  = jwtUtil.generateAccessToken(membershipNo, user.getUserId());
                        String refreshToken = jwtUtil.generateRefreshToken(membershipNo, deviceId);

                        // 4) Refresh 저장 (DB 테이블이 있다면)
                        RefreshToken rt = new RefreshToken();
                        rt.setMembershipNo(membershipNo);
                        rt.setDeviceId(deviceId);
                        rt.setToken(refreshToken);
                        rt.setStatus("VALID");
                        rt.setCreatedAt(LocalDateTime.now());
                        rt.setExpiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpSeconds()));
                        refreshTokenRepository.save(rt); // MyBatis면 insert 호출

                        Map<String, Object> data = Map.of(
                                "accessToken", accessToken,
                                "accessTokenExpiresIn", jwtUtil.getAccessExpSeconds(),
                                "userId", user.getUserId(),
                                "membershipNo", user.getMembershipNo(),
                                "refreshToken", refreshToken,
                                "refreshTokenExpiresIn", jwtUtil.getRefreshExpSeconds()
                        );
                        apiResponse.setData(data);

                        resultCode = "200";
                        message = "로그인에 성공하였습니다.";
                    }
                }
            }
        } catch (Exception e) {
            logger.error("basic login error", e);
            resultCode = "400";
            message = "오류가 발생하였습니다.";
            // 필요 시 적절한 예외 처리
        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }
        return apiResponse;
    }


    @Operation(summary = "아이디 찾기 기능", description = "아이디 찾기 기능입니다.")
    @PostMapping("/basic/membership/userId/search")
    public ApiResponse searchUserId(@RequestBody SearchUserIdDTO searchUserIdDTO){

        logger.info("CALL /login/basic/membership/userId/search");
        logger.info("[input] API_SECRET_KEY={}", searchUserIdDTO.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if(searchUserIdDTO.getApiSecretKey().equals(API_SECRET_KEY)){

                if(inputValidate.isEmpty(searchUserIdDTO.getTel())){
                    resultCode = "101";
                    message = "전화번호가 존재하지 않습니다.";

                } else {

                    searchUserIdDTO.setTel(encryptUtil.encrypt(searchUserIdDTO.getTel()));
                    logger.info("tel={}", searchUserIdDTO.getTel());
                    List<UserIdDTO> myUserIdList = loginService.myUserIdList(searchUserIdDTO);

                    int userIdCnt = myUserIdList.size();
                    if(userIdCnt > 0){
                        resultCode = "200";
                        message = "해당 전화번호로 가입된 아이디입니다.";
                        apiResponse.setData(myUserIdList);

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

    @Operation(summary = "비밀번호 재설정 기능", description = "비밀번호 재설정 기능입니다.")
    @PostMapping("/basic/membership/password/update")
    public ApiResponse updatePassword(@RequestBody LoginDTO passwordResertDTO){

        logger.info("CALL /login/basic/membership/password/update");
        logger.info("[input] API_SECRET_KEY={}", passwordResertDTO.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if(passwordResertDTO.getApiSecretKey().equals(API_SECRET_KEY)){

                if(inputValidate.isEmpty(passwordResertDTO.getUserId()) || inputValidate.isEmpty(passwordResertDTO.getPassword())){
                    resultCode = "101";
                    message = "아이디, 비밀번호 값이 없습니다.";

                } else {
                    // 비밀번호 암호화
                    String encodePassword = passwordConfig.passwordEncoder().encode(passwordResertDTO.getPassword());
                    passwordResertDTO.setPassword(encodePassword);

                    // 비밀번호 update
                    int updatePassword = loginService.updatePassword(passwordResertDTO);
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
