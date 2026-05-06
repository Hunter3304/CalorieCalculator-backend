package com.calorie.CalorieCalculator_backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;


@Data
public class DailySummaryDto {
    private LocalDate date;
    private List<DailyRecordDetailDto> list;

    //total gewicht
    private Double totalWeight;
    private Double totalProteinMass;
    private Double totalCarbsMass;
    private Double totalFatMass;

    //total calories
    private Double totalCalories;
    private Double proteinCalories;
    private Double carbsCalories;
    private Double fatCalories;

    //getter und setter

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<DailyRecordDetailDto> getList() {
        return list;
    }

    public void setList(List<DailyRecordDetailDto> list) {
        this.list = list;
    }

    public Double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(Double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public Double getTotalProteinMass() {
        return totalProteinMass;
    }

    public void setTotalProteinMass(Double totalProteinMass) {
        this.totalProteinMass = totalProteinMass;
    }

    public Double getTotalCarbsMass() {
        return totalCarbsMass;
    }

    public void setTotalCarbsMass(Double totalCarbsMass) {
        this.totalCarbsMass = totalCarbsMass;
    }

    public Double getTotalFatMass() {
        return totalFatMass;
    }

    public void setTotalFatMass(Double totalFatMass) {
        this.totalFatMass = totalFatMass;
    }

    public Double getProteinCalories() {
        return proteinCalories;
    }

    public void setProteinCalories(Double proteinCalories) {
        this.proteinCalories = proteinCalories;
    }

    public Double getTotalCalories() {
        return totalCalories;
    }


    public Double getCarbsCalories() {
        return carbsCalories;
    }

    public void setCarbsCalories(Double carbsCalories) {
        this.carbsCalories = carbsCalories;
    }

    public Double getFatCalories() {
        return fatCalories;
    }

    public void setFatCalories(Double fatCalories) {
        this.fatCalories = fatCalories;
    }

    public void setTotalCalories(Double totalCalories){
        this.totalCalories = totalCalories;
    }


}
