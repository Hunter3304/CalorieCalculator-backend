package com.calorie.CalorieCalculator_backend.controller;


import com.calorie.CalorieCalculator_backend.dto.NutritionResult;
import com.calorie.CalorieCalculator_backend.entity.FoodItem;
import com.calorie.CalorieCalculator_backend.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}/calculate")
    public NutritionResult calculate(
            @PathVariable("id") Long id,
            @RequestParam("weight") Double weight) {
        return foodService.calculateNutrition(id, weight);
    }
}
