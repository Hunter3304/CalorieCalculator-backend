package com.calorie.CalorieCalculator_backend.entity;

import java.util.Arrays;

public enum BodyCircumferenceType {
    CHEST("chest", "chest_cm"),
    WAIST("waist", "waist_cm"),
    HIP("hip", "hip_cm"),
    ARM("arm", "arm_cm"),
    THIGH("thigh", "thigh_cm"),
    CALF("calf", "calf_cm");

    private final String key;
    private final String columnName;

    BodyCircumferenceType(String key, String columnName) {
        this.key = key;
        this.columnName = columnName;
    }

    public String key() {
        return key;
    }

    public String columnName() {
        return columnName;
    }

    public static BodyCircumferenceType fromKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Circumference type is required");
        }
        return Arrays.stream(values())
                .filter(type -> type.key.equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported circumference type: " + key));
    }
}
