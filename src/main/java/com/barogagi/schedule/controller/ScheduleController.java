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
        logger.info("[input] scheduleNum={}", scheduleNum);

        ScheduleDetailResDTO result;
        try {
            // TODO. 해당 사용자의 일정이 맞는지도 체크해야 함
            //if(userIdCheckVO.getApiSecretKey().equals(API_SECRET_KEY)){
            result = scheduleQueryService.getScheduleDetail(scheduleNum);

        } catch (Exception e) {
            return ApiResponse.error("404", "일정 조회 실패");
        }


        return ApiResponse.success(result, "일정 조회 성공");
    }


    @Operation(summary = "일정 생성 기능",
            description = "일정을 생성하는 기능입니다.<br>" +
            "- 생성된 일정은 '일정 등록'과정을 거쳐야 DB에 저장됩니다.<br>" +
            "- 사용자가 이 API로 생성된 일정을 확인한 후 '일정 생성하기' 버튼을 누르면 '일정 등록' API를 호출해 주세요.")
    @PostMapping("/create")
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

        logger.info("CALL /schedule/create");
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


    @Operation(summary = "일정 등록 기능",
            description = "일정을 등록(DB에 저장)하는 기능입니다.<br>" +
                    "- '일정 생성하기' 버튼을 눌렀을 때 호출되는 API입니다.<br>" +
                    "- '일정 생성' API로 받은 응답 DTO를 그대로 보내주세요.")
    @PostMapping("")
    public ApiResponse registSchedule(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "일정 등록 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "일정 등록 요청 예시",
                                    value = "{\n" +
                                            "    \"scheduleNum\": null,\n" +
                                            "    \"scheduleNm\": \"서울 데이트 코스\",\n" +
                                            "    \"startDate\": \"2025-07-01\",\n" +
                                            "    \"endDate\": \"2025-07-01\",\n" +
                                            "    \"planRegistResDTOList\": [\n" +
                                            "      {\n" +
                                            "        \"startTime\": \"08:30\",\n" +
                                            "        \"endTime\": \"09:00\",\n" +
                                            "        \"itemNum\": 10,\n" +
                                            "        \"itemNm\": \"프랜차이즈카페\",\n" +
                                            "        \"categoryNum\": 2,\n" +
                                            "        \"categoryNm\": \"카페\",\n" +
                                            "        \"planNm\": \"제비꽃다방\",\n" +
                                            "        \"planLink\": \"http://place.map.kakao.com/24944966\",\n" +
                                            "        \"planDescription\": \"분위기 좋은 한옥 카페 '더숲 초소책방'은 서울 종로구에 위치해 있으며, 숲속의 아늑함을 느낄 수 있는 넓은 야외 공간과 아름다운 서울 풍경을 감상할 수 있는 2층 테라스가 특징입니다.\",\n" +
                                            "        \"planAddress\": \"서울 종로구 창의문로 146\",\n" +
                                            "        \"regionNm\": \"종로구\",\n" +
                                            "        \"regionNum\": 1,\n" +
                                            "        \"planTagRegistResDTOList\": [\n" +
                                            "          { \"tagNum\": 14, \"tagNm\": \"디저트맛집\" },\n" +
                                            "          { \"tagNum\": 15, \"tagNm\": \"인스타핫플\" }\n" +
                                            "        ]\n" +
                                            "      },\n" +
                                            "      {\n" +
                                            "        \"startTime\": \"14:00\",\n" +
                                            "        \"endTime\": \"15:00\",\n" +
                                            "        \"itemNum\": 2,\n" +
                                            "        \"itemNm\": \"한식\",\n" +
                                            "        \"categoryNum\": 1,\n" +
                                            "        \"categoryNm\": \"식사\",\n" +
                                            "        \"planNm\": \"식사\",\n" +
                                            "        \"planLink\": \"http://place.map.kakao.com/1581311090\",\n" +
                                            "        \"planDescription\": \"분위기 좋은 카페로 뷰가 좋은 곳입니다.\",\n" +
                                            "        \"planAddress\": \"서울 중구 무교로 17\",\n" +
                                            "        \"regionNm\": \"종로구\",\n" +
                                            "        \"regionNum\": 1,\n" +
                                            "        \"planTagRegistResDTOList\": [\n" +
                                            "          { \"tagNum\": 17, \"tagNm\": \"조용한\" }\n" +
                                            "        ]\n" +
                                            "      },\n" +
                                            "      {\n" +
                                            "        \"startTime\": \"15:30\",\n" +
                                            "        \"endTime\": \"19:00\",\n" +
                                            "        \"itemNum\": 15,\n" +
                                            "        \"itemNm\": \"놀이공원\",\n" +
                                            "        \"categoryNum\": 4,\n" +
                                            "        \"categoryNm\": \"놀거리\",\n" +
                                            "        \"planNm\": \"구룡관 혜화본점\",\n" +
                                            "        \"planLink\": \"http://place.map.kakao.com/40669117\",\n" +
                                            "        \"planDescription\": \"혜화에서 분위기 좋고 저렴한 중식 술집으로는 구룡관 혜화본점이 추천됩니다.\",\n" +
                                            "        \"planAddress\": \"서울 종로구 창경궁로 258-5\",\n" +
                                            "        \"regionNm\": \"종로구\",\n" +
                                            "        \"regionNum\": 1,\n" +
                                            "        \"planTagRegistResDTOList\": [\n" +
                                            "          { \"tagNum\": 4, \"tagNm\": \"테마파크\" }\n" +
                                            "        ]\n" +
                                            "      }\n" +
                                            "    ]\n" +
                                            "}"
                            )
                    )
            )
            @RequestBody ScheduleRegistResDTO scheduleRegistResDTO
    ) {

        logger.info("CALL /schedule");
        logger.info("[input] scheduleRegistResDTO={}", scheduleRegistResDTO);

        int result;
        try {
            //if(userIdCheckVO.getApiSecretKey().equals(API_SECRET_KEY)){
            result = scheduleCommandService.registSchedule(scheduleRegistResDTO);

        } catch (Exception e) {
            return ApiResponse.error("404", "일정 생성 실패");
        }


        return ApiResponse.success(result, "일정 생성 성공");
    }

}
