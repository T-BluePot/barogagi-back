package com.barogagi.schedule.query.mapper;

import com.barogagi.schedule.query.vo.ScheduleDetailVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper {
    ScheduleDetailVO selectScheduleDetail(int scheduleNum);
}
