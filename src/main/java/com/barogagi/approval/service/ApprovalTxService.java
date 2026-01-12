package com.barogagi.approval.service;

import com.barogagi.approval.domain.ApprovalNumInfo;
import com.barogagi.approval.repository.ApprovalNumInfoRepository;
import com.barogagi.approval.vo.ApprovalVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApprovalTxService {

    private final ApprovalNumInfoRepository approvalNumInfoRepository;


    @Transactional
    public boolean cancelApproval(String tel, String type, String completeYn) {
        ApprovalNumInfo entity = approvalNumInfoRepository.findTopByTelAndTypeAndCompleteYnOrderByRegDateDesc(tel, type, completeYn).orElse(null);

        if(null == entity) {
            return false;
        }

        entity.setCompleteYn("C");
        entity.setCancelDate(LocalDateTime.now());
        return true;
    }

    @Transactional
    public boolean insertApprovalRecord(ApprovalVO vo){
        try {
            ApprovalNumInfo entity = new ApprovalNumInfo();
            entity.setTel(vo.getTel());
            entity.setAuthCode(vo.getAuthCode());
            entity.setCompleteYn(vo.getCompleteYn());
            entity.setType(vo.getType());
            entity.setMessageContent(vo.getMessageContent());
            entity.setRegDate(LocalDateTime.now());

            approvalNumInfoRepository.save(entity);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public boolean updateApprovalComplete(String tel, String type, String completeYn, String authCode){

        ApprovalNumInfo entity = approvalNumInfoRepository.findTopByTelAndTypeAndCompleteYnAndAuthCodeOrderByRegDateDesc(
                tel, type, completeYn, authCode).orElse(null);

        if(null == entity) {
            return false;
        }

        entity.setCompleteYn("Y");
        entity.setCompleteDate(LocalDateTime.now());
        return true;
    }
}
