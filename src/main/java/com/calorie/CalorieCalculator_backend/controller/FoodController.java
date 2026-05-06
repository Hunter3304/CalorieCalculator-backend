package com.calorie.CalorieCalculator_backend.controller;

import java.util.HashMap;
import com.calorie.CalorieCalculator_backend.dto.NutritionResult;
import com.calorie.CalorieCalculator_backend.entity.FoodItem;
import com.calorie.CalorieCalculator_backend.mapper.FoodMapper;
import com.calorie.CalorieCalculator_backend.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600) // 允许所有来源访问，方便调试
@RestController
@RequestMapping("/api/foods")
public class FoodController {
    @Autowired
    private FoodService foodService;

    private final FoodMapper foodMapper;

    public FoodController(FoodMapper foodMapper) {
        this.foodMapper = foodMapper;
    }

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

    // 搜索食物接口
    @GetMapping("/search")
    public ResponseEntity<List<FoodItem>> searchFoods(@RequestParam("keyword") String keyword) {
        List<FoodItem> list = foodMapper.searchFoodsByName(keyword);
        return ResponseEntity.ok(list);
    }

    //处理用户自定义添加的食物
    @PostMapping("/custom")
    public ResponseEntity<String> addCustomFood(@RequestBody FoodItem foodItem) {
        if (foodItem.getNameEn() == null) {
            foodItem.setNameEn("");
        }
        foodMapper.insertCustomFood(foodItem);
        return ResponseEntity.ok("自定义食物添加成功");
    }

    // 获取自定义食物列表
    @GetMapping("/custom")
    public ResponseEntity<List<FoodItem>> getCustomFoods() {
        return ResponseEntity.ok(foodMapper.getCustomFoods());
    }

    // 删除自定义食物
    @DeleteMapping("/custom/{id}")
    public ResponseEntity<String> deleteCustomFood(@PathVariable Integer id) {
        foodMapper.deleteCustomFood(id);
        return ResponseEntity.ok("删除成功");
    }

    // 修改自定义食物
    @PutMapping("/custom/{id}")
    public ResponseEntity<String> updateCustomFood(@PathVariable Long id, @RequestBody FoodItem foodItem) {
        foodItem.setId(id);
        foodMapper.updateCustomFood(foodItem);
        return ResponseEntity.ok("修改成功");
    }




    //翻页接口
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getFoodsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int offset = (page - 1) * size;
        List<FoodItem> list = foodMapper.getFoodsByPage(offset, size);
        int total = foodMapper.getTotalFoodsCount();

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / size);

        Map<String, Object> response = new HashMap<>();
        response.put("list", list);
        response.put("total", total);
        response.put("page", page);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }
}
