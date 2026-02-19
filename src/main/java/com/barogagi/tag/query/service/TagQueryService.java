package com.barogagi.tag.query.service;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.command.service.ScheduleCommandService;
import com.barogagi.schedule.dto.ScheduleRegistReqDTO;
import com.barogagi.tag.dto.TagSearchReqDTO;
import com.barogagi.tag.dto.TagSearchResDTO;
import com.barogagi.tag.query.mapper.TagMapper;
import com.barogagi.tag.query.vo.TagDetailVO;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import java.util.List;

@Service
public class TagQueryService {
    private static final Logger logger = LoggerFactory.getLogger(TagQueryService.class);

    private final TagMapper tagMapper;

    private final Validator validator;

    @Autowired
    public TagQueryService (TagMapper tagMapper, Validator validator) {
        this.tagMapper = tagMapper;
        this.validator = validator;
    }
    
    // 계획 번호로 연결된 태그 상세 리스트 조회
    public List<TagDetailVO> findTagByPlanNum(int planNum) {
        return tagMapper.selectTagByPlanNum(planNum);
    }

    // 태그 번호 리스트로 태그명 리스트 조회
    public List<String> findTagNmByTagNum(List<Integer> tagNums) {
        return tagNums.stream()
                .map(tagMapper::selectTagByTagNum)
                .map(TagDetailVO::getTagNm)
                .collect(Collectors.toList());
    }

    public ApiResponse searchList(TagSearchReqDTO tagSearchReqDTO, HttpServletRequest request) {
        try {
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                return ApiResponse.error(ErrorCode.NOT_EQUAL_API_SECRET_KEY.getCode(), ErrorCode.NOT_EQUAL_API_SECRET_KEY.getMessage());
            }

            List<TagSearchResDTO> tagList = tagMapper.selectTagByTagTypeAndCategoryNum(tagSearchReqDTO);
            return ApiResponse.success(tagList, "태그 조회 성공");

        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }
}