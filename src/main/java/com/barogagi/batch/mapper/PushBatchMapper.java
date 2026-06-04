package com.barogagi.batch.mapper;

import com.barogagi.batch.dto.ScheduleDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PushBatchMapper {

    List<ScheduleDTO> selectSchedulePushTarget();
}
