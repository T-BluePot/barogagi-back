package com.barogagi.member.basic.join.controller;

import com.barogagi.config.PasswordConfig;
import com.barogagi.member.basic.join.dto.NickNameDTO;
import com.barogagi.member.basic.join.service.JoinService;
import com.barogagi.member.basic.join.dto.JoinDTO;
import com.barogagi.member.basic.join.dto.JoinRequestDTO;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.member.login.service.AccountService;
import com.barogagi.member.login.service.AuthService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.Validator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Tag(name = "일반 회원가입", description = "일반 회원가입 관련 API")
@RestController
@RequestMapping("/api/v1/users")
public class JoinController {
    private static final Logger logger = LoggerFactory.getLogger(JoinController.class);

    private final JoinService joinService;
    private final AccountService accountService;
    private final AuthService authService;
    private final InputValidate inputValidate;
    private final EncryptUtil encryptUtil;
    private final Validator validator;
    private final PasswordConfig passwordConfig;

    private final String API_SECRET_KEY;

    @Autowired
    public JoinController(Environment environment,
                          JoinService joinService,
                          AccountService accountService,
                          AuthService authService,
                          InputValidate inputValidate,
                          EncryptUtil encryptUtil,
                          Validator validator,
                          PasswordConfig passwordConfig) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
        this.joinService = joinService;
        this.accountService = accountService;
        this.authService = authService;
        this.inputValidate = inputValidate;
        this.encryptUtil = encryptUtil;
        this.validator = validator;
        this.passwordConfig = passwordConfig;
    }

    @Operation(summary = "아이디 중복 체크 기능", description = "아이디 중복 체크 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "해당 아이디 사용이 가능합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "아이디를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "102", description = "적합한 아이디가 아닙니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "300", description = "해당 아이디 사용이 불가능합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @GetMapping("/userid/exists")
    public ApiResponse checkUserId(@RequestHeader("API-KEY") String apiSecretKey, @RequestParam String userId) {

        logger.info("CALL /api/v1/users/userId/exists");
        logger.info("[input] API_SECRET_KEY={}", apiSecretKey);
        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            if(apiSecretKey.equals(API_SECRET_KEY)){

                if(inputValidate.isEmpty(userId)) {
                    resultCode = "101";
                    message = "아이디를 입력해주세요.";
                } else{

                    if(!validator.isValidId(userId)) {
                        resultCode = "102";
                        message = "적합한 아이디가 아닙니다.";
                    } else {
                        JoinDTO joinDTO = new JoinDTO();
                        joinDTO.setUserId(userId);

                        int checkUserId = joinService.checkUserId(joinDTO);
                        logger.info("@@ checkUserId={}", checkUserId);

                        if(checkUserId > 0){
                            resultCode = "300";
                            message = "해당 아이디 사용이 불가능합니다.";

                        } else{
                            resultCode = "200";
                            message = "해당 아이디 사용이 가능합니다.";
                        }
                    }
                }

            } else {
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
        return apiResponse;
    }

    @Operation(summary = "회원가입 정보 저장 기능", description = "회원가입 정보 저장 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입에 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "회원가입에 필요한 정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "102", description = "적합한 아이디, 비밀번호, 닉네임이 아닙니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "103", description = "해당 아이디에 대한 회원 정보가 이미 존재합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "300", description = "회원가입에 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping
    public ApiResponse membershipJoinInsert(@RequestBody JoinRequestDTO joinRequestDTO){

        logger.info("CALL /api/v1/users");
        logger.info("[input] API_SECRET_KEY={}", joinRequestDTO.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            if(joinRequestDTO.getApiSecretKey().equals(API_SECRET_KEY)){

                // 필수 입력값(아이디, 비밀번호, 휴대전화번호 값이 빈 값이 아닌지 확인)
                // 선택 입력값(이메일, 생년월일, 성별, 닉네임)
                if(inputValidate.isEmpty(joinRequestDTO.getUserId()) || inputValidate.isEmpty(joinRequestDTO.getPassword()) || inputValidate.isEmpty(joinRequestDTO.getTel())){

                    // 필수 입력값 중 빈 값이 존재. insert 중지
                    resultCode = "101";
                    message = "회원가입에 필요한 정보를 입력해주세요.";

                } else{

                    // 아이디, 비밀번호, 닉네임 적합성 검사
                    if(!(validator.isValidId(joinRequestDTO.getUserId()) && validator.isValidPassword(joinRequestDTO.getPassword()) && validator.isValidNickname(joinRequestDTO.getNickName()))){
                        resultCode = "102";
                        message = "적합한 아이디, 비밀번호, 닉네임이 아닙니다.";
                    } else {
                        // 입력값 암호화 & 값 세팅
                        // 휴대전화번호, 비밀번호 암호화
                        joinRequestDTO.setTel(encryptUtil.encrypt(joinRequestDTO.getTel().replaceAll("[^0-9]", "")));

                        // 이메일 값이 넘어오면 암호화
                        if(!inputValidate.isEmpty(joinRequestDTO.getEmail())){
                            joinRequestDTO.setEmail(encryptUtil.encrypt(joinRequestDTO.getEmail()));
                        }

                        String encodedPassword = passwordConfig.passwordEncoder().encode(joinRequestDTO.getPassword());
                        joinRequestDTO.setPassword(encodedPassword);

                        // 회원 정보 저장(회원가입)
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
                        int duplicateUserId = joinService.checkUserId(joinDTO);

                        if(duplicateUserId > 0) {
                            resultCode = "103";
                            message = "해당 아이디에 대한 회원 정보가 이미 존재합니다.";
                        } else {
                            int insertResult = joinService.insertMembershipInfo(joinDTO);
                            logger.info("@@ insertResult={}", insertResult);

                            if(insertResult > 0){
                                resultCode = "200";
                                message = "회원가입에 성공하였습니다.";
                            } else{
                                resultCode = "300";
                                message = "회원가입에 실패하였습니다.";
                            }
                        }
                    }
                }

            } else {
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

        return apiResponse;
    }

    @Operation(summary = "닉네임 중복 체크 API", description = "닉네임 중복 체크 API입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이용 가능한 닉네임입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "닉네임 데이터를 보내주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "102", description = "적합하지 않는 닉네임입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "103", description = "이미 존재하는 닉네임입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @GetMapping("/nickname/exists")
    public ApiResponse checkDuplicateNickname(@RequestHeader("API-KEY") String apiSecretKey,
                                              @RequestParam String nickname){

        logger.info("CALL /api/v1/user/nickname/exists");
        logger.info("[input] API_SECRET_KEY={}", apiSecretKey);

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            if(apiSecretKey.equals(API_SECRET_KEY)){

                // 필수 입력값
                if(inputValidate.isEmpty(nickname)){

                    // 필수 입력값 중 빈 값이 존재. insert 중지
                    resultCode = "101";
                    message = "닉네임 정보를 입력해주세요.";

                } else{

                    if(!validator.isValidNickname(nickname)) {
                        resultCode = "102";
                        message = "적합하지 않는 닉네임입니다.";
                    } else {

                        NickNameDTO nickNameDTO = new NickNameDTO();
                        nickNameDTO.setNickName(nickname);

                        int nickNameCnt = joinService.checkNickName(nickNameDTO);
                        logger.info("nickNameCnt={}", nickNameCnt);
                        if(nickNameCnt > 0) {
                            resultCode = "103";
                            message = "이미 존재하는 닉네임입니다.";
                        } else {
                            resultCode = "200";
                            message = "이용 가능한 닉네임입니다.";
                        }
                    }
                }

            } else {
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

        return apiResponse;
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "refresh token이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 탈퇴되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "300", description = "회원 탈퇴 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @DeleteMapping("/me")
    public ApiResponse deleteMe(@RequestHeader(value = "Refresh-Token", required = false) String refreshHeader,
                                @RequestBody(required = false) Map<String, String> body) {

        logger.info("CALL /api/v1/users/me");

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            String refreshToken = Optional.ofNullable(refreshHeader)
                    .or(() -> Optional.ofNullable(body == null ? null : body.get("refreshToken")))
                    .orElse(null);

            if (refreshToken == null || refreshToken.isBlank()) {
                throw new InvalidRefreshTokenException("100", "refresh token이 존재하지 않습니다.");
            }

            // refresh token을 이용해서 membershipNo 구하기

            Map<String, String> resultMap = authService.selectUserInfoByToken(refreshToken);
            if(!resultMap.get("resultCode").equals("200")) {
                throw new InvalidRefreshTokenException(resultMap.get("resultCode"), resultMap.get("message"));
            }

            String membershipNo = resultMap.get("membershipNo");

            int deleteResult = accountService.deleteMyAccount(membershipNo);
            if(deleteResult > 0) {
                resultCode = "200";
                message = "회원 탈퇴되었습니다.";
            } else {
                resultCode = "300";
                message = "회원 탈퇴 실패하였습니다.";
            }

        } catch (InvalidRefreshTokenException ex) {
            resultCode = ex.getCode();
            message = ex.getMessage();

        } catch (Exception e) {
            logger.error("error", e);
            resultCode = "400";
            message = "오류가 발생하였습니다.";

        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }

        return apiResponse;
    }
}
