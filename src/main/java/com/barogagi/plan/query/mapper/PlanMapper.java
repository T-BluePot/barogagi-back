package com.barogagi.plan.query.mapper;

import com.barogagi.plan.query.vo.PlanDetailVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PlanMapper {

    // 계획 상세 조회
    List<PlanDetailVO> selectPlanDetailByScheduleNum(int scheduleNum);

}
