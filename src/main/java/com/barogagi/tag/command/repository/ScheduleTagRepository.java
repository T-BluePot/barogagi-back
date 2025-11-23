package com.barogagi.tag.command.repository;

import com.barogagi.schedule.command.entity.Schedule;
import com.barogagi.tag.command.entity.ScheduleTag;
import com.barogagi.tag.command.entity.ScheduleTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleTagRepository extends JpaRepository<ScheduleTag, ScheduleTagId> {

    void deleteBySchedule(Schedule schedule);

}