package com.barogagi.member.login.controller;

import com.barogagi.member.login.service.LoginService;
import com.barogagi.member.login.vo.LoginVO;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
    private final EncryptUtil encryptUtil;
    private final LoginService loginService;

    private final String API_SECRET_KEY;

    @Autowired
    public LoginController(Environment environment,
                           InputValidate inputValidate,
                           EncryptUtil encryptUtil,
                           LoginService loginService){
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");

        this.inputValidate = inputValidate;
        this.encryptUtil = encryptUtil;
        this.loginService = loginService;
    }

    @Operation(summary = "로그인", description = "로그인 기능입니다. apiSecretKey, userId와 password 값만 보내주세요.")
    @PostMapping("/basic/membership/login")
    public ApiResponse basicMemberLogin(@RequestBody LoginVO loginVO){

        logger.info("CALL /login/basic/membership/login");
        logger.info("[input] API_SECRET_KEY={}", loginVO.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if(loginVO.getApiSecretKey().equals(API_SECRET_KEY)){

                if(inputValidate.isEmpty(loginVO.getUserId()) || inputValidate.isEmpty(loginVO.getPassword())){
                    resultCode = "101";
                    message = "로그인이 불가능합니다.";
                } else {
                    // 비밀번호 암호화
                    loginVO.setPassword(encryptUtil.hashEncodeString(loginVO.getPassword()));

                    // 동일한 아이디와 비밀번호가 있는지 확인(로그인)
                    int membershipInfoCnt = loginService.selectMemberCnt(loginVO);

                    logger.info("@@ membershipInfoCnt={}", membershipInfoCnt);
                    if(membershipInfoCnt > 0) {
                        resultCode = "200";
                        message = "로그인에 성공하였습니다.";
                    } else {
                        resultCode = "300";
                        message = "로그인에 실패하였습니다.";
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

    @Operation(summary = "아이디 찾기 기능", description = "아이디 찾기 기능입니다. apiSecretKey, tel 값만 보내주시면 됩니다.")
    @PostMapping("/basic/membership/userId/search")
    public ApiResponse searchUserId(@RequestBody LoginVO loginVO){

        logger.info("CALL /login/basic/membership/userId/search");
        logger.info("[input] API_SECRET_KEY={}", loginVO.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if(loginVO.getApiSecretKey().equals(API_SECRET_KEY)){

                if(inputValidate.isEmpty(loginVO.getTel())){
                    resultCode = "101";
                    message = "전화번호가 존재하지 않습니다.";

                } else {

                    loginVO.setTel(encryptUtil.encrypt(loginVO.getTel()));
                    logger.info("tel={}", loginVO.getTel());
                    List<LoginVO> myUserIdList = loginService.myUserIdList(loginVO);

                    int userIdCnt = myUserIdList.size();
                    if(userIdCnt > 0){
                        resultCode = "200";
                        message = "해당 전화번호로 가입된 아이디입니다.";

                        List<Map<String, Object>> userIdList = new ArrayList<>();
                        for(LoginVO vo : myUserIdList){
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
    public ApiResponse updatePassword(@RequestBody LoginVO loginVO){

        logger.info("CALL /login/basic/membership/password/update");
        logger.info("[input] API_SECRET_KEY={}", loginVO.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if(loginVO.getApiSecretKey().equals(API_SECRET_KEY)){

                if(inputValidate.isEmpty(loginVO.getUserId()) || inputValidate.isEmpty(loginVO.getPassword())){
                    resultCode = "101";
                    message = "아이디, 비밀번호 값이 없습니다.";

                } else {
                    // 비밀번호 암호화
                    loginVO.setPassword(encryptUtil.hashEncodeString(loginVO.getPassword()));

                    // 비밀번호 update
                    int updatePassword = loginService.updatePassword(loginVO);
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
