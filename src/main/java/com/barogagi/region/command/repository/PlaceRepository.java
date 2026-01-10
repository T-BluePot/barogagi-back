package com.barogagi.region.command.repository;

import com.barogagi.region.command.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Integer> {
}
