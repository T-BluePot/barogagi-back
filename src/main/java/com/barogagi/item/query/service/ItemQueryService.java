package com.barogagi.item.query.service;

import com.barogagi.category.dto.CategoryResDto;
import com.barogagi.category.query.vo.CategoryVO;
import com.barogagi.config.exception.BusinessException;
import com.barogagi.item.dto.ItemResDto;
import com.barogagi.item.query.mapper.ItemMapper;
import com.barogagi.item.query.vo.ItemVO;
import com.barogagi.response.ApiResponse;
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
public class ItemQueryService {
    private static final Logger logger = LoggerFactory.getLogger(ItemQueryService.class);

    private final ItemMapper itemMapper;
    private final Validator validator;

    @Autowired
    public ItemQueryService(MembershipUtil membershipUtil,
                            ItemMapper itemMapper,
                            Validator validator) {
        this.itemMapper = itemMapper;
        this.validator = validator;

    }

    public ApiResponse getItemList(Integer categoryNum, HttpServletRequest request) {
        try {
            // 1. API SECRET KEY 검증
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                return ApiResponse.error(ErrorCode.NOT_EQUAL_API_SECRET_KEY.getCode(), ErrorCode.NOT_EQUAL_API_SECRET_KEY.getMessage());
            }

            List<ItemVO> result = itemMapper.selectItemList(categoryNum);

            List<ItemResDto> itemResDtoList = result.stream()
                    .map(vo -> ItemResDto.builder()
                            .itemNum(vo.getItemNum())
                            .itemNm(vo.getItemNm())
                            .build())
                    .collect(Collectors.toList());

            return ApiResponse.success(itemResDtoList, "아이템(세부 카테고리) 목록 조회 성공");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.NOT_FOUND_ITEM.getCode(), ErrorCode.NOT_FOUND_ITEM.getMessage());
        }
    }
}
