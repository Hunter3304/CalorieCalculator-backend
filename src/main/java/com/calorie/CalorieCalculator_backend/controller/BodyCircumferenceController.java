package com.calorie.CalorieCalculator_backend.controller;

import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceRequest;
import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceSnapshotDto;
import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceTrendDto;
import com.calorie.CalorieCalculator_backend.service.BodyCircumferenceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/circumferences")
@CrossOrigin(origins = "*")
public class BodyCircumferenceController {
    private final BodyCircumferenceService service;

    public BodyCircumferenceController(BodyCircumferenceService service) {
        this.service = service;
    }

    @GetMapping("/{date}")
    public ResponseEntity<BodyCircumferenceSnapshotDto> getSnapshot(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.getSnapshot(date));
    }

    @PutMapping("/{date}")
    public ResponseEntity<BodyCircumferenceSnapshotDto> save(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody BodyCircumferenceRequest request) {
        return ResponseEntity.ok(service.save(date, request));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trend")
    public ResponseEntity<BodyCircumferenceTrendDto> getTrend(
            @RequestParam String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.getTrend(type, startDate, endDate));
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
