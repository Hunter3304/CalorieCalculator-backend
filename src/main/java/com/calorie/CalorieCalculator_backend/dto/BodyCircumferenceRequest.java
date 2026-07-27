package com.calorie.CalorieCalculator_backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record BodyCircumferenceRequest(
        BigDecimal chestCm,
        BigDecimal waistCm,
        BigDecimal hipCm,
        BigDecimal armCm,
        BigDecimal thighCm,
        BigDecimal calfCm,
        List<String> clearFields) {
}
