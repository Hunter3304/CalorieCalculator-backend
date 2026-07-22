package com.calorie.CalorieCalculator_backend.mapper;

import com.calorie.CalorieCalculator_backend.entity.BodyWeightRecord;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BodyWeightMapper {
    @Insert("INSERT INTO body_weight_records(record_date, weight_kg) " +
            "VALUES(#{recordDate}, #{weightKg}) " +
            "ON CONFLICT (record_date) DO UPDATE SET " +
            "weight_kg = EXCLUDED.weight_kg, updated_at = CURRENT_TIMESTAMP")
    void upsert(@Param("recordDate") LocalDate recordDate, @Param("weightKg") BigDecimal weightKg);

    @Select("SELECT id, record_date AS recordDate, weight_kg AS weightKg " +
            "FROM body_weight_records WHERE id = #{id}")
    BodyWeightRecord findById(@Param("id") Integer id);

    @Select("SELECT id, record_date AS recordDate, weight_kg AS weightKg " +
            "FROM body_weight_records WHERE record_date <= #{date} " +
            "ORDER BY record_date DESC LIMIT 1")
    BodyWeightRecord findLatestOnOrBefore(@Param("date") LocalDate date);

    @Select("SELECT MIN(record_date) FROM body_weight_records")
    LocalDate findFirstRecordDate();

    @Select("SELECT id, record_date AS recordDate, weight_kg AS weightKg " +
            "FROM body_weight_records WHERE record_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY record_date")
    List<BodyWeightRecord> findBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Update("UPDATE body_weight_records SET weight_kg = #{weightKg}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateWeight(@Param("id") Integer id, @Param("weightKg") BigDecimal weightKg);

    @Delete("DELETE FROM body_weight_records WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);
}