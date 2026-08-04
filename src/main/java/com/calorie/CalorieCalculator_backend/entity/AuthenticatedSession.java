package com.calorie.CalorieCalculator_backend.entity;

public record AuthenticatedSession(Long userId, String tokenHash) {
}
