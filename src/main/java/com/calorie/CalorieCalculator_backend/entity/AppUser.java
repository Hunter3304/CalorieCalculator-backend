package com.calorie.CalorieCalculator_backend.entity;

import java.time.Instant;

public class AppUser {
    private Long id;
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
