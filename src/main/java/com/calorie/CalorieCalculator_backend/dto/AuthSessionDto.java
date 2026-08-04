package com.calorie.CalorieCalculator_backend.dto;

import java.time.Instant;

public record AuthSessionDto(String token, Instant expiresAt) {
}
