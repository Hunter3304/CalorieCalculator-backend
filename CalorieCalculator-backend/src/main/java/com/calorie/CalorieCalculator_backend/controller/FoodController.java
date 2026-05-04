package com.calorie.CalorieCalculator_backend.controller;


import com.calorie.CalorieCalculator_backend.entity.FoodItem;
import com.calorie.CalorieCalculator_backend.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {
    @Autowired
    private FoodService foodService;

    @GetMapping
    public List<FoodItem> getFoods(){
        return foodService.getAllFoodItems();
    }
}
