package com.calorie.CalorieCalculator_backend.mapper;

import com.calorie.CalorieCalculator_backend.entity.FoodItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

//myBatis Mapper接口
@Mapper
public interface FoodMapper {

    //alle fooddaten abfragen
    @Select("SELECT * FROM food_items ORDER BY id ASC")
    List<FoodItem> getAllFoods();
}
