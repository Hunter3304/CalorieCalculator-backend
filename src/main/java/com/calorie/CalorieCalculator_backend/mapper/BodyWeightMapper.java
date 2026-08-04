package com.calorie.CalorieCalculator_backend.mapper;

import com.calorie.CalorieCalculator_backend.entity.BodyWeightRecord;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BodyWeightMapper {
    @Insert("INSERT INTO body_weight_records(owner_user_id, record_date, weight_kg) " +
            "VALUES(#{userId}, #{recordDate}, #{weightKg}) " +
            "ON CONFLICT (owner_user_id, record_date) DO UPDATE SET " +
            "weight_kg = EXCLUDED.weight_kg, updated_at = CURRENT_TIMESTAMP")
    void upsert(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate,
                @Param("weightKg") BigDecimal weightKg);

    @Select("SELECT id, record_date AS recordDate, weight_kg AS weightKg " +
            "FROM body_weight_records WHERE id = #{id} AND owner_user_id = #{userId}")
    BodyWeightRecord findById(@Param("userId") Long userId, @Param("id") Integer id);

    @Select("SELECT id, record_date AS recordDate, weight_kg AS weightKg " +
            "FROM body_weight_records WHERE owner_user_id = #{userId} AND record_date <= #{date} " +
            "ORDER BY record_date DESC LIMIT 1")
    BodyWeightRecord findLatestOnOrBefore(@Param("userId") Long userId,
                                          @Param("date") LocalDate date);

    @Select("SELECT MIN(record_date) FROM body_weight_records WHERE owner_user_id = #{userId}")
    LocalDate findFirstRecordDate(@Param("userId") Long userId);

    @Select("SELECT id, record_date AS recordDate, weight_kg AS weightKg " +
            "FROM body_weight_records WHERE owner_user_id = #{userId} " +
            "AND record_date BETWEEN #{startDate} AND #{endDate} ORDER BY record_date")
    List<BodyWeightRecord> findBetween(@Param("userId") Long userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Update("UPDATE body_weight_records SET weight_kg = #{weightKg}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND owner_user_id = #{userId}")
    int updateWeight(@Param("userId") Long userId, @Param("id") Integer id,
                     @Param("weightKg") BigDecimal weightKg);

    @Delete("DELETE FROM body_weight_records WHERE id = #{id} AND owner_user_id = #{userId}")
    int deleteById(@Param("userId") Long userId, @Param("id") Integer id);

    @Delete("DELETE FROM body_weight_records WHERE owner_user_id = #{userId}")
    int deleteByUser(@Param("userId") Long userId);
}
