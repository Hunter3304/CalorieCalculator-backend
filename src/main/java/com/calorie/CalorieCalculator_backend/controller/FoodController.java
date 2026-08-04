package com.calorie.CalorieCalculator_backend.controller;

import com.calorie.CalorieCalculator_backend.auth.CurrentUserAttributes;
import com.calorie.CalorieCalculator_backend.dto.NutritionResult;
import com.calorie.CalorieCalculator_backend.entity.FoodItem;
import com.calorie.CalorieCalculator_backend.service.FoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/foods")
public class FoodController {
    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public List<FoodItem> getFoods(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId) {
        return foodService.getAllFoodItems(userId);
    }

    @GetMapping("/{id}/calculate")
    public NutritionResult calculate(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable Long id,
            @RequestParam Double weight) {
        return foodService.calculateNutrition(userId, id, weight);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FoodItem>> searchFoods(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @RequestParam String keyword) {
        return ResponseEntity.ok(foodService.search(userId, keyword));
    }

    @PostMapping("/custom")
    public ResponseEntity<Void> addCustomFood(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @RequestBody FoodItem foodItem) {
        foodService.addCustomFood(userId, foodItem);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/custom")
    public ResponseEntity<List<FoodItem>> getCustomFoods(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId) {
        return ResponseEntity.ok(foodService.getCustomFoods(userId));
    }

    @DeleteMapping("/custom/{id}")
    public ResponseEntity<Void> deleteCustomFood(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable Integer id) {
        foodService.deleteCustomFood(userId, id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/custom/{id}")
    public ResponseEntity<Void> updateCustomFood(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable Long id,
            @RequestBody FoodItem foodItem) {
        foodService.updateCustomFood(userId, id, foodItem);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getFoodsByPage(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(foodService.getPage(userId, page, size));
    }
}
