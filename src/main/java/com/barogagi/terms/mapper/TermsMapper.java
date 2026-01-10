package com.barogagi.terms.mapper;

import com.barogagi.terms.dto.TermsAgreeDTO;
import com.barogagi.terms.dto.TermsInputDTO;
import com.barogagi.terms.dto.TermsOutputDTO;
import com.barogagi.terms.dto.TermsProcessDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TermsMapper {
    // 사용중인 약관 목록 조회
    List<TermsOutputDTO> selectTermsList(TermsInputDTO termsInputDTO);

    int insertTermsAgreeInfo(TermsAgreeDTO vo);
}
