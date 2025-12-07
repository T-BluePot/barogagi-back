package com.barogagi.schedule.controller;

//import com.barogagi.member.join.vo.JoinVO;
//import com.barogagi.member.join.vo.UserIdCheckVO;
import com.barogagi.member.login.controller.LoginController;
import com.barogagi.plan.dto.PlanRegistReqDTO;
import com.barogagi.plan.query.service.PlanQueryService;
import com.barogagi.plan.query.vo.PlanDetailVO;
import com.barogagi.region.dto.RegionRegistReqDTO;
import com.barogagi.region.dto.RegionSearchResDTO;
import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.command.service.ScheduleCommandService;
import com.barogagi.schedule.dto.ScheduleDetailResDTO;
import com.barogagi.schedule.dto.ScheduleListResDTO;
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

    @Operation(summary = "내 일정 목록 조회 기능", description = "일정 목록을 조회하는 기능입니다.")
    @GetMapping("/list")
    public ApiResponse getScheduleList() {

        logger.info("CALL /schedule/list");

        List<ScheduleListResDTO> result;
        try {
            // TODO. token으로 사용자 확인 후, 해당 사용자의 일정만 조회하도록 수정해야 함
            //if(userIdCheckVO.getApiSecretKey().equals(API_SECRET_KEY)){
            result = scheduleQueryService.getScheduleList(1);

        } catch (Exception e) {
            return ApiResponse.error("404", "일정 목록 조회 실패");
        }


        return ApiResponse.success(result, "일정 목록 조회 성공");
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
            "- 사용자가 직접 일정을 생성하는 경우는 2가지가 존재합니다.<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp; CASE 1. 카카오 장소 검색 API를 사용해서 사용자가 가고 싶은 장소를 선택하는 경우, 카카오 장소 검색 API에서 검색한 placeName, placeUrl, addressName을 보내주세요.<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 주의 1) 사용자가 랜덤 카테고리를 선택한 경우 isRandomCategory=\"Y\"로 전달해 주세요. 이때 categoryNum은 전달하지 않아도 됩니다.<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp; CASE 2. 사용자가 세부일정을 직접 텍스트로 입력하는 경우(ex, 친구집 방문), 세부일정명을 planNm 필드에 담아 보내주세요.<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 주의 1) 반드시 isUserAdded=\"Y\"로 전달해 주세요.<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 주의 2) 사용자가 직접 일정을 생성하는 경우 planTagRegistReqDTOList 값을 전달할 필요는 없습니다.<br>" +
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
                                            "  \"scheduleNm\": \"서울 카페투어\",\n" +
                                            "  \"startDate\": \"2025-12-01\",\n" +
                                            "  \"endDate\": \"2025-12-01\",\n" +
                                            "  \"comment\": \"분위기 좋은 카페 추천해주세요\",\n" +
                                            "  \"scheduleTagRegistReqDTOList\": [\n" +
                                            "    { \"tagNm\": \"핫플\", \"tagNum\": 5 },\n" +
                                            "    { \"tagNm\": \"활동적인\", \"tagNum\": 8 }\n" +
                                            "  ],\n" +
                                            "  \"planRegistReqDTOList\": [\n" +
                                            "    {\n" +
                                            "      \"startTime\": \"08:00\",\n" +
                                            "      \"endTime\": \"09:00\",\n" +
                                            "      \"itemNum\": 10,\n" +
                                            "      \"categoryNum\": 2,\n" +
                                            "      \"isUserAdded\": \"N\",\n" +
                                            "      \"isRandomCategory\": \"Y\",\n" +
                                            "      \"regionRegistReqDTOList\": [\n" +
                                            "        { \"regionNum\": 1 }\n" +
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
                                            "      \"isUserAdded\": \"Y\",\n" +
                                            "      \"isRandomCategory\": \"N\",\n" +
                                            "      \"userAddedPlaceDTO\": {\n" +
                                            "        \"placeName\": \"카카오프렌즈 코엑스점\",\n" +
                                            "        \"placeUrl\": \"http://place.map.kakao.com/26338954\",\n" +
                                            "        \"addressName\": \"서울 강남구 삼성동 159\"\n" +
                                            "      }\n" +
                                            "    },\n" +
                                            "    {\n" +
                                            "      \"startTime\": \"15:30\",\n" +
                                            "      \"endTime\": \"19:00\",\n" +
                                            "      \"itemNum\": 15,\n" +
                                            "      \"categoryNum\": 4,\n" +
                                            "      \"isUserAdded\": \"Y\",\n" +
                                            "      \"planNm\": \"친구집 방문\",\n" +
                                            "      \"regionRegistReqDTOList\": [\n" +
                                            "        { \"regionNum\": 1 }\n" +
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


    @Operation(summary = "일정 저장 기능",
            description = "일정을 DB에 저장하는 기능입니다.<br>" +
                    "- '일정 생성하기' 버튼을 눌렀을 때 호출되는 API입니다.<br>" +
                    "- '일정 생성' API로 받은 응답 DTO를 그대로 보내주세요.")
    @PostMapping("/save")
    public ApiResponse saveSchedule(
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

        logger.info("CALL /schedule/save");
        logger.info("[input] scheduleRegistResDTO={}", scheduleRegistResDTO);

        int result;
        try {
            //if(userIdCheckVO.getApiSecretKey().equals(API_SECRET_KEY)){
            result = scheduleCommandService.saveSchedule(scheduleRegistResDTO);

        } catch (Exception e) {
            return ApiResponse.error("404", "일정 저장 실패");
        }


        return ApiResponse.success(result, "일정 저장 성공");
    }

    @Operation(summary = "일정 수정 기능",
            description = "DB에 저장되어 있는 일정을 수정하는 기능입니다.<br>" +
            "- 전체 일정 내 세부 일정을 수정/삭제하는 경우에도 이 API를 호출해주세요.<br>" +
            "- '일정 번호'가 반드시 필요합니다.<br>" +
            "- '일정 조회' API로 받은 응답 DTO를 수정하여 보내주세요.")
    @PutMapping("/")
    public ApiResponse updateSchedule(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "일정 등록 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "일정 수정 요청 예시",
                                    value = "{\n" +
                                            "    \"scheduleNum\": 1,\n" +
                                            "    \"scheduleNm\": \"서울 데이트 코스2\",\n" +
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

        logger.info("CALL /schedule/update");
        logger.info("[input] scheduleRegistResDTO={}", scheduleRegistResDTO);

        boolean result;
        try {
            //if(userIdCheckVO.getApiSecretKey().equals(API_SECRET_KEY)){
            result = scheduleCommandService.updateSchedule(scheduleRegistResDTO);

        } catch (Exception e) {
            return ApiResponse.error("404", "일정 저장 실패");
        }


        return ApiResponse.success(result, "일정 저장 성공");
    }

    @Operation(summary = "일정 전체 삭제 기능",
            description = "일정 전체를 DB에서 삭제하는 기능입니다.")
    @DeleteMapping("/")
    public ApiResponse deleteSchedule(@Parameter(description = "삭제할 일정 번호", example = "1")
                                      @RequestParam Integer scheduleNum) {

        logger.info("CALL /schedule/delete");
        logger.info("[input] scheduleNum={}", scheduleNum);

        boolean result;
        try {
            //if(userIdCheckVO.getApiSecretKey().equals(API_SECRET_KEY)){
            // TODO. membershipNo를 토큰으로부터 받아와야 함
            result = scheduleCommandService.deleteSchedule(scheduleNum, 1);

            if (!result) return ApiResponse.error("404", "일정 삭제 실패");

        } catch (Exception e) {
            return ApiResponse.error("404", "일정 삭제 실패");
        }


        return ApiResponse.success(result, "일정 삭제 성공");
    }
}
