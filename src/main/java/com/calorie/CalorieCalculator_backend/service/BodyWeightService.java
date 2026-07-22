package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.dto.BodyWeightSnapshotDto;
import com.calorie.CalorieCalculator_backend.dto.BodyWeightTrendDto;
import com.calorie.CalorieCalculator_backend.dto.BodyWeightTrendPointDto;
import com.calorie.CalorieCalculator_backend.entity.BodyWeightRecord;
import com.calorie.CalorieCalculator_backend.mapper.BodyWeightMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BodyWeightService {
    private static final int MAX_TREND_DAYS = 365;
    private final BodyWeightMapper bodyWeightMapper;

    public BodyWeightService(BodyWeightMapper bodyWeightMapper) {
        this.bodyWeightMapper = bodyWeightMapper;
    }

    public BodyWeightSnapshotDto getSnapshot(LocalDate selectedDate) {
        requireDate(selectedDate);
        LocalDate firstRecordDate = bodyWeightMapper.findFirstRecordDate();
        BodyWeightRecord record = bodyWeightMapper.findLatestOnOrBefore(selectedDate);
        if (record == null) {
            return new BodyWeightSnapshotDto(selectedDate, null, null, null, false, firstRecordDate);
        }
        return new BodyWeightSnapshotDto(selectedDate, record.getId(), record.getRecordDate(),
                record.getWeightKg(), selectedDate.equals(record.getRecordDate()), firstRecordDate);
    }

    public BodyWeightSnapshotDto save(LocalDate recordDate, BigDecimal weightKg) {
        validateRecordDate(recordDate, LocalDate.now());
        validateWeight(weightKg);
        bodyWeightMapper.upsert(recordDate, weightKg);
        return getSnapshot(recordDate);
    }

    public BodyWeightSnapshotDto update(Integer id, BigDecimal weightKg) {
        validateWeight(weightKg);
        BodyWeightRecord existing = requireRecord(id);
        bodyWeightMapper.updateWeight(id, weightKg);
        return getSnapshot(existing.getRecordDate());
    }

    public void delete(Integer id) {
        requireRecord(id);
        bodyWeightMapper.deleteById(id);
    }

    public BodyWeightTrendDto getTrend(LocalDate startDate, LocalDate endDate) {
        return getTrend(startDate, endDate, LocalDate.now());
    }

    BodyWeightTrendDto getTrend(LocalDate startDate, LocalDate endDate, LocalDate today) {
        requireDate(startDate);
        requireDate(endDate);
        if (endDate.isAfter(today)) {
            throw new IllegalArgumentException("Trend end date cannot be in the future");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Trend start date cannot be after end date");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) + 1 > MAX_TREND_DAYS) {
            throw new IllegalArgumentException("Trend range cannot exceed 365 days");
        }

        LocalDate firstRecordDate = bodyWeightMapper.findFirstRecordDate();
        if (firstRecordDate == null || endDate.isBefore(firstRecordDate)) {
            return new BodyWeightTrendDto(startDate, endDate, firstRecordDate, List.of());
        }

        BodyWeightRecord current = bodyWeightMapper.findLatestOnOrBefore(startDate);
        List<BodyWeightRecord> records = bodyWeightMapper.findBetween(startDate, endDate);
        List<BodyWeightTrendPointDto> points = new ArrayList<>();
        int recordIndex = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            while (recordIndex < records.size() && records.get(recordIndex).getRecordDate().equals(date)) {
                current = records.get(recordIndex++);
            }
            if (current != null && !date.isBefore(current.getRecordDate())) {
                points.add(new BodyWeightTrendPointDto(date, current.getWeightKg(),
                        current.getRecordDate(), date.equals(current.getRecordDate())));
            } else {
                points.add(new BodyWeightTrendPointDto(date, null, null, false));
            }
        }
        return new BodyWeightTrendDto(startDate, endDate, firstRecordDate, points);
    }

    void validateRecordDate(LocalDate recordDate, LocalDate today) {
        requireDate(recordDate);
        if (recordDate.isAfter(today)) {
            throw new IllegalArgumentException("Weight cannot be recorded for a future date");
        }
    }

    void validateWeight(BigDecimal weightKg) {
        if (weightKg == null || weightKg.signum() <= 0) {
            throw new IllegalArgumentException("Weight must be greater than zero");
        }
        if (weightKg.stripTrailingZeros().scale() > 1) {
            throw new IllegalArgumentException("Weight supports at most one decimal place");
        }
    }

    private BodyWeightRecord requireRecord(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Weight record id is required");
        }
        BodyWeightRecord record = bodyWeightMapper.findById(id);
        if (record == null) {
            throw new NoSuchElementException("Weight record not found");
        }
        return record;
    }

    private void requireDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }
    }
}