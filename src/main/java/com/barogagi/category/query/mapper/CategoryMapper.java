package com.barogagi.category.query.mapper;

import com.barogagi.category.query.vo.CategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    // 카테고리명 조회
    String selectCategoryNmBy(int categoryNum);

    int selectRandomCategoryNum();

    List<CategoryVO> selectCategoryList();
}
