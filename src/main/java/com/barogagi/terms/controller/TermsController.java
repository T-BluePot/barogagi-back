package com.barogagi.terms.controller;

import com.barogagi.member.login.service.LoginService;
import com.barogagi.member.login.vo.LoginVO;
import com.barogagi.response.ApiResponse;
import com.barogagi.terms.service.TermsService;
import com.barogagi.terms.vo.TermsVO;
import com.barogagi.util.InputValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "약관", description = "약관 관련 API")
@RestController
@RequestMapping("/terms")
public class TermsController {
    private static final Logger logger = LoggerFactory.getLogger(TermsController.class);

    private InputValidate inputValidate;
    private TermsService termsService;
    private LoginService loginService;

    private final String API_SECRET_KEY;

    @Autowired
    public TermsController(Environment environment, InputValidate inputValidate,
                           TermsService termsService, LoginService loginService){
        this.inputValidate = inputValidate;
        this.termsService = termsService;
        this.loginService = loginService;
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
    }

    @Operation(summary = "약관 목록 조회", description = "약관 목록 조회 기능입니다. apiSecretKey와 termsType값만 보내주시면 됩니다.")
    @PostMapping("/list")
    public ApiResponse termsList(@RequestBody TermsVO vo){
        logger.info("CALL /terms/list");
        logger.info("[input] API_SECRET_KEY={}", vo.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if(vo.getApiSecretKey().equals(API_SECRET_KEY)) {

                if(inputValidate.isEmpty(vo.getTermsType())) {
                    resultCode = "101";
                    message = "조회하실 약관의 종류 값이 존재하지 않습니다.";
                } else {
                    List<TermsVO> termsList = termsService.selectTermsList(vo);

                    int termsCnt = termsList.size();
                    logger.info("termsCnt={}", termsCnt);
                    if(termsCnt > 0) {
                        resultCode = "200";
                        message = "약관 조회에 성공하였습니다.";
                        apiResponse.setData(termsList);

                    } else {
                        resultCode = "102";
                        message = "약관이 존재하지 않습니다.";
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

    @Operation(summary = "약관 동의 여부 저장", description = "약관 동의 여부 저장 기능입니다. apiSecretKey와 termsNum, agreeYn, userId 값을 보내주세요.")
    @PostMapping("/agree/insert")
    public ApiResponse insertTermsAgree(@RequestBody TermsVO vo) {
        logger.info("CALL /agree/insert");
        logger.info("[input] API_SECRET_KEY={}", vo.getApiSecretKey());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if(vo.getApiSecretKey().equals(API_SECRET_KEY)) {

                String userId = vo.getUserId();
                LoginVO lvo = new LoginVO();
                lvo.setUserId(userId);

                LoginVO loginVO = loginService.findMembershipNo(lvo);
                if(null != loginVO) {
                    List<TermsVO> termsAgreeList = vo.getTermsAgreeList();
                    for(TermsVO termsVO : termsAgreeList) {
                        termsVO.setMembershipNo(loginVO.getMembershipNo());
                    }
                    String resCode = termsService.insertTermsAgreeList(termsAgreeList);
                    if(resCode.equals("200")) {
                        resultCode = "200";
                        message = "약관 저장에 성공하였습니다.";
                    } else {
                        resultCode = "300";
                        message = "약관 저장에 실패하였습니다.";
                    }

                } else {
                    resultCode = "101";
                    message = "해당 사용자의 정보가 존재하지 않습니다.";
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
