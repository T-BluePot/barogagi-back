package com.barogagi.region.controller;

import com.barogagi.plan.query.service.PlanQueryService;
import com.barogagi.region.dto.RegionGeoCodeResDTO;
import com.barogagi.region.query.service.RegionGeoCodeService;
import com.barogagi.region.query.service.RegionQueryService;
import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.command.service.ScheduleCommandService;
import com.barogagi.schedule.controller.ScheduleController;
import com.barogagi.schedule.dto.ScheduleDetailResDTO;
import com.barogagi.schedule.query.service.ScheduleQueryService;
import com.barogagi.util.InputValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지역", description = "지역 관련 API")
@RestController
@RequestMapping("/region")
public class RegionController {
    private static final Logger logger = LoggerFactory.getLogger(RegionController.class);

    private final InputValidate inputValidate;

    private final RegionGeoCodeService regionGeoCodeService;

    private final String API_SECRET_KEY;

    public RegionController(Environment environment,
                            InputValidate inputValidate,
                            RegionGeoCodeService regionGeoCodeService) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
        this.inputValidate = inputValidate;
        this.regionGeoCodeService = regionGeoCodeService;
    }
    @Operation(summary = "주소를 x,y 좌표로 변환하는 기능", description = "법정동/행정동 주소를 받아서 x,y 좌표로 변환하는 기능입니다")
    @GetMapping("/geocode")
    public ApiResponse getGeocode(@Parameter(description = "법정동/행정동의 주소 번호", example = "1") @RequestParam Integer regionNum) {

        logger.info("CALL /region/geocode");
        logger.info("[input] SchedulNm={}", regionNum);

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            //if(userIdCheckVO.getApiSecretKey().equals(API_SECRET_KEY)){
            if (true) {

                // TODO. 에러 메시지 정의하기
                if (inputValidate.isInvalidInteger(regionNum)) {
                    resultCode = "101";
                    message = "좌표로 변환할 법정동/행정동 번호가 숫자가 아닙니다.";
                } else {

                    RegionGeoCodeResDTO result = regionGeoCodeService.getGeocode(regionNum);
                    logger.info("result={}", result.toString());

                    if (result == null) {
                        resultCode = "300";
                        message = "조회할 일정이 존재하지 않습니다."; // TODO. 에러 메시지 정의하기

                    } else {
                        resultCode = "200";
                        message = "일정 상세 조회 성공";
                        apiResponse.setData(result);
                        logger.info("#$# result={}", result.toString());
                    }
                }

            } else {
                resultCode = "100";
                message = "잘못된 접근입니다.";
            }
            logger.info("#$# 11 resultCode={}", resultCode);
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
