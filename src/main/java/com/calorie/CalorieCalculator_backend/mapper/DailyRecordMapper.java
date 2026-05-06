package com.calorie.CalorieCalculator_backend.mapper;

import com.calorie.CalorieCalculator_backend.dto.DailyRecordDetailDto;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyRecordMapper {
    //插入数据
    @Insert("INSERT INTO daily_records(record_date, food_id, weight) VALUES(#{recordDate}, #{foodId}, #{weight})")
    void insertRecord(LocalDate recordDate, Integer foodId, Double weight);


    //删除记录
    @Delete("DELETE FROM daily_records WHERE id = #{id}")
    void deleteRecord(@Param("id") Integer id);

    //修改重量
    @Update("UPDATE daily_records SET weight = #{weight} WHERE id = #{id}")
    void updateRecordWeight(@Param("id") Integer id, @Param("weight") Double weight);

    @Select("SELECT r.id, " +
            "r.record_date as recordDate, " +
            "r.food_id as foodId, " +
            "r.weight, " +
            "f.name_zh as nameZh, " +
            "f.protein_per_100g as proteinPer100g, " +
            "f.carbs_per_100g as carbsPer100g, " +
            "f.fat_per_100g as fatPer100g, " +
            // 【注意】：这里算完单种食物卡路里 AS calories 之后，后面直接接 FROM，不要再重复写别的字段了
            "ROUND(CAST( " +
            "  (COALESCE(f.protein_per_100g, 0) * 4 + " +
            "   COALESCE(f.carbs_per_100g, 0) * 4 + " +
            "   COALESCE(f.fat_per_100g, 0) * 9) * (r.weight / 100.0) " +
            "AS numeric), 1) AS calories " +
            "FROM daily_records r " +
            "JOIN food_items f ON r.food_id = f.id " +
            "WHERE r.record_date = #{date}")
    List<DailyRecordDetailDto> findRecordsByDate(@Param("date") LocalDate date);

    // 刷新食物的最后使用时间，用于置顶
    @Update("UPDATE food_items SET last_used_time = CURRENT_TIMESTAMP WHERE id = #{foodId}")
    void bumpFoodLastUsedTime(@Param("foodId") Integer foodId);
}
