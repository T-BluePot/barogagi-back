package com.barogagi.schedule.controller;

import com.barogagi.member.join.vo.JoinVO;
import com.barogagi.member.join.vo.UserIdCheckVO;
import com.barogagi.member.login.controller.LoginController;
import com.barogagi.plan.dto.PlanRegistReqDTO;
import com.barogagi.plan.query.service.PlanQueryService;
import com.barogagi.plan.query.vo.PlanDetailVO;
import com.barogagi.region.dto.RegionRegistReqDTO;
import com.barogagi.region.dto.RegionSearchResDTO;
import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.command.service.ScheduleCommandService;
import com.barogagi.schedule.dto.ScheduleDetailResDTO;
import com.barogagi.schedule.dto.ScheduleRegistReqDTO;
import com.barogagi.schedule.dto.ScheduleRegistResDTO;
import com.barogagi.schedule.query.service.ScheduleQueryService;
import com.barogagi.schedule.query.vo.ScheduleDetailVO;
import com.barogagi.util.InputValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Tag(name = "일정", description = "일정 관련 API")
@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    private final InputValidate inputValidate;

    private final ScheduleQueryService scheduleQueryService;
    private final ScheduleCommandService scheduleCommandService;
    private final PlanQueryService planQueryService;

    private final String API_SECRET_KEY;

    public ScheduleController(Environment environment,
                              InputValidate inputValidate,
                              ScheduleQueryService scheduleQueryService,
                              ScheduleCommandService scheduleCommandService,
                              PlanQueryService planQueryService) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
        this.inputValidate = inputValidate;
        this.scheduleQueryService = scheduleQueryService;
        this.scheduleCommandService = scheduleCommandService;
        this.planQueryService = planQueryService;
    }

    @Operation(summary = "일정 상세 조회 기능", description = "일정을 상세 조회하는 기능입니다.")
    @GetMapping("/detail")
    public ApiResponse getScheduleDetail(@Parameter(description = "조회할 일정 번호", example = "1")
                                         @RequestParam Integer scheduleNum) {

        logger.info("CALL /schedule/detail");
        logger.info("[input] SchedulNm={}", scheduleNum);

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            //if(userIdCheckVO.getApiSecretKey().equals(API_SECRET_KEY)){
            if (true) {

                // TODO. 해당 사용자의 일정이 맞는지도 체크해야 함
                if (inputValidate.isInvalidInteger(scheduleNum)) {
                    resultCode = "101";
                    message = "조회할 일정을 선택해주세요.";
                } else {

                    ScheduleDetailResDTO result = scheduleQueryService.getScheduleDetail(scheduleNum);
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


    @Operation(summary = "일정 생성 기능", description = "일정을 생성하는 기능입니다.")
    @PostMapping("")
    public ApiResponse createSchedule(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "일정 등록 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "일정 등록 요청 예시",
                                    value = "{\n" +
                                            "  \"scheduleNm\": \"서울 데이트 코스\",\n" +
                                            "  \"startDate\": \"2025-07-01\",\n" +
                                            "  \"endDate\": \"2025-07-01\",\n" +
                                            "  \"comment\": \"분위기 좋은 카페 추천해주세요\",\n" +
                                            "  \"scheduleTagRegistReqDTOList\": [\n" +
                                            "        { \"tagNm\": \"핫플\", \"tagNum\": 5 },\n" +
                                            "        { \"tagNm\": \"활동적인\", \"tagNum\": 8 }\n" +
                                            "    ],\n" +
                                            "  \"planRegistReqDTOList\": [\n" +
                                            "    {\n" +
                                            "      \"startTime\": \"08:00\",\n" +
                                            "      \"endTime\": \"09:00\",\n" +
                                            "      \"itemNum\": 10,\n" +
                                            "      \"categoryNum\": 2,\n" +
                                            "      \"regionRegistReqDTOList\": [\n" +
                                            "        {\n" +
                                            "          \"regionNum\": 1\n" +
                                            "        }\n" +
                                            "      ],\n" +
                                            "      \"planTagRegistReqDTOList\": [\n" +
                                            "        { \"tagNm\": \"디저트맛집\", \"tagNum\": 14 },\n" +
                                            "        { \"tagNm\": \"인스타핫플\", \"tagNum\": 15 }\n" +
                                            "      ]\n" +
                                            "    },\n" +
                                            "    {\n" +
                                            "      \"startTime\": \"14:00\",\n" +
                                            "      \"endTime\": \"15:00\",\n" +
                                            "      \"itemNum\": 2,\n" +
                                            "      \"categoryNum\": 1,\n" +
                                            "      \"regionRegistReqDTOList\": [\n" +
                                            "        {\n" +
                                            "          \"regionNum\": 1\n" +
                                            "        }\n" +
                                            "      ],\n" +
                                            "      \"planTagRegistReqDTOList\": [\n" +
                                            "        { \"tagNm\": \"조용한\", \"tagNum\": 17 }\n" +
                                            "      ]\n" +
                                            "    },\n" +
                                            "    {\n" +
                                            "      \"startTime\": \"15:30\",\n" +
                                            "      \"endTime\": \"19:00\",\n" +
                                            "      \"itemNum\": 15,\n" +
                                            "      \"categoryNum\": 4,\n" +
                                            "      \"regionRegistReqDTOList\": [\n" +
                                            "        {\n" +
                                            "          \"regionNum\": 1\n" +
                                            "        },\n" +
                                            "        {\n" +
                                            "          \"regionNum\": 2\n" +
                                            "        }\n" +
                                            "      ],\n" +
                                            "      \"planTagRegistReqDTOList\": [\n" +
                                            "        { \"tagNm\": \"테마파크\", \"tagNum\": 4 }\n" +
                                            "      ]\n" +
                                            "    }\n" +
                                            "  ]\n" +
                                            "}"
                            )
                    )
            )
            @RequestBody ScheduleRegistReqDTO scheduleRegistReqDTO
    ) {

        logger.info("CALL /schedule");
        logger.info("[input] scheduleRegistReqDTO={}", scheduleRegistReqDTO);

        ScheduleRegistResDTO result;
        try {
            //if(userIdCheckVO.getApiSecretKey().equals(API_SECRET_KEY)){
            result = scheduleCommandService.createSchedule(scheduleRegistReqDTO);

        } catch (Exception e) {
            return ApiResponse.error("404", "일정 생성 실패");
        }


        return ApiResponse.success(result, "일정 생성 성공");
    }
}
