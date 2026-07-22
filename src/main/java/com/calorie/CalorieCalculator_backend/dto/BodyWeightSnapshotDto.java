package com.calorie.CalorieCalculator_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BodyWeightSnapshotDto(
        LocalDate selectedDate,
        Integer recordId,
        LocalDate sourceDate,
        BigDecimal weightKg,
        boolean recordedOnSelectedDate,
        LocalDate firstRecordDate) {
}