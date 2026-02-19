package com.barogagi.item.controller;

import com.barogagi.category.query.service.CategoryQueryService;
import com.barogagi.item.query.service.ItemQueryService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "아이템(세부 카테고리)", description = "아이템(세부 카테고리) 관련 API")
@RestController
@RequestMapping("/api/v1/item")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "apiKeyAuth")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    private final String API_SECRET_KEY;
    private final ItemQueryService itemQueryService;

    @Autowired
    public ItemController(Environment environment,
                          ItemQueryService itemQueryService) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
        this.itemQueryService = itemQueryService;
    }

    @Operation(summary = "아이템(세부 카테고리) 목록 조회 기능",
            description = "카테고리별 아이템(세부 카테고리) 목록을 조회하는 기능입니다. 카테고리 번호를 쿼리 파라미터로 전달해야 합니다.")
    @GetMapping("/")
    public ApiResponse getItem(HttpServletRequest request, @RequestParam int categoryNum) {
        logger.info("CALL /item");
        return itemQueryService.getItemList(categoryNum, request);
    }
}
