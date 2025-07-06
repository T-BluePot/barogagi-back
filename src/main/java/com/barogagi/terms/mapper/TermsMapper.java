package com.barogagi.terms.mapper;

import com.barogagi.terms.vo.TermsInputDTO;
import com.barogagi.terms.vo.TermsDTO;
import com.barogagi.terms.vo.TermsOutputDTO;
import com.barogagi.terms.vo.TermsProcessDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TermsMapper {
    // 사용중인 약관 목록 조회
    List<TermsOutputDTO> selectTermsList(TermsInputDTO termsInputDTO);

    int insertTermsAgreeInfo(TermsProcessDTO vo);
}
