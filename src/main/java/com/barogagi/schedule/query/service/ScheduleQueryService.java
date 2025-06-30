package com.barogagi.schedule.query.service;

import com.barogagi.member.login.controller.LoginController;
import com.barogagi.plan.query.service.PlanQueryService;
import com.barogagi.plan.query.vo.PlanDetailVO;
import com.barogagi.schedule.dto.ScheduleDetailResDTO;
import com.barogagi.schedule.query.mapper.ScheduleMapper;
import com.barogagi.schedule.query.vo.ScheduleDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleQueryService {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final ScheduleMapper scheduleMapper;

    private final PlanQueryService planQueryService;


    @Autowired
    public ScheduleQueryService (ScheduleMapper scheduleMapper,
                                 PlanQueryService planQueryService) {
        this.scheduleMapper = scheduleMapper;
        this.planQueryService = planQueryService;
    }

    public ScheduleDetailResDTO getScheduleDetail(int scheduleNum) throws Exception{
        // 일정 정보 조회
        ScheduleDetailVO scheduleDetailVO = scheduleMapper.selectScheduleDetail(scheduleNum);

        // 계획 정보 조회 (리스트)
        List<PlanDetailVO> planDetailVOList = planQueryService.getPlanDetail(scheduleNum);

        // DTO에 정보 저장
        ScheduleDetailResDTO result = ScheduleDetailResDTO.builder()
                .scheduleNum(scheduleDetailVO.getScheduleNum())
                .scheduleNm(scheduleDetailVO.getScheduleNm())
                .startDate(scheduleDetailVO.getStartDate())
                .endDate(scheduleDetailVO.getEndDate())
                .radius(scheduleDetailVO.getRadius())
                .planDetailVOList(planDetailVOList)
                .build();

        logger.info("result={}", result.toString());
        return result;
    }
}
