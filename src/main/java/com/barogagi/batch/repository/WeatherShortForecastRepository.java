package com.barogagi.batch.repository;

import com.barogagi.batch.entity.WeatherShortForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherShortForecastRepository extends JpaRepository<WeatherShortForecast, Long> {
}
