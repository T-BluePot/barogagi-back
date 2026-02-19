package com.barogagi.schedule.controller;

import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.dto.ScheduleRegistReqDTO;
import com.barogagi.schedule.dto.ScheduleRegistResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.servlet.http.HttpServletRequest;

public interface SwaggerScheduleController {

    @Operation(
            summary = "내 일정 목록 조회 기능",
            description = "일정 목록을 조회하는 기능입니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A100",
                            description = "API SECRET KEY 불일치"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A401",
                            description = "접근 권한이 존재하지 않습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "S202",
                            description = "일정 조회에 성공하였습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "오류가 발생하였습니다."
                    )
            }
    )
    ApiResponse getScheduleList(HttpServletRequest request);

    @Operation(
            summary = "일정 상세 조회 기능",
            description = "일정을 상세 조회하는 기능입니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A100",
                            description = "API SECRET KEY 불일치"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A401",
                            description = "접근 권한이 존재하지 않습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "S202",
                            description = "일정 상세 조회에 성공하였습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "오류가 발생하였습니다."
                    )
            }
    )
    ApiResponse getScheduleDetail(
            @Parameter(description = "조회할 일정 번호", example = "1")
            Integer scheduleNum,
            HttpServletRequest request
    );

    @Operation(
            summary = "일정 생성 기능",
            description = "일정을 생성하는 기능입니다.<br>" +
                    "- 사용자가 직접 일정을 생성하는 경우는 2가지가 존재합니다.<br>" +
                    "&nbsp;&nbsp;&nbsp;&nbsp; CASE 1. 카카오 장소 검색 API를 사용해서 사용자가 가고 싶은 장소를 선택하는 경우, 카카오 장소 검색 API에서 검색한 placeName, placeUrl, addressName을 보내주세요.<br>" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 주의 1) 사용자가 랜덤 카테고리를 선택한 경우 isRandomCategory=\"Y\"로 전달해 주세요. 이때 categoryNum은 전달하지 않아도 됩니다.<br>" +
                    "&nbsp;&nbsp;&nbsp;&nbsp; CASE 2. 사용자가 세부일정을 직접 텍스트로 입력하는 경우(ex, 친구집 방문), 세부일정명을 planNm 필드에 담아 보내주세요.<br>" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 주의 1) 반드시 isUserAdded=\"Y\"로 전달해 주세요.<br>" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 주의 2) 사용자가 직접 일정을 생성하는 경우 planTagRegistReqDTOList 값을 전달할 필요는 없습니다.<br>" +
                    "- 생성된 일정은 '일정 등록'과정을 거쳐야 DB에 저장됩니다.<br>" +
                    "- 사용자가 이 API로 생성된 일정을 확인한 후 '일정 생성하기' 버튼을 누르면 '일정 등록' API를 호출해 주세요.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A100",
                            description = "API SECRET KEY 불일치"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A401",
                            description = "접근 권한이 존재하지 않습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "S203",
                            description = "일정 저장 성공"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "일정 저장 실패"
                    )
            }
    )
    ApiResponse createSchedule(
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
            ScheduleRegistReqDTO scheduleRegistReqDTO,
            HttpServletRequest request
    );

    @Operation(
            summary = "일정 저장 기능",
            description = "일정을 DB에 저장하는 기능입니다.<br>" +
                    "- '일정 생성하기' 버튼을 눌렀을 때 호출되는 API입니다.<br>" +
                    "- '일정 생성' API로 받은 응답 DTO를 그대로 보내주세요.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A100",
                            description = "API SECRET KEY 불일치"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A401",
                            description = "접근 권한이 존재하지 않습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "S301",
                            description = "태그를 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "S302",
                            description = "아이템을 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "S303",
                            description = "지역을 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "S203",
                            description = "일정 저장 성공"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "일정 저장 실패"
                    )
            }
    )
    ApiResponse saveSchedule(
            HttpServletRequest request,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "일정 등록 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "AI 추천 일정만 저장 요청 예시",
                                            value = "{\n" +
                                                    "    \"scheduleNum\": null,\n" +
                                                    "    \"scheduleNm\": \"서울 데이트 코스\",\n" +
                                                    "    \"startDate\": \"2025-07-01\",\n" +
                                                    "    \"endDate\": \"2025-07-01\",\n" +
                                                    "    \"scheduleTagRegistResDTOList\": [\n" +
                                                    "      { \"tagNm\": \"핫플\", \"tagNum\": 5 },\n" +
                                                    "      { \"tagNm\": \"활동적인\", \"tagNum\": 8 }\n" +
                                                    "    ],\n" +
                                                    "    \"planRegistResDTOList\": [\n" +
                                                    "      {\n" +
                                                    "        \"planSource\": \"AI\",\n" +
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
                                                    "        \"planSource\": \"AI\",\n" +
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
                                                    "        \"planSource\": \"AI\",\n" +
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
                                    ),
                                    @ExampleObject(
                                            name = "AI 추천 + 사용자 추가 일정 저장 요청 예시",
                                            value = "{\n" +
                                                    "    \"scheduleNum\": null,\n" +
                                                    "    \"scheduleNm\": \"3월 여행 일정\",\n" +
                                                    "    \"startDate\": \"2026-03-11\",\n" +
                                                    "    \"endDate\": \"2026-03-11\",\n" +
                                                    "    \"scheduleTagRegistResDTOList\": [\n" +
                                                    "      { \"tagNm\": \"즐거운\", \"tagNum\": 6 },\n" +
                                                    "      { \"tagNm\": \"저렴한\", \"tagNum\": 4 }\n" +
                                                    "    ],\n" +
                                                    "    \"planRegistResDTOList\": [\n" +
                                                    "      {\n" +
                                                    "        \"planSource\": \"AI\",\n" +
                                                    "        \"startTime\": \"08:30\",\n" +
                                                    "        \"endTime\": \"09:00\",\n" +
                                                    "        \"itemNum\": 10,\n" +
                                                    "        \"itemNm\": \"프랜차이즈카페\",\n" +
                                                    "        \"categoryNum\": 2,\n" +
                                                    "        \"categoryNm\": \"카페\",\n" +
                                                    "        \"planNm\": \"부빙\",\n" +
                                                    "        \"planLink\": \"http://place.map.kakao.com/20459372\",\n" +
                                                    "        \"planDescription\": \"'부빙'은 계절마다 변하는 감성 빙수를 판매하는 디저트 카페입니다.\",\n" +
                                                    "        \"planAddress\": \"서울 종로구 창의문로 136\",\n" +
                                                    "        \"regionNm\": \"종로구\",\n" +
                                                    "        \"regionNum\": 1,\n" +
                                                    "        \"planTagRegistResDTOList\": [\n" +
                                                    "          { \"tagNum\": 14, \"tagNm\": \"디저트맛집\" },\n" +
                                                    "          { \"tagNum\": 15, \"tagNm\": \"인스타핫플\" }\n" +
                                                    "        ]\n" +
                                                    "      },\n" +
                                                    "      {\n" +
                                                    "        \"planSource\": \"USER_PLACE\",\n" +
                                                    "        \"startTime\": \"14:00\",\n" +
                                                    "        \"endTime\": \"15:00\",\n" +
                                                    "        \"itemNum\": 2,\n" +
                                                    "        \"itemNm\": \"한식\",\n" +
                                                    "        \"categoryNum\": 1,\n" +
                                                    "        \"categoryNm\": \"식사\",\n" +
                                                    "        \"planNm\": \"카카오프렌즈 코엑스점\",\n" +
                                                    "        \"planLink\": \"http://place.map.kakao.com/26338954\",\n" +
                                                    "        \"planDescription\": null,\n" +
                                                    "        \"planAddress\": \"서울 강남구 삼성동 159\",\n" +
                                                    "        \"regionNm\": \"강남구\",\n" +
                                                    "        \"regionNum\": 9,\n" +
                                                    "        \"planTagRegistResDTOList\": []\n" +
                                                    "      },\n" +
                                                    "      {\n" +
                                                    "        \"planSource\": \"USER_CUSTOM\",\n" +
                                                    "        \"startTime\": \"15:30\",\n" +
                                                    "        \"endTime\": \"19:00\",\n" +
                                                    "        \"itemNum\": 15,\n" +
                                                    "        \"itemNm\": \"놀이공원\",\n" +
                                                    "        \"categoryNum\": 4,\n" +
                                                    "        \"categoryNm\": \"놀거리\",\n" +
                                                    "        \"planNm\": \"친구집 방문\",\n" +
                                                    "        \"planLink\": null,\n" +
                                                    "        \"planDescription\": null,\n" +
                                                    "        \"planAddress\": null,\n" +
                                                    "        \"regionNm\": \"종로구\",\n" +
                                                    "        \"regionNum\": 1,\n" +
                                                    "        \"planTagRegistResDTOList\": []\n" +
                                                    "      }\n" +
                                                    "    ]\n" +
                                                    "}"
                                    )
                            }
                    )
            )
            ScheduleRegistResDTO scheduleRegistResDTO
    );

    @Operation(
            summary = "일정 수정 기능",
            description = "DB에 저장되어 있는 일정을 수정하는 기능입니다.<br>" +
                    "- 전체 일정 내 세부 일정을 수정/삭제하는 경우에도 이 API를 호출해주세요.<br>" +
                    "- '일정 번호'가 반드시 필요합니다.<br>" +
                    "- '일정 조회' API로 받은 응답 DTO를 수정하여 보내주세요.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A100",
                            description = "잘못된 접근입니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A401",
                            description = "접근 권한이 존재하지 않습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "S401",
                            description = "일정 정보를 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "G201",
                            description = "태그 정보를 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "I201",
                            description = "아이템 정보를 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "R201",
                            description = "지역 정보를 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "일정 수정 성공"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "C500",
                            description = "서버 오류가 발생했습니다."
                    )
            }
    )
    ApiResponse updateSchedule(
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
            ScheduleRegistResDTO scheduleRegistResDTO, HttpServletRequest request
    );

    @Operation(
            summary = "일정 전체 삭제 기능",
            description = "일정 전체를 DB에서 삭제하는 기능입니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A100",
                            description = "잘못된 접근입니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A401",
                            description = "접근 권한이 존재하지 않습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "S401",
                            description = "일정 정보를 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "일정 삭제 성공"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "C500",
                            description = "서버 오류가 발생했습니다."
                    )
            }
    )
    ApiResponse deleteSchedule(
            @Parameter(description = "삭제할 일정 번호", example = "1")
            Integer scheduleNum,
            HttpServletRequest request
    );
}