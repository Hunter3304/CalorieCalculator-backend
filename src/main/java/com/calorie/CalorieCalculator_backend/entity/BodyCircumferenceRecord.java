package com.calorie.CalorieCalculator_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BodyCircumferenceRecord {
    private Integer id;
    private LocalDate recordDate;
    private BigDecimal chestCm;
    private BigDecimal waistCm;
    private BigDecimal hipCm;
    private BigDecimal armCm;
    private BigDecimal thighCm;
    private BigDecimal calfCm;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public BigDecimal getChestCm() { return chestCm; }
    public void setChestCm(BigDecimal chestCm) { this.chestCm = chestCm; }
    public BigDecimal getWaistCm() { return waistCm; }
    public void setWaistCm(BigDecimal waistCm) { this.waistCm = waistCm; }
    public BigDecimal getHipCm() { return hipCm; }
    public void setHipCm(BigDecimal hipCm) { this.hipCm = hipCm; }
    public BigDecimal getArmCm() { return armCm; }
    public void setArmCm(BigDecimal armCm) { this.armCm = armCm; }
    public BigDecimal getThighCm() { return thighCm; }
    public void setThighCm(BigDecimal thighCm) { this.thighCm = thighCm; }
    public BigDecimal getCalfCm() { return calfCm; }
    public void setCalfCm(BigDecimal calfCm) { this.calfCm = calfCm; }
}
