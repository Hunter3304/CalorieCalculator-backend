package com.calorie.CalorieCalculator_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BodyWeightTrendPointDto(
        LocalDate date,
        BigDecimal weightKg,
        LocalDate sourceDate,
        boolean recorded) {
}