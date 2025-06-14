package com.barogagi.approval.service;

import com.barogagi.approval.mapper.ApprovalMapper;
import com.barogagi.approval.vo.ApprovalVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApprovalService {

    private final ApprovalMapper approvalMapper;

    @Autowired
    public ApprovalService(ApprovalMapper approvalMapper){
        this.approvalMapper = approvalMapper;
    }

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
