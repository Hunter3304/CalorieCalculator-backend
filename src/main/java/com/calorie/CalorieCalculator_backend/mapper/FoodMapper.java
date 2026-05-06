package com.calorie.CalorieCalculator_backend.mapper;

import com.calorie.CalorieCalculator_backend.entity.FoodItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

//myBatis Mapper接口
@Mapper
public interface FoodMapper {

    //alle fooddaten abfragen
    @Select("SELECT * FROM food_items ORDER BY id ASC")
    List<FoodItem> getAllFoods();

    @Select("SELECT * FROM food_items WHERE id = #{id}")
    FoodItem getFoodById(Long id);


    //插入食物items
    @Insert("INSERT INTO food_items(name_zh, name_en, protein_per_100g, carbs_per_100g, fat_per_100g) " +
            "VALUES(#{nameZh}, #{nameEn}, #{proteinPer100g}, #{carbsPer100g}, #{fatPer100g})")
    void insertFood(FoodItem foodItem);

    // 查询某一页的食物（核心修改：先按最后使用时间降序，再按 ID 降序）
    @Select("SELECT * FROM food_items ORDER BY last_used_time DESC, id DESC LIMIT #{size} OFFSET #{offset}")
    List<FoodItem> getFoodsByPage(@Param("offset") int offset, @Param("size") int size);

    // 查询数据库里总共有多少条食物（用来计算总页数）
    @Select("SELECT COUNT(*) FROM food_items")
    int getTotalFoodsCount();

    // 模糊搜索食物名字
    @Select("SELECT * FROM food_items WHERE name_zh LIKE CONCAT('%', #{keyword}, '%') ORDER BY id DESC LIMIT 50")
    List<FoodItem> searchFoodsByName(@Param("keyword") String keyword);

    // 1. 获取所有自定义食物
    @Select("SELECT * FROM food_items WHERE is_custom = 1 ORDER BY id DESC")
    List<FoodItem> getCustomFoods();

    // 2. 修改之前的插入语句，强制加上 is_custom = 1
    @Insert("INSERT INTO food_items(name_zh, name_en, protein_per_100g, carbs_per_100g, fat_per_100g, is_custom) " +
            "VALUES(#{nameZh}, #{nameEn}, #{proteinPer100g}, #{carbsPer100g}, #{fatPer100g}, 1)")
    void insertCustomFood(FoodItem foodItem);

    // 删除自定义食物
    @Delete("DELETE FROM food_items WHERE id = #{id} AND is_custom = 1")
    void deleteCustomFood(Integer id);

    // 更新自定义食物（如果用户填错了克数，可以修改）
    @Update("UPDATE food_items SET name_zh=#{nameZh}, protein_per_100g=#{proteinPer100g}, " +
            "carbs_per_100g=#{carbsPer100g}, fat_per_100g=#{fatPer100g} " +
            "WHERE id=#{id} AND is_custom = 1")
    void updateCustomFood(FoodItem foodItem);
}
