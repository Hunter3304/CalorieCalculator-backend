package com.calorie.CalorieCalculator_backend.dto;

import java.time.LocalDate;
import java.util.List;

public record BodyCircumferenceTrendDto(
        String measurementType,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate firstRecordDate,
        List<BodyCircumferenceTrendPointDto> points) {
}
