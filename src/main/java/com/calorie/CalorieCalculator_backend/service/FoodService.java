package com.calorie.CalorieCalculator_backend.service;


import com.calorie.CalorieCalculator_backend.dto.NutritionResult;
import com.calorie.CalorieCalculator_backend.entity.FoodItem;
import com.calorie.CalorieCalculator_backend.mapper.FoodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodService {

    @Autowired
    private FoodMapper foodMapper;

    public List<FoodItem> getAllFoodItems(){
        return foodMapper.getAllFoods();
    }

    //计算食物各个属性的值
    public NutritionResult calculateNutrition(Long foodId, Double weight){
        FoodItem food = foodMapper.getFoodById(foodId);
        if(food == null || weight <= 0){
            return null;
        }

        NutritionResult result = new NutritionResult();
        double ratio = weight/100;
        result.setWeight(weight);
        result.setCarbs(food.getCarbsPer100g() * ratio);
        result.setFat(food.getFatPer100g() * ratio);
        result.setProtein(food.getProteinPer100g() * ratio);

        double totalCalories = (result.getProtein() * 4) + (result.getCarbs() * 4) + (result.getFat() * 9);
        result.setTotalCalories(Math.round(totalCalories * 10.0) / 10.0);//保留一位小数

        return result;
    }
}
