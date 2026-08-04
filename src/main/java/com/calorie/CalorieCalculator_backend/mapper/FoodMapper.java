package com.calorie.CalorieCalculator_backend.mapper;

import com.calorie.CalorieCalculator_backend.entity.FoodItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FoodMapper {
    String ACCESSIBLE = "(f.is_custom = 0 OR f.owner_user_id = #{userId})";

    @Select("SELECT f.* FROM food_items f WHERE " + ACCESSIBLE + " ORDER BY f.id")
    List<FoodItem> getAllFoods(@Param("userId") Long userId);

    @Select("SELECT f.* FROM food_items f WHERE f.id = #{id} AND " + ACCESSIBLE)
    FoodItem getFoodById(@Param("id") Long id, @Param("userId") Long userId);

    @Insert("INSERT INTO food_items(name_zh, name_en, protein_per_100g, carbs_per_100g, fat_per_100g, is_custom) " +
            "VALUES(#{nameZh}, #{nameEn}, #{proteinPer100g}, #{carbsPer100g}, #{fatPer100g}, 0)")
    void insertFood(FoodItem foodItem);

    @Select("SELECT f.* FROM food_items f " +
            "LEFT JOIN user_food_usage u ON u.food_id = f.id AND u.owner_user_id = #{userId} " +
            "WHERE " + ACCESSIBLE + " " +
            "ORDER BY u.last_used_at DESC NULLS LAST, f.id DESC LIMIT #{size} OFFSET #{offset}")
    List<FoodItem> getFoodsByPage(@Param("userId") Long userId,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    @Select("SELECT COUNT(*) FROM food_items f WHERE " + ACCESSIBLE)
    int getTotalFoodsCount(@Param("userId") Long userId);

    @Select("SELECT f.* FROM food_items f " +
            "LEFT JOIN user_food_usage u ON u.food_id = f.id AND u.owner_user_id = #{userId} " +
            "WHERE " + ACCESSIBLE + " AND f.name_zh LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY u.last_used_at DESC NULLS LAST, f.id DESC LIMIT 50")
    List<FoodItem> searchFoodsByName(@Param("userId") Long userId,
                                     @Param("keyword") String keyword);

    @Select("SELECT f.* FROM food_items f " +
            "WHERE f.is_custom = 1 AND f.owner_user_id = #{userId} ORDER BY f.id DESC")
    List<FoodItem> getCustomFoods(@Param("userId") Long userId);

    @Insert("INSERT INTO food_items(owner_user_id, name_zh, name_en, protein_per_100g, " +
            "carbs_per_100g, fat_per_100g, is_custom) VALUES(#{userId}, #{food.nameZh}, " +
            "#{food.nameEn}, #{food.proteinPer100g}, #{food.carbsPer100g}, #{food.fatPer100g}, 1)")
    void insertCustomFood(@Param("userId") Long userId, @Param("food") FoodItem foodItem);

    @Delete("DELETE FROM food_items WHERE id = #{id} AND is_custom = 1 " +
            "AND owner_user_id = #{userId}")
    int deleteCustomFood(@Param("userId") Long userId, @Param("id") Integer id);

    @Update("UPDATE food_items SET name_zh = #{food.nameZh}, " +
            "protein_per_100g = #{food.proteinPer100g}, carbs_per_100g = #{food.carbsPer100g}, " +
            "fat_per_100g = #{food.fatPer100g} WHERE id = #{food.id} AND is_custom = 1 " +
            "AND owner_user_id = #{userId}")
    int updateCustomFood(@Param("userId") Long userId, @Param("food") FoodItem foodItem);

    @Insert("INSERT INTO user_food_usage(owner_user_id, food_id, last_used_at) " +
            "VALUES(#{userId}, #{foodId}, CURRENT_TIMESTAMP) " +
            "ON CONFLICT (owner_user_id, food_id) DO UPDATE SET last_used_at = CURRENT_TIMESTAMP")
    void recordFoodUsage(@Param("userId") Long userId, @Param("foodId") Integer foodId);

    @Delete("DELETE FROM food_items WHERE owner_user_id = #{userId} AND is_custom = 1")
    int deleteCustomFoodsByUser(@Param("userId") Long userId);
}
