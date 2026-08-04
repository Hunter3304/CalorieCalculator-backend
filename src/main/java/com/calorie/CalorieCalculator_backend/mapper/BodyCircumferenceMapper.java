package com.calorie.CalorieCalculator_backend.mapper;

import com.calorie.CalorieCalculator_backend.entity.BodyCircumferenceRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BodyCircumferenceMapper {
    String COLUMNS = "id, record_date AS recordDate, chest_cm AS chestCm, waist_cm AS waistCm, " +
            "hip_cm AS hipCm, arm_cm AS armCm, thigh_cm AS thighCm, calf_cm AS calfCm";

    @Select("SELECT " + COLUMNS + " FROM body_circumference_records " +
            "WHERE id = #{id} AND owner_user_id = #{userId}")
    BodyCircumferenceRecord findById(@Param("userId") Long userId, @Param("id") Integer id);

    @Select("SELECT " + COLUMNS + " FROM body_circumference_records " +
            "WHERE record_date = #{date} AND owner_user_id = #{userId}")
    BodyCircumferenceRecord findByDate(@Param("userId") Long userId,
                                       @Param("date") LocalDate date);

    @Select("SELECT " + COLUMNS + " FROM body_circumference_records " +
            "WHERE owner_user_id = #{userId} AND record_date <= #{date} " +
            "AND ${column} IS NOT NULL ORDER BY record_date DESC LIMIT 1")
    BodyCircumferenceRecord findLatestForColumn(@Param("userId") Long userId,
                                                @Param("date") LocalDate date,
                                                @Param("column") String column);

    @Select("SELECT MIN(record_date) FROM body_circumference_records " +
            "WHERE owner_user_id = #{userId} AND ${column} IS NOT NULL")
    LocalDate findFirstDateForColumn(@Param("userId") Long userId,
                                     @Param("column") String column);

    @Select("SELECT " + COLUMNS + " FROM body_circumference_records " +
            "WHERE owner_user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "AND ${column} IS NOT NULL ORDER BY record_date")
    List<BodyCircumferenceRecord> findBetweenForColumn(@Param("userId") Long userId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate,
                                                       @Param("column") String column);

    @Insert("INSERT INTO body_circumference_records(owner_user_id, record_date, chest_cm, " +
            "waist_cm, hip_cm, arm_cm, thigh_cm, calf_cm) VALUES(#{userId}, #{record.recordDate}, " +
            "#{record.chestCm}, #{record.waistCm}, #{record.hipCm}, #{record.armCm}, " +
            "#{record.thighCm}, #{record.calfCm})")
    void insert(@Param("userId") Long userId,
                @Param("record") BodyCircumferenceRecord record);

    @Update("UPDATE body_circumference_records SET chest_cm = #{record.chestCm}, " +
            "waist_cm = #{record.waistCm}, hip_cm = #{record.hipCm}, arm_cm = #{record.armCm}, " +
            "thigh_cm = #{record.thighCm}, calf_cm = #{record.calfCm}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{record.id} AND owner_user_id = #{userId}")
    int update(@Param("userId") Long userId,
               @Param("record") BodyCircumferenceRecord record);

    @Delete("DELETE FROM body_circumference_records " +
            "WHERE id = #{id} AND owner_user_id = #{userId}")
    int deleteById(@Param("userId") Long userId, @Param("id") Integer id);

    @Delete("DELETE FROM body_circumference_records WHERE owner_user_id = #{userId}")
    int deleteByUser(@Param("userId") Long userId);
}
