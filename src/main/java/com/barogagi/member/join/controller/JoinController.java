package com.barogagi.member.join.controller;

import com.barogagi.member.join.dto.JoinRequestDTO;
import com.barogagi.member.join.dto.NickNameDTO;
import com.barogagi.member.join.service.JoinService;
import com.barogagi.member.join.dto.JoinDTO;
import com.barogagi.member.join.dto.UserIdCheckDTO;
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

@Tag(name = "일반 회원가입", description = "일반 회원가입 관련 API")
@RestController
@RequestMapping("/membership/join")
public class JoinController {
    private static final Logger logger = LoggerFactory.getLogger(JoinController.class);

    private final JoinService joinService;
    private final InputValidate inputValidate;
    private final EncryptUtil encryptUtil;
    private final Validator validator;

    private final String API_SECRET_KEY;

    @Autowired
    public JoinController(Environment environment,
                          JoinService joinService,
                          InputValidate inputValidate,
                          EncryptUtil encryptUtil,
                          Validator validator){
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
        this.joinService = joinService;
        this.inputValidate = inputValidate;
        this.encryptUtil = encryptUtil;
        this.validator = validator;
    }

    @Operation(summary = "아이디 중복 체크 기능", description = "아이디 중복 체크 기능입니다.")
    @PostMapping("/basic/membership/userId/check")
    public ApiResponse checkUserId(@RequestBody UserIdCheckDTO userIdCheckDTO) {

        logger.info("CALL /membership/join/basic/membership/userId/check");
        logger.info("[input] API_SECRET_KEY={}", userIdCheckDTO.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            if(userIdCheckDTO.getApiSecretKey().equals(API_SECRET_KEY)){

                if(inputValidate.isEmpty(userIdCheckDTO.getUserId())) {
                    resultCode = "101";
                    message = "아이디를 입력해주세요.";
                } else{

                    if(!validator.isValidId(userIdCheckDTO.getUserId())) {
                        resultCode = "102";
                        message = "적합한 아이디가 아닙니다.";
                    } else {
                        JoinDTO joinDTO = new JoinDTO();
                        joinDTO.setUserId(userIdCheckDTO.getUserId());

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

    @Operation(summary = "회원가입 정보 저장 기능", description = "회원가입 정보 저장 기능입니다.")
    @PostMapping("/basic/membership/insert")
    public ApiResponse membershipJoinInsert(@RequestBody JoinRequestDTO joinRequestDTO){

        logger.info("CALL /membership/join/basic/membership/insert");
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
                        joinRequestDTO.setTel(encryptUtil.encrypt(joinRequestDTO.getTel()));

                        // 이메일 값이 넘어오면 암호화
                        if(!inputValidate.isEmpty(joinRequestDTO.getEmail())){
                            joinRequestDTO.setEmail(encryptUtil.encrypt(joinRequestDTO.getEmail()));
                        }

                        joinRequestDTO.setPassword(encryptUtil.hashEncodeString(joinRequestDTO.getPassword()));

                        // 회원 정보 저장(회원가입)
                        JoinDTO joinDTO = new JoinDTO();
                        joinDTO.setUserId(joinRequestDTO.getUserId());
                        joinDTO.setPassword(joinRequestDTO.getPassword());
                        joinDTO.setEmail(joinRequestDTO.getEmail());
                        joinDTO.setBirth(joinRequestDTO.getBirth());
                        joinDTO.setTel(joinRequestDTO.getTel());
                        joinDTO.setGender(joinRequestDTO.getGender());
                        joinDTO.setNickName(joinRequestDTO.getNickName());
                        joinDTO.setJoinType("BASIC");

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

    @Operation(summary = "닉네임 중복 체크 API", description = "닉네임 중복 체크 API입니다.")
    @PostMapping("/check/duplicate/nickname")
    public ApiResponse checkDuplicateNickname(@RequestBody NickNameDTO nickNameDTO){

        logger.info("CALL /membership/join/check/duplicate/nickname");
        logger.info("[input] API_SECRET_KEY={}", nickNameDTO.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            if(nickNameDTO.getApiSecretKey().equals(API_SECRET_KEY)){

                // 필수 입력값
                if(inputValidate.isEmpty(nickNameDTO.getNickName())){

                    // 필수 입력값 중 빈 값이 존재. insert 중지
                    resultCode = "101";
                    message = "닉네임 정보를 입력해주세요.";

                } else{

                    if(!validator.isValidNickname(nickNameDTO.getNickName())) {
                        resultCode = "102";
                        message = "적합하지 않는 닉네임입니다.";
                    } else {
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
}
