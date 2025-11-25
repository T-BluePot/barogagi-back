package com.barogagi.plan.query.service;

import com.barogagi.plan.query.mapper.PlanMapper;
import com.barogagi.plan.query.vo.PlanDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanQueryService {
    private static final Logger logger = LoggerFactory.getLogger(PlanQueryService.class);

    private final PlanMapper planMapper;
    @Autowired
    public PlanQueryService (PlanMapper planMapper) {
        this.planMapper = planMapper;
    }

    public List<PlanDetailVO> getPlanDetail(int scheduleNum) throws Exception{
        return planMapper.selectPlanDetailByScheduleNum(scheduleNum);
    }
}
