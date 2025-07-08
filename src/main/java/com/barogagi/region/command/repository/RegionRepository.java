package com.barogagi.region.command.repository;

import com.barogagi.plan.command.entity.Plan;
import com.barogagi.region.command.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("RegionCommandRepository")
public interface RegionRepository extends JpaRepository<Region, Integer> {
}