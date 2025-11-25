package com.barogagi.plan.query.mapper;

import com.barogagi.plan.query.vo.PlanDetailVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    // 카테고리명 조회
    String selectCategoryNmBy(int categoryNum);
}
