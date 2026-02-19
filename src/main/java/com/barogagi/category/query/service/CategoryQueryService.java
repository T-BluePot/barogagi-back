package com.barogagi.category.query.service;

import com.barogagi.category.dto.CategoryResDto;
import com.barogagi.category.query.vo.CategoryVO;
import com.barogagi.config.exception.BusinessException;
import com.barogagi.category.query.mapper.CategoryMapper;
import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.dto.ScheduleListGroupResDTO;
import com.barogagi.schedule.query.service.ScheduleQueryService;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryQueryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryQueryService.class);

    private final MembershipUtil membershipUtil;
    private final CategoryMapper categoryMapper;
    private final Validator validator;

    @Autowired
    public CategoryQueryService (MembershipUtil membershipUtil,
                                 CategoryMapper categoryMapper,
                                 Validator validator) {
        this.membershipUtil = membershipUtil;
        this.categoryMapper = categoryMapper;
        this.validator = validator;

    }

    public ApiResponse getCategoryList(HttpServletRequest request) {
        try {
            // 1. API SECRET KEY 검증
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                return ApiResponse.error(ErrorCode.NOT_EQUAL_API_SECRET_KEY.getCode(), ErrorCode.NOT_EQUAL_API_SECRET_KEY.getMessage());
            }

            List<CategoryVO> result = categoryMapper.selectCategoryList();

            List<CategoryResDto> categoryResDtoList = result.stream()
                    .map(vo -> CategoryResDto.builder()
                            .categoryNum(vo.getCategoryNum())
                            .categoryNm(vo.getCategoryNm())
                            .build())
                    .collect(Collectors.toList());

            return ApiResponse.success(categoryResDtoList, "카테고리 목록 조회 성공");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.NOT_FOUND_CATEGORY.getCode(), ErrorCode.NOT_FOUND_CATEGORY.getMessage());
        }
    }
}
