package com.barogagi.schedule.command.repository;

import com.barogagi.schedule.command.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("ScheduleCommandRepository")
public interface ScheduleRepository extends JpaRepository<Schedule, Integer>{
}