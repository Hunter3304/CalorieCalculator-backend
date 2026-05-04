package com.calorie.CalorieCalculator_backend.service;


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
}
