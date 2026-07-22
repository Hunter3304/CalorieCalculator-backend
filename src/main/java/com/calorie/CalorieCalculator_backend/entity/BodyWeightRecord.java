package com.calorie.CalorieCalculator_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BodyWeightRecord {
    private Integer id;
    private LocalDate recordDate;
    private BigDecimal weightKg;

    public BodyWeightRecord() {
    }

    public BodyWeightRecord(Integer id, LocalDate recordDate, BigDecimal weightKg) {
        this.id = id;
        this.recordDate = recordDate;
        this.weightKg = weightKg;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
}