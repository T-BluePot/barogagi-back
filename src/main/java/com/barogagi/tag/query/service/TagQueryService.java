package com.barogagi.tag.query.service;

import com.barogagi.schedule.command.service.ScheduleCommandService;
import com.barogagi.schedule.dto.ScheduleRegistReqDTO;
import com.barogagi.tag.dto.TagSearchReqDTO;
import com.barogagi.tag.dto.TagSearchResDTO;
import com.barogagi.tag.query.mapper.TagMapper;
import com.barogagi.tag.query.vo.TagDetailVO;
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

    @Autowired
    public TagQueryService (TagMapper tagMapper) {
        this.tagMapper = tagMapper;
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

    public List<TagSearchResDTO> searchList(TagSearchReqDTO tagSearchReqDTO) {
        return tagMapper.selectTagByTagTypeAndCategoryNum(tagSearchReqDTO);
    }
}