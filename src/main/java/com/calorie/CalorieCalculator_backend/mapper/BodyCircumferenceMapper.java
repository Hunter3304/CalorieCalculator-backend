package com.calorie.CalorieCalculator_backend.mapper;

import com.calorie.CalorieCalculator_backend.entity.BodyCircumferenceRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BodyCircumferenceMapper {
    String COLUMNS = "id, record_date AS recordDate, chest_cm AS chestCm, waist_cm AS waistCm, " +
            "hip_cm AS hipCm, arm_cm AS armCm, thigh_cm AS thighCm, calf_cm AS calfCm";

    @Select("SELECT " + COLUMNS + " FROM body_circumference_records WHERE id = #{id}")
    BodyCircumferenceRecord findById(@Param("id") Integer id);

    @Select("SELECT " + COLUMNS + " FROM body_circumference_records WHERE record_date = #{date}")
    BodyCircumferenceRecord findByDate(@Param("date") LocalDate date);

    @Select("SELECT " + COLUMNS + " FROM body_circumference_records " +
            "WHERE record_date <= #{date} AND ${column} IS NOT NULL " +
            "ORDER BY record_date DESC LIMIT 1")
    BodyCircumferenceRecord findLatestForColumn(
            @Param("date") LocalDate date,
            @Param("column") String column);

    @Select("SELECT MIN(record_date) FROM body_circumference_records WHERE ${column} IS NOT NULL")
    LocalDate findFirstDateForColumn(@Param("column") String column);

    @Select("SELECT " + COLUMNS + " FROM body_circumference_records " +
            "WHERE record_date BETWEEN #{startDate} AND #{endDate} AND ${column} IS NOT NULL " +
            "ORDER BY record_date")
    List<BodyCircumferenceRecord> findBetweenForColumn(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("column") String column);

    @Insert("INSERT INTO body_circumference_records(" +
            "record_date, chest_cm, waist_cm, hip_cm, arm_cm, thigh_cm, calf_cm) VALUES(" +
            "#{recordDate}, #{chestCm}, #{waistCm}, #{hipCm}, #{armCm}, #{thighCm}, #{calfCm})")
    void insert(BodyCircumferenceRecord record);

    @Update("UPDATE body_circumference_records SET chest_cm = #{chestCm}, waist_cm = #{waistCm}, " +
            "hip_cm = #{hipCm}, arm_cm = #{armCm}, thigh_cm = #{thighCm}, calf_cm = #{calfCm}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(BodyCircumferenceRecord record);

    @Delete("DELETE FROM body_circumference_records WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);
}
