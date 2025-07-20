package com.barogagi.approval.service;

import com.barogagi.approval.mapper.ApprovalMapper;
import com.barogagi.approval.vo.ApprovalVO;
import com.barogagi.sendSms.service.SendSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalService.class);

    @Autowired
    private ApprovalMapper approvalMapper;

    @Autowired
    private SendSmsService sendSmsService;

    public int updateApprovalRecord(ApprovalVO vo){
        return approvalMapper.updateApprovalRecord(vo);
    }

    public int insertApprovalRecord(ApprovalVO vo){
        return approvalMapper.insertApprovalRecord(vo);
    }

    public int updateApprovalComplete(ApprovalVO vo){
        return approvalMapper.updateApprovalComplete(vo);
    }
}
