package com.calorie.CalorieCalculator_backend.controller;

import com.calorie.CalorieCalculator_backend.auth.CurrentUserAttributes;
import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceRequest;
import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceSnapshotDto;
import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceTrendDto;
import com.calorie.CalorieCalculator_backend.service.BodyCircumferenceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/circumferences")
public class BodyCircumferenceController {
    private final BodyCircumferenceService service;

    public BodyCircumferenceController(BodyCircumferenceService service) {
        this.service = service;
    }

    @GetMapping("/{date}")
    public ResponseEntity<BodyCircumferenceSnapshotDto> getSnapshot(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.getSnapshot(userId, date));
    }

    @PutMapping("/{date}")
    public ResponseEntity<BodyCircumferenceSnapshotDto> save(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody BodyCircumferenceRequest request) {
        return ResponseEntity.ok(service.save(userId, date, request));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> delete(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @PathVariable Integer id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trend")
    public ResponseEntity<BodyCircumferenceTrendDto> getTrend(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @RequestParam String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.getTrend(userId, type, startDate, endDate));
    }
}
