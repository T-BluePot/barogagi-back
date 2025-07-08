package com.barogagi.plan.command.repository;

import com.barogagi.plan.command.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("PlanCommandRepository")
public interface PlanRepository extends JpaRepository<Plan, Integer> {
}
