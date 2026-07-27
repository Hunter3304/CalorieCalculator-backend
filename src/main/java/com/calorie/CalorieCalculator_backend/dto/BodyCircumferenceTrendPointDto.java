package com.calorie.CalorieCalculator_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BodyCircumferenceTrendPointDto(
        LocalDate date,
        BigDecimal valueCm,
        LocalDate sourceDate,
        boolean recorded) {
}
