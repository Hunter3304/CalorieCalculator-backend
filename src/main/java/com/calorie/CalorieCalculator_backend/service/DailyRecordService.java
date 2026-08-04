package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.dto.CalendarMetadataDto;
import com.calorie.CalorieCalculator_backend.dto.DailyRecordDetailDto;
import com.calorie.CalorieCalculator_backend.dto.DailySummaryDto;
import com.calorie.CalorieCalculator_backend.mapper.DailyRecordMapper;
import com.calorie.CalorieCalculator_backend.mapper.FoodMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DailyRecordService {
    private final DailyRecordMapper dailyRecordMapper;
    private final FoodMapper foodMapper;

    public DailyRecordService(DailyRecordMapper dailyRecordMapper, FoodMapper foodMapper) {
        this.dailyRecordMapper = dailyRecordMapper;
        this.foodMapper = foodMapper;
    }

    public void addRecord(Long userId, LocalDate date, Integer foodId, Double weight) {
        validateRecordDate(userId, date, LocalDate.now());
        if (foodId == null || foodMapper.getFoodById(foodId.longValue(), userId) == null) {
            throw new NoSuchElementException("Food not found");
        }
        if (weight == null || weight <= 0) {
            throw new IllegalArgumentException("Weight must be greater than zero");
        }
        dailyRecordMapper.insertRecord(userId, date, foodId, weight);
        foodMapper.recordFoodUsage(userId, foodId);
    }

    public CalendarMetadataDto getCalendarMetadata(Long userId, YearMonth month) {
        return getCalendarMetadata(userId, month, LocalDate.now());
    }

    CalendarMetadataDto getCalendarMetadata(Long userId, YearMonth month, LocalDate today) {
        LocalDate minDate = getMinDate(userId, today);
        LocalDate maxDate = today.plusDays(7);
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        LocalDate queryStart = monthStart.isBefore(minDate) ? minDate : monthStart;
        LocalDate queryEnd = monthEnd.isAfter(maxDate) ? maxDate : monthEnd;
        List<LocalDate> recordedDates = queryStart.isAfter(queryEnd)
                ? List.of()
                : dailyRecordMapper.findRecordedDatesBetween(userId, queryStart, queryEnd);
        return new CalendarMetadataDto(minDate, maxDate, recordedDates);
    }

    void validateRecordDate(Long userId, LocalDate date, LocalDate today) {
        if (date == null) {
            throw new IllegalArgumentException("Record date is required");
        }
        LocalDate minDate = getMinDate(userId, today);
        LocalDate maxDate = today.plusDays(7);
        if (date.isBefore(minDate) || date.isAfter(maxDate)) {
            throw new IllegalArgumentException(
                    "Record date must be between " + minDate + " and " + maxDate);
        }
    }

    private LocalDate getMinDate(Long userId, LocalDate today) {
        LocalDate oneYearAgo = today.minusYears(1);
        LocalDate earliestRecordDate = dailyRecordMapper.findEarliestRecordDate(userId);
        if (earliestRecordDate == null || earliestRecordDate.isAfter(today)) {
            return today;
        }
        return earliestRecordDate.isBefore(oneYearAgo) ? oneYearAgo : earliestRecordDate;
    }

    public void deleteRecord(Long userId, Integer id) {
        if (id == null || dailyRecordMapper.deleteRecord(userId, id) == 0) {
            throw new NoSuchElementException("Daily record not found");
        }
    }

    public void updateRecordWeight(Long userId, Integer id, Double weight) {
        if (weight == null || weight <= 0) {
            throw new IllegalArgumentException("Weight must be greater than zero");
        }
        if (id == null || dailyRecordMapper.updateRecordWeight(userId, id, weight) == 0) {
            throw new NoSuchElementException("Daily record not found");
        }
    }

    public DailySummaryDto getDailySummary(Long userId, LocalDate date) {
        List<DailyRecordDetailDto> records = dailyRecordMapper.findRecordsByDate(userId, date);
        DailySummaryDto summaryDto = new DailySummaryDto();
        summaryDto.setDate(date);
        summaryDto.setList(records);

        double totalWeight = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;
        for (DailyRecordDetailDto record : records) {
            double ratio = record.getWeight() / 100.0;
            totalCarbs += record.getCarbsPer100g() * ratio;
            totalFat += record.getFatPer100g() * ratio;
            totalProtein += record.getProteinPer100g() * ratio;
            totalWeight += record.getWeight();
        }
        double totalCalories = totalFat * 9 + totalCarbs * 4 + totalProtein * 4;
        summaryDto.setTotalWeight(roundOne(totalWeight));
        summaryDto.setTotalProteinMass(roundOne(totalProtein));
        summaryDto.setTotalCarbsMass(roundOne(totalCarbs));
        summaryDto.setTotalFatMass(roundOne(totalFat));
        summaryDto.setTotalCalories(roundOne(totalCalories));
        summaryDto.setProteinCalories(roundOne(totalProtein * 4));
        summaryDto.setCarbsCalories(roundOne(totalCarbs * 4));
        summaryDto.setFatCalories(roundOne(totalFat * 9));
        return summaryDto;
    }

    private double roundOne(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
