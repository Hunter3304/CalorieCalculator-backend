package com.calorie.CalorieCalculator_backend.controller;

import com.calorie.CalorieCalculator_backend.entity.FoodItem;
import com.calorie.CalorieCalculator_backend.mapper.FoodMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final FoodMapper foodMapper;

    // 【关键】：我们自己 new 出来，不让 Spring 找了
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 【关键】：构造函数里 *只有* foodMapper，绝对不能有 objectMapper
    public ImportController(FoodMapper foodMapper) {
        this.foodMapper = foodMapper;
    }

    @GetMapping("/json")
    public String importJsonData() {
        int totalCount = 0;
        int fileCount = 0;

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            // 读取你放进去的 75 个 JSON 文件
            Resource[] resources = resolver.getResources("classpath:data/foods/*.json");

            for (Resource resource : resources) {
                fileCount++;
                try (InputStream inputStream = resource.getInputStream()) {
                    JsonNode rootNode = objectMapper.readTree(inputStream);

                    if (rootNode.isArray()) {
                        for (JsonNode node : rootNode) {
                            FoodItem food = new FoodItem();

                            // 读取 JSON 字段
                            String nameZh = node.path("foodName").asText("未知食物");
                            double protein = parseDoubleSafe(node.path("protein").asText());
                            double carbs = parseDoubleSafe(node.path("CHO").asText());
                            double fat = parseDoubleSafe(node.path("fat").asText());

                            food.setNameZh(nameZh);
                            food.setNameEn("");
                            food.setProteinPer100g(protein);
                            food.setCarbsPer100g(carbs);
                            food.setFatPer100g(fat);

                            // 存入数据库
                            foodMapper.insertFood(food);
                            totalCount++;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("读取文件出错: " + resource.getFilename());
                    e.printStackTrace();
                }
            }

            return "🔥 完美！共扫描了 " + fileCount + " 个文件，成功清洗并导入了 " + totalCount + " 条食物数据！";

        } catch (Exception e) {
            e.printStackTrace();
            return "导入失败，请看控制台报错: " + e.getMessage();
        }
    }

    // 专门清洗《中国食物成分表》脏数据的工具方法
    private double parseDoubleSafe(String val) {
        if (val == null || val.trim().isEmpty() || val.equalsIgnoreCase("Tr") || val.equals("-") || val.contains("一")) {
            return 0.0;
        }
        try {
            String cleanVal = val.trim().split("\\s+")[0];
            return Double.parseDouble(cleanVal);
        } catch (Exception e) {
            return 0.0;
        }
    }
}