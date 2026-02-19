package com.barogagi.item.query.mapper;

import com.barogagi.item.query.vo.ItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItemMapper {

    // 세부 카테고리(아이템)명 조회
    String selectItemNmBy(int itemNum);

    List<ItemVO> selectItemList(int categoryNum);
}
