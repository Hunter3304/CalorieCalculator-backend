package com.calorie.CalorieCalculator_backend.dto;

import java.time.LocalDate;
import java.util.List;

public record BodyWeightTrendDto(
        LocalDate startDate,
        LocalDate endDate,
        LocalDate firstRecordDate,
        List<BodyWeightTrendPointDto> points) {
}