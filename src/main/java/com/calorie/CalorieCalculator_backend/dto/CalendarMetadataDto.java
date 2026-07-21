package com.calorie.CalorieCalculator_backend.dto;

import java.time.LocalDate;
import java.util.List;

public class CalendarMetadataDto {
    private LocalDate minDate;
    private LocalDate maxDate;
    private List<LocalDate> recordedDates;

    public CalendarMetadataDto(LocalDate minDate, LocalDate maxDate, List<LocalDate> recordedDates) {
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.recordedDates = recordedDates;
    }

    public LocalDate getMinDate() { return minDate; }
    public void setMinDate(LocalDate minDate) { this.minDate = minDate; }
    public LocalDate getMaxDate() { return maxDate; }
    public void setMaxDate(LocalDate maxDate) { this.maxDate = maxDate; }
    public List<LocalDate> getRecordedDates() { return recordedDates; }
    public void setRecordedDates(List<LocalDate> recordedDates) { this.recordedDates = recordedDates; }
}
