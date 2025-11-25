package com.barogagi.schedule.command.repository;

import com.barogagi.schedule.command.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer>{
    Optional<Schedule> findByScheduleNumAndMembershipNo(Integer scheduleNum, Integer membershipNo);
}