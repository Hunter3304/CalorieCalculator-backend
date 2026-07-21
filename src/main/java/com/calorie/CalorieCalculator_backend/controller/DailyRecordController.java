package com.calorie.CalorieCalculator_backend.controller;

import com.calorie.CalorieCalculator_backend.dto.DailySummaryDto;
import com.calorie.CalorieCalculator_backend.dto.CalendarMetadataDto;
import com.calorie.CalorieCalculator_backend.service.DailyRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@CrossOrigin(origins = "*")
public class DailyRecordController {

    private final DailyRecordService recordService;

    public DailyRecordController(DailyRecordService recordService) {
        this.recordService = recordService;
    }

    //导入食物记录
    @PostMapping
    public ResponseEntity<String> addRecord(@RequestBody Map<String, Object> payload) {
        LocalDate date = LocalDate.parse(payload.get("date").toString());
        Integer foodId = (Integer) payload.get("foodId");
        Double weight = Double.valueOf(payload.get("weight").toString());

        recordService.addRecord(date, foodId, weight);
        return ResponseEntity.ok("添加成功");
    }

    @GetMapping("/calendar")
    public ResponseEntity<CalendarMetadataDto> getCalendarMetadata(@RequestParam String month) {
        return ResponseEntity.ok(recordService.getCalendarMetadata(YearMonth.parse(month)));
    }

    // 获取某一天的汇总数据
    @GetMapping("/{date}")
    public ResponseEntity<DailySummaryDto> getDailySummary(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailySummaryDto summary = recordService.getDailySummary(date);
        return ResponseEntity.ok(summary);
    }

    // 删除 根据id
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRecord(@PathVariable Integer id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok("success");
    }

    //根据id 修改重量
    @PutMapping("/{id}")
    public ResponseEntity<String> updateRecord(@PathVariable Integer id, @RequestBody Map<String, Double> payload) {
        Double weight = payload.get("weight");
        if (weight == null || weight <= 0) {
            return ResponseEntity.badRequest().body("Invalid weight");
        }
        recordService.updateRecordWeight(id, weight);
        return ResponseEntity.ok("success");
    }

    @ExceptionHandler({IllegalArgumentException.class, java.time.format.DateTimeParseException.class})
    public ResponseEntity<String> handleInvalidRequest(RuntimeException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
