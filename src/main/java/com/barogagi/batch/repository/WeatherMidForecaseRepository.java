package com.barogagi.batch.repository;

import com.barogagi.batch.entity.WeatherMidForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherMidForecaseRepository extends JpaRepository<WeatherMidForecast, Long> {
}
