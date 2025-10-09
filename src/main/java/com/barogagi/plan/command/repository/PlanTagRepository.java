package com.barogagi.plan.command.repository;

import com.barogagi.tag.command.entity.PlanTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanTagRepository extends JpaRepository<PlanTag, Integer> {
}
