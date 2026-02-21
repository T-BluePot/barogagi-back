package com.barogagi.tag.command.repository;

import com.barogagi.schedule.command.entity.Schedule;
import com.barogagi.tag.command.entity.ScheduleTag;
import com.barogagi.tag.command.entity.ScheduleTagId;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleTagRepository extends JpaRepository<ScheduleTag, ScheduleTagId> {

    @Modifying
    @Query("DELETE FROM ScheduleTag st WHERE st.schedule = :schedule")
    void deleteBySchedule(@Param("schedule") Schedule schedule);
}