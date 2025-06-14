package com.barogagi.approval.mapper;

import com.barogagi.approval.vo.ApprovalVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApprovalMapper {

    int updateApprovalRecord(ApprovalVO vo);

    int insertApprovalRecord(ApprovalVO vo);

    int updateApprovalComplete(ApprovalVO vo);
}
