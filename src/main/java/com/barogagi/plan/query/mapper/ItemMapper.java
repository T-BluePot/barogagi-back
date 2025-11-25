package com.barogagi.plan.query.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemMapper {

    // 세부 카테고리(아이템)명 조회
    String selectItemNmBy(int itemNum);
}
