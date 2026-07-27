package com.calorie.CalorieCalculator_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BodyCircumferenceValueDto(
        BigDecimal valueCm,
        LocalDate sourceDate,
        boolean recordedOnSelectedDate) {
}
