package com.calorie.CalorieCalculator_backend.controller;

import com.calorie.CalorieCalculator_backend.auth.CurrentUserAttributes;
import com.calorie.CalorieCalculator_backend.dto.CalendarMetadataDto;
import com.calorie.CalorieCalculator_backend.dto.DailySummaryDto;
import com.calorie.CalorieCalculator_backend.service.DailyRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class DailyRecordController {
    private final DailyRecordService recordService;

    public DailyRecordController(DailyRecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    public ResponseEntity<String> addRecord(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @RequestBody Map<String, Object> payload) {
        LocalDate date = LocalDate.parse(payload.get("date").toString());
        Integer foodId = Integer.valueOf(payload.get("foodId").toString());
        Double weight = Double.valueOf(payload.get("weight").toString());
        recordService.addRecord(userId, date, foodId, weight);
        return ResponseEntity.ok("success");
    }

    @GetMapping("/calendar")
    public ResponseEntity<CalendarMetadataDto> getCalendarMetadata(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @RequestParam String month) {
        return ResponseEntity.ok(recordService.getCalendarMetadata(userId, YearMonth.parse(month)));
    }

    @GetMapping("/{date}")
    public ResponseEntity<DailySummaryDto> getDailySummary(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(recordService.getDailySummary(userId, date));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable Integer id) {
        recordService.deleteRecord(userId, id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateRecord(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable Integer id,
            @RequestBody Map<String, Double> payload) {
        recordService.updateRecordWeight(userId, id, payload.get("weight"));
        return ResponseEntity.ok().build();
    }
}
