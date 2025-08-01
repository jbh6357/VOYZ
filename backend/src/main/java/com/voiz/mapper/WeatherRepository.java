package com.voiz.mapper;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.voiz.vo.Weather;





@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {

    
    @Query(value = "SELECT * FROM VOYZ_WEATHER w WHERE w.DONG_NAME = :dongName AND w.FORECAST_DATE BETWEEN :from AND :to",
           nativeQuery = true)
    List<Weather> findWeatherByDongNameAndDateRange(
        @Param("dongName") String dongName,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    
}
