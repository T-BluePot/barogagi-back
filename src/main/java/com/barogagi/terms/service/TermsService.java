package com.barogagi.terms.service;

import com.barogagi.terms.mapper.TermsMapper;
import com.barogagi.terms.vo.TermsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

@Service
public class TermsService {

    private TermsMapper termsMapper;

    @Autowired
    public TermsService(TermsMapper termsMapper) {
        this.termsMapper = termsMapper;
    }

    // 사용중인 약관 목록 조회
    public List<TermsVO> selectTermsList(TermsVO vo) throws Exception {
        return termsMapper.selectTermsList(vo);
    }

    // 약관 동의 여부 저장
    public int insertTermsAgreeInfo(TermsVO vo) throws Exception {
        return termsMapper.insertTermsAgreeInfo(vo);
    }

    @Transactional
    public String insertTermsAgreeList(List<TermsVO> termsList) {
        String resultCode = "";
        try {
            for(TermsVO vo : termsList) {
                int insertFlag = this.insertTermsAgreeInfo(vo);
                if(insertFlag > 0){
                    resultCode = "200";
                } else {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                }
            }
        } catch (Exception e) {
            resultCode = "400";
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new RuntimeException(e);
        }

        return resultCode;
    }
}
