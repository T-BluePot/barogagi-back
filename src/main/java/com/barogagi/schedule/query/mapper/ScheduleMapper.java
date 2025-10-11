package com.barogagi.schedule.query.mapper;

import com.barogagi.schedule.dto.ScheduleListResDTO;
import com.barogagi.schedule.query.vo.ScheduleDetailVO;
import com.barogagi.schedule.query.vo.ScheduleListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScheduleMapper {
    ScheduleDetailVO selectScheduleDetail(int scheduleNum);

    List<ScheduleListVO> selectScheduleList(int membershipNo);
}
