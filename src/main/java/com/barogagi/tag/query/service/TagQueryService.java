package com.barogagi.tag.query.service;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.command.service.ScheduleCommandService;
import com.barogagi.schedule.dto.ScheduleRegistReqDTO;
import com.barogagi.tag.dto.TagSearchReqDTO;
import com.barogagi.tag.dto.TagSearchResDTO;
import com.barogagi.tag.enums.TagType;
import com.barogagi.tag.exception.TagException;
import com.barogagi.tag.query.mapper.TagMapper;
import com.barogagi.tag.query.vo.TagDetailVO;
import com.barogagi.terms.exception.TermsException;
import com.barogagi.util.InputValidate;
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
    private final InputValidate inputValidate;


    @Autowired
    public TagQueryService (TagMapper tagMapper, Validator validator, InputValidate inputValidate) {
        this.tagMapper = tagMapper;
        this.validator = validator;
        this.inputValidate = inputValidate;
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

    /**
     * 태그 목록을 검색하는 기능입니다.
     * - 여행 스타일 태그(S): categoryNum을 null로 전달하세요.
     * - 상세 일정 태그(P): 해당 일정의 카테고리 번호(categoryNum)를 전달하세요.
     * 검색 결과는 최대 10개의 태그를 반환합니다.
     *
     * @throws TagException NOT_EQUAL_API_SECRET_KEY - API 시크릿 키가 일치하지 않을 경우
     * @throws TagException EMPTY_DATA - tagType이 비어있을 경우
     * @throws TagException INVALID_TAG_TYPE - tagType이 유효한 Enum 값이 아니거나, tagType과 categoryNum 조합이 올바르지 않을 경우
     *                                         (S인데 categoryNum이 존재하거나, P인데 categoryNum이 없을 경우)
     * @throws TagException NOT_FOUND_TAG - 조회된 태그 목록이 없을 경우
     */
    public ApiResponse searchList(TagSearchReqDTO tagSearchReqDTO, HttpServletRequest request) {
        try {
            // 1. API SECRET KEY 일치 여부 확인
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                throw new TagException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
            }

            // 2. 필수 입력값(tagType) 확인
            if (inputValidate.isEmpty(String.valueOf(tagSearchReqDTO.getTagType()))) {
                throw new TagException(ErrorCode.EMPTY_DATA);
            }

            TagType tagType = TagType.fromValue(String.valueOf(tagSearchReqDTO.getTagType()));

            // tagType이 S이면 categoryNum은 null이어야 함
            if (TagType.S == tagType && tagSearchReqDTO.getCategoryNum() != null) {
                throw new TagException(ErrorCode.INVALID_TAG_TYPE);
            }

            // tagType이 P이면 categoryNum 필수
            if (TagType.P == tagType && tagSearchReqDTO.getCategoryNum() == null) {
                throw new TagException(ErrorCode.EMPTY_DATA);
            }

            List<TagSearchResDTO> tagList = tagMapper.selectTagByTagTypeAndCategoryNum(tagSearchReqDTO);

            if (tagList == null || tagList.isEmpty()) {
                return ApiResponse.error(ErrorCode.NOT_FOUND_TAG.getCode(), ErrorCode.NOT_FOUND_TAG.getMessage());
            }

            return ApiResponse.resultData(
                    tagList,
                    ErrorCode.SUCCESS_FOUND_TAG.getCode(),
                    ErrorCode.SUCCESS_FOUND_TAG.getMessage()
            );

        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }
}