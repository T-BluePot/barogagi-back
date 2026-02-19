package com.barogagi.category.controller;

import com.barogagi.plan.query.service.PlaceQueryService;
import com.barogagi.response.ApiResponse;
import com.barogagi.category.query.service.CategoryQueryService;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "카테고리", description = "카테고리 관련 API")
@RestController
@RequestMapping("/api/v1/category")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "apiKeyAuth")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final String API_SECRET_KEY;
    private final CategoryQueryService categoryQueryService;

    @Autowired
    public CategoryController(Environment environment,
                              CategoryQueryService categoryQueryService) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
        this.categoryQueryService = categoryQueryService;
    }

    @Operation(summary = "카테고리 목록 조회 기능",
            description = "카테고리 목록을 조회하는 기능입니다")
    @GetMapping("/")
    public ApiResponse getCategory(HttpServletRequest request) {
        logger.info("CALL /category");
        return categoryQueryService.getCategoryList(request);
    }
}
