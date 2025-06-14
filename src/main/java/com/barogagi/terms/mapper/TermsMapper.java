package com.barogagi.terms.mapper;

import com.barogagi.terms.vo.TermsVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TermsMapper {
    // 사용중인 약관 목록 조회
    List<TermsVO> selectTermsList(TermsVO vo);

    int insertTermsAgreeInfo(TermsVO vo);
}
