package com.calorie.CalorieCalculator_backend.mapper;

import com.calorie.CalorieCalculator_backend.dto.DailyRecordDetailDto;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyRecordMapper {
    @Insert("INSERT INTO daily_records(owner_user_id, record_date, food_id, weight) " +
            "VALUES(#{userId}, #{recordDate}, #{foodId}, #{weight})")
    void insertRecord(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate,
                      @Param("foodId") Integer foodId, @Param("weight") Double weight);

    @Delete("DELETE FROM daily_records WHERE id = #{id} AND owner_user_id = #{userId}")
    int deleteRecord(@Param("userId") Long userId, @Param("id") Integer id);

    @Update("UPDATE daily_records SET weight = #{weight} " +
            "WHERE id = #{id} AND owner_user_id = #{userId}")
    int updateRecordWeight(@Param("userId") Long userId, @Param("id") Integer id,
                           @Param("weight") Double weight);

    @Select("SELECT r.id, r.record_date AS recordDate, r.food_id AS foodId, r.weight, " +
            "f.name_zh AS nameZh, f.protein_per_100g AS proteinPer100g, " +
            "f.carbs_per_100g AS carbsPer100g, f.fat_per_100g AS fatPer100g, " +
            "ROUND(CAST((COALESCE(f.protein_per_100g, 0) * 4 + " +
            "COALESCE(f.carbs_per_100g, 0) * 4 + COALESCE(f.fat_per_100g, 0) * 9) " +
            "* (r.weight / 100.0) AS numeric), 1) AS calories " +
            "FROM daily_records r JOIN food_items f ON r.food_id = f.id " +
            "WHERE r.owner_user_id = #{userId} AND r.record_date = #{date}")
    List<DailyRecordDetailDto> findRecordsByDate(@Param("userId") Long userId,
                                                  @Param("date") LocalDate date);

    @Select("SELECT MIN(record_date) FROM daily_records WHERE owner_user_id = #{userId}")
    LocalDate findEarliestRecordDate(@Param("userId") Long userId);

    @Select("SELECT DISTINCT record_date FROM daily_records " +
            "WHERE owner_user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY record_date")
    List<LocalDate> findRecordedDatesBetween(@Param("userId") Long userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Delete("DELETE FROM daily_records WHERE owner_user_id = #{userId}")
    int deleteByUser(@Param("userId") Long userId);
}
