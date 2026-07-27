package com.calorie.CalorieCalculator_backend.dto;

import java.time.LocalDate;

public record BodyCircumferenceSnapshotDto(
        LocalDate selectedDate,
        Integer recordId,
        BodyCircumferenceValueDto chest,
        BodyCircumferenceValueDto waist,
        BodyCircumferenceValueDto hip,
        BodyCircumferenceValueDto arm,
        BodyCircumferenceValueDto thigh,
        BodyCircumferenceValueDto calf) {
}
