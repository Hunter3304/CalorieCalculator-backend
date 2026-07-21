package com.calorie.CalorieCalculator_backend.service;


import com.calorie.CalorieCalculator_backend.dto.DailyRecordDetailDto;
import com.calorie.CalorieCalculator_backend.dto.DailySummaryDto;
import com.calorie.CalorieCalculator_backend.dto.CalendarMetadataDto;
import com.calorie.CalorieCalculator_backend.mapper.DailyRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;



@Service
public class DailyRecordService {

    private final DailyRecordMapper dailyRecordMapper;

    public DailyRecordService(DailyRecordMapper dailyRecordMapper) {
        this.dailyRecordMapper = dailyRecordMapper;
    }

    //调用mapper层接口 给表增加记录
    public void addRecord(LocalDate date, Integer foodId, Double weight) {
        validateRecordDate(date, LocalDate.now());
        dailyRecordMapper.insertRecord(date, foodId, weight);
        dailyRecordMapper.bumpFoodLastUsedTime(foodId);
    }

    public CalendarMetadataDto getCalendarMetadata(YearMonth month) {
        return getCalendarMetadata(month, LocalDate.now());
    }

    CalendarMetadataDto getCalendarMetadata(YearMonth month, LocalDate today) {
        LocalDate minDate = getMinDate(today);
        LocalDate maxDate = today.plusDays(7);
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        LocalDate queryStart = monthStart.isBefore(minDate) ? minDate : monthStart;
        LocalDate queryEnd = monthEnd.isAfter(maxDate) ? maxDate : monthEnd;
        List<LocalDate> recordedDates = queryStart.isAfter(queryEnd)
                ? List.of()
                : dailyRecordMapper.findRecordedDatesBetween(queryStart, queryEnd);
        return new CalendarMetadataDto(minDate, maxDate, recordedDates);
    }

    void validateRecordDate(LocalDate date, LocalDate today) {
        if (date == null) {
            throw new IllegalArgumentException("Record date is required");
        }
        LocalDate minDate = getMinDate(today);
        LocalDate maxDate = today.plusDays(7);
        if (date.isBefore(minDate) || date.isAfter(maxDate)) {
            throw new IllegalArgumentException(
                    "Record date must be between " + minDate + " and " + maxDate);
        }
    }

    private LocalDate getMinDate(LocalDate today) {
        LocalDate oneYearAgo = today.minusYears(1);
        LocalDate earliestRecordDate = dailyRecordMapper.findEarliestRecordDate();
        if (earliestRecordDate == null) {
            return today;
        }
        if (earliestRecordDate.isAfter(today)) {
            return today;
        }
        return earliestRecordDate.isBefore(oneYearAgo) ? oneYearAgo : earliestRecordDate;
    }

    // 删除记录
    public void deleteRecord(Integer id) {
        dailyRecordMapper.deleteRecord(id);
    }

    // 改记录
    public void updateRecordWeight(Integer id, Double weight) {
        dailyRecordMapper.updateRecordWeight(id, weight);
    }


    //给当天记录表 赋值
    public DailySummaryDto getDailySummary(LocalDate date) {
        List<DailyRecordDetailDto> records = dailyRecordMapper.findRecordsByDate(date);

        DailySummaryDto summaryDto = new DailySummaryDto();
        summaryDto.setDate(date);
        summaryDto.setList(records);

        double totalWeight = 0, totalCal = 0, totalPro = 0, totalCarbs = 0, totalFat = 0;
        for(DailyRecordDetailDto record : records){
            double ratio = record.getWeight()/100.0;
            totalCarbs += record.getCarbsPer100g() * ratio;
            totalFat += record.getFatPer100g() * ratio;
            totalPro += record.getProteinPer100g() * ratio;
           // totalCal += record.getCaloriesPer100g() * ratio;
            totalWeight += record.getWeight();
        }
        totalCal = (totalFat * 9) + (totalCarbs * 4) + (totalPro * 4);


        summaryDto.setTotalWeight(Math.round(totalWeight * 10.0) / 10.0);
        summaryDto.setTotalProteinMass(Math.round(totalPro * 10.0) / 10.0);
        summaryDto.setTotalCarbsMass(Math.round(totalCarbs * 10.0) / 10.0);
        summaryDto.setTotalFatMass(Math.round(totalFat * 10.0) / 10.0);
        summaryDto.setTotalCalories(Math.round(totalCal * 10.0) / 10.0);

        // 5. 计算三大营养素各自提供的卡路里 (蛋白质1g=4kcal, 碳水1g=4kcal, 脂肪1g=9kcal)
        summaryDto.setProteinCalories(Math.round(totalPro * 4.0 * 10.0) / 10.0);
        summaryDto.setCarbsCalories(Math.round(totalCarbs * 4.0 * 10.0) / 10.0);
        summaryDto.setFatCalories(Math.round(totalFat * 9.0 * 10.0) / 10.0);

        return summaryDto;
    }



}
