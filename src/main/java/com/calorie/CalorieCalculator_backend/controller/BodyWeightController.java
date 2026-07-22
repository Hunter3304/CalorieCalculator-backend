package com.calorie.CalorieCalculator_backend.controller;

import com.calorie.CalorieCalculator_backend.dto.BodyWeightRequest;
import com.calorie.CalorieCalculator_backend.dto.BodyWeightSnapshotDto;
import com.calorie.CalorieCalculator_backend.dto.BodyWeightTrendDto;
import com.calorie.CalorieCalculator_backend.service.BodyWeightService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/weights")
@CrossOrigin(origins = "*")
public class BodyWeightController {
    private final BodyWeightService bodyWeightService;

    public BodyWeightController(BodyWeightService bodyWeightService) {
        this.bodyWeightService = bodyWeightService;
    }

    @GetMapping("/{date}")
    public ResponseEntity<BodyWeightSnapshotDto> getSnapshot(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bodyWeightService.getSnapshot(date));
    }

    @PutMapping("/{date}")
    public ResponseEntity<BodyWeightSnapshotDto> save(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody BodyWeightRequest request) {
        return ResponseEntity.ok(bodyWeightService.save(date, request.weightKg()));
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<BodyWeightSnapshotDto> update(
            @PathVariable Integer id,
            @RequestBody BodyWeightRequest request) {
        return ResponseEntity.ok(bodyWeightService.update(id, request.weightKg()));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        bodyWeightService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trend")
    public ResponseEntity<BodyWeightTrendDto> getTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(bodyWeightService.getTrend(startDate, endDate));
    }

    @ExceptionHandler({IllegalArgumentException.class, java.time.format.DateTimeParseException.class})
    public ResponseEntity<String> handleInvalidRequest(RuntimeException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleMissingRecord(NoSuchElementException exception) {
        return ResponseEntity.status(404).body(exception.getMessage());
    }
}