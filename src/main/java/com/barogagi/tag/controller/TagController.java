package com.barogagi.tag.controller;

import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.dto.ScheduleRegistReqDTO;
import com.barogagi.tag.dto.TagSearchReqDTO;
import com.barogagi.tag.dto.TagSearchResDTO;
import com.barogagi.tag.query.service.TagQueryService;
import com.barogagi.util.InputValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "태그", description = "태그 관련 API")
@RestController
@RequestMapping("/api/v1/tag")
public class TagController {
    private static final Logger logger = LoggerFactory.getLogger(TagController.class);

    private final InputValidate inputValidate;

    private final String API_SECRET_KEY;

    private final TagQueryService tagQueryService;

    public TagController(Environment environment, InputValidate inputValidate,
                         TagQueryService tagQueryService) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
        this.inputValidate = inputValidate;
        this.tagQueryService = tagQueryService;
    }

    @Operation(
            summary = "태그 목록 검색",
            description = "태그 목록을 검색하는 기능입니다.<br>" +
                    "- 여행 스타일 태그(S): categoryNum을 null로 전달하세요.<br>" +
                    "- 상세 일정 태그(P): 해당 일정의 카테고리 번호(categoryNum)를 전달하세요.<br>" +
                    "검색 결과는 최대 10개의 태그를 반환합니다."
    )
    @PostMapping("/search-list")
    public ApiResponse searchList(@RequestBody TagSearchReqDTO tagSearchReqDTO) {

        logger.info("CALL /tag/searchList");

        List<TagSearchResDTO> result = tagQueryService.searchList(tagSearchReqDTO);
        return ApiResponse.success(result, "태그 목록 검색 성공");

    }

}