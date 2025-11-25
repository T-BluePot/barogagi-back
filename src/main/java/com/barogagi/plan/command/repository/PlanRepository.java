package com.barogagi.plan.command.repository;

import com.barogagi.plan.command.entity.Plan;
import com.barogagi.schedule.command.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer> {

    List<Plan> findBySchedule(Schedule schedule);
}
