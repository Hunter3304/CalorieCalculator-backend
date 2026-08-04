package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.dto.NutritionResult;
import com.calorie.CalorieCalculator_backend.entity.FoodItem;
import com.calorie.CalorieCalculator_backend.mapper.FoodMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class FoodService {
    private final FoodMapper foodMapper;

    public FoodService(FoodMapper foodMapper) {
        this.foodMapper = foodMapper;
    }

    public List<FoodItem> getAllFoodItems(Long userId) {
        return foodMapper.getAllFoods(userId);
    }

    public NutritionResult calculateNutrition(Long userId, Long foodId, Double weight) {
        FoodItem food = foodMapper.getFoodById(foodId, userId);
        if (food == null) {
            throw new NoSuchElementException("Food not found");
        }
        if (weight == null || weight <= 0) {
            throw new IllegalArgumentException("Weight must be greater than zero");
        }
        double ratio = weight / 100;
        NutritionResult result = new NutritionResult();
        result.setWeight(weight);
        result.setCarbs(food.getCarbsPer100g() * ratio);
        result.setFat(food.getFatPer100g() * ratio);
        result.setProtein(food.getProteinPer100g() * ratio);
        result.setTotalCalories(Math.round(
                (result.getProtein() * 4 + result.getCarbs() * 4 + result.getFat() * 9) * 10.0
        ) / 10.0);
        return result;
    }

    public List<FoodItem> search(Long userId, String keyword) {
        return foodMapper.searchFoodsByName(userId, keyword == null ? "" : keyword);
    }

    public List<FoodItem> getCustomFoods(Long userId) {
        return foodMapper.getCustomFoods(userId);
    }

    public void addCustomFood(Long userId, FoodItem foodItem) {
        requireFood(foodItem);
        if (foodItem.getNameEn() == null) {
            foodItem.setNameEn("");
        }
        foodMapper.insertCustomFood(userId, foodItem);
    }

    public void updateCustomFood(Long userId, Long id, FoodItem foodItem) {
        requireFood(foodItem);
        foodItem.setId(id);
        if (foodMapper.updateCustomFood(userId, foodItem) == 0) {
            throw new NoSuchElementException("Custom food not found");
        }
    }

    public void deleteCustomFood(Long userId, Integer id) {
        if (id == null || foodMapper.deleteCustomFood(userId, id) == 0) {
            throw new NoSuchElementException("Custom food not found");
        }
    }

    public Map<String, Object> getPage(Long userId, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page or size");
        }
        int offset = (page - 1) * size;
        int total = foodMapper.getTotalFoodsCount(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("list", foodMapper.getFoodsByPage(userId, offset, size));
        response.put("total", total);
        response.put("page", page);
        response.put("totalPages", (int) Math.ceil((double) total / size));
        return response;
    }

    private void requireFood(FoodItem foodItem) {
        if (foodItem == null || foodItem.getNameZh() == null || foodItem.getNameZh().isBlank()) {
            throw new IllegalArgumentException("Food name is required");
        }
        if (foodItem.getProteinPer100g() == null || foodItem.getCarbsPer100g() == null
                || foodItem.getFatPer100g() == null) {
            throw new IllegalArgumentException("Nutrition values are required");
        }
        if (foodItem.getProteinPer100g() < 0 || foodItem.getCarbsPer100g() < 0
                || foodItem.getFatPer100g() < 0) {
            throw new IllegalArgumentException("Nutrition values cannot be negative");
        }
    }
}
