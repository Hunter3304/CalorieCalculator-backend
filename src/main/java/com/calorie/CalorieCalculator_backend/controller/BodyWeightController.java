package com.calorie.CalorieCalculator_backend.controller;

import com.calorie.CalorieCalculator_backend.auth.CurrentUserAttributes;
import com.calorie.CalorieCalculator_backend.dto.BodyWeightRequest;
import com.calorie.CalorieCalculator_backend.dto.BodyWeightSnapshotDto;
import com.calorie.CalorieCalculator_backend.dto.BodyWeightTrendDto;
import com.calorie.CalorieCalculator_backend.service.BodyWeightService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/weights")
public class BodyWeightController {
    private final BodyWeightService bodyWeightService;

    public BodyWeightController(BodyWeightService bodyWeightService) {
        this.bodyWeightService = bodyWeightService;
    }

    @GetMapping("/{date}")
    public ResponseEntity<BodyWeightSnapshotDto> getSnapshot(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bodyWeightService.getSnapshot(userId, date));
    }

    @PutMapping("/{date}")
    public ResponseEntity<BodyWeightSnapshotDto> save(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody BodyWeightRequest request) {
        return ResponseEntity.ok(bodyWeightService.save(userId, date, request.weightKg()));
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<BodyWeightSnapshotDto> update(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable Integer id,
            @RequestBody BodyWeightRequest request) {
        return ResponseEntity.ok(bodyWeightService.update(userId, id, request.weightKg()));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> delete(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable Integer id) {
        bodyWeightService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trend")
    public ResponseEntity<BodyWeightTrendDto> getTrend(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(bodyWeightService.getTrend(userId, startDate, endDate));
    }
}
