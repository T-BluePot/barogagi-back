package com.barogagi.tag.query.mapper;

import com.barogagi.tag.query.vo.TagDetailVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagMapper {

    // 계획 상세 조회 - 태그 상세 조회
    List<TagDetailVO> selectTagByPlanNum (int planNum);

    // 일정 생성 - 태그 번호로 태그명 조회
    TagDetailVO selectTagByTagNum (int tagNum);
}
