package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceRequest;
import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceSnapshotDto;
import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceTrendDto;
import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceTrendPointDto;
import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceValueDto;
import com.calorie.CalorieCalculator_backend.entity.BodyCircumferenceRecord;
import com.calorie.CalorieCalculator_backend.entity.BodyCircumferenceType;
import com.calorie.CalorieCalculator_backend.mapper.BodyCircumferenceMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

@Service
public class BodyCircumferenceService {
    private static final int MAX_TREND_DAYS = 365;
    private final BodyCircumferenceMapper mapper;

    public BodyCircumferenceService(BodyCircumferenceMapper mapper) {
        this.mapper = mapper;
    }

    public BodyCircumferenceSnapshotDto getSnapshot(Long userId, LocalDate selectedDate) {
        requireDate(selectedDate);
        BodyCircumferenceRecord selectedRecord = mapper.findByDate(userId, selectedDate);
        return new BodyCircumferenceSnapshotDto(
                selectedDate,
                selectedRecord == null ? null : selectedRecord.getId(),
                resolveValue(userId, selectedDate, BodyCircumferenceType.CHEST),
                resolveValue(userId, selectedDate, BodyCircumferenceType.WAIST),
                resolveValue(userId, selectedDate, BodyCircumferenceType.HIP),
                resolveValue(userId, selectedDate, BodyCircumferenceType.ARM),
                resolveValue(userId, selectedDate, BodyCircumferenceType.THIGH),
                resolveValue(userId, selectedDate, BodyCircumferenceType.CALF));
    }

    public BodyCircumferenceSnapshotDto save(
            Long userId,
            LocalDate recordDate,
            BodyCircumferenceRequest request) {
        validateRecordDate(recordDate, LocalDate.now());
        if (request == null) {
            throw new IllegalArgumentException("Circumference request is required");
        }

        EnumMap<BodyCircumferenceType, BigDecimal> requestedValues = requestValues(request);
        requestedValues.values().forEach(this::validateValue);
        Set<BodyCircumferenceType> clearFields = parseClearFields(request.clearFields());
        BodyCircumferenceRecord existing = mapper.findByDate(userId, recordDate);

        if (existing == null) {
            if (requestedValues.values().stream().allMatch(Objects::isNull)) {
                return getSnapshot(userId, recordDate);
            }
            BodyCircumferenceRecord created = new BodyCircumferenceRecord();
            created.setRecordDate(recordDate);
            applyValues(created, requestedValues);
            mapper.insert(userId, created);
            return getSnapshot(userId, recordDate);
        }

        for (BodyCircumferenceType type : BodyCircumferenceType.values()) {
            BigDecimal requested = requestedValues.get(type);
            if (requested != null) {
                setValue(existing, type, requested);
            } else if (clearFields.contains(type)) {
                setValue(existing, type, null);
            }
        }

        if (isEmpty(existing)) {
            mapper.deleteById(userId, existing.getId());
        } else {
            mapper.update(userId, existing);
        }
        return getSnapshot(userId, recordDate);
    }

    public void delete(Long userId, Integer id) {
        requireRecord(userId, id);
        if (mapper.deleteById(userId, id) == 0) {
            throw new NoSuchElementException("Circumference record not found");
        }
    }

    public BodyCircumferenceTrendDto getTrend(
            Long userId,
            String measurementType,
            LocalDate startDate,
            LocalDate endDate) {
        return getTrend(userId, measurementType, startDate, endDate, LocalDate.now());
    }

    BodyCircumferenceTrendDto getTrend(
            Long userId,
            String measurementType,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate today) {
        BodyCircumferenceType type = BodyCircumferenceType.fromKey(measurementType);
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

        LocalDate firstRecordDate = mapper.findFirstDateForColumn(userId, type.columnName());
        BodyCircumferenceRecord current =
                mapper.findLatestForColumn(userId, startDate.minusDays(1), type.columnName());
        List<BodyCircumferenceRecord> records =
                mapper.findBetweenForColumn(userId, startDate, endDate, type.columnName());
        List<BodyCircumferenceTrendPointDto> points = new ArrayList<>();
        int recordIndex = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            boolean recorded = false;
            if (recordIndex < records.size()
                    && records.get(recordIndex).getRecordDate().equals(date)) {
                current = records.get(recordIndex++);
                recorded = true;
            }
            points.add(current == null
                    ? new BodyCircumferenceTrendPointDto(date, null, null, false)
                    : new BodyCircumferenceTrendPointDto(
                            date, getValue(current, type), current.getRecordDate(), recorded));
        }

        return new BodyCircumferenceTrendDto(
                type.key(), startDate, endDate, firstRecordDate, points);
    }

    void validateRecordDate(LocalDate recordDate, LocalDate today) {
        requireDate(recordDate);
        if (recordDate.isAfter(today)) {
            throw new IllegalArgumentException("Circumference cannot be recorded for a future date");
        }
    }

    void validateValue(BigDecimal valueCm) {
        if (valueCm == null) {
            return;
        }
        if (valueCm.signum() <= 0) {
            throw new IllegalArgumentException("Circumference must be greater than zero");
        }
        if (valueCm.stripTrailingZeros().scale() > 1) {
            throw new IllegalArgumentException("Circumference supports at most one decimal place");
        }
    }

    private BodyCircumferenceValueDto resolveValue(
            Long userId,
            LocalDate selectedDate,
            BodyCircumferenceType type) {
        BodyCircumferenceRecord record =
                mapper.findLatestForColumn(userId, selectedDate, type.columnName());
        if (record == null) {
            return new BodyCircumferenceValueDto(null, null, false);
        }
        return new BodyCircumferenceValueDto(
                getValue(record, type),
                record.getRecordDate(),
                selectedDate.equals(record.getRecordDate()));
    }

    private EnumMap<BodyCircumferenceType, BigDecimal> requestValues(
            BodyCircumferenceRequest request) {
        EnumMap<BodyCircumferenceType, BigDecimal> values =
                new EnumMap<>(BodyCircumferenceType.class);
        values.put(BodyCircumferenceType.CHEST, request.chestCm());
        values.put(BodyCircumferenceType.WAIST, request.waistCm());
        values.put(BodyCircumferenceType.HIP, request.hipCm());
        values.put(BodyCircumferenceType.ARM, request.armCm());
        values.put(BodyCircumferenceType.THIGH, request.thighCm());
        values.put(BodyCircumferenceType.CALF, request.calfCm());
        return values;
    }

    private Set<BodyCircumferenceType> parseClearFields(List<String> clearFields) {
        if (clearFields == null) {
            return EnumSet.noneOf(BodyCircumferenceType.class);
        }
        EnumSet<BodyCircumferenceType> parsed =
                EnumSet.noneOf(BodyCircumferenceType.class);
        clearFields.forEach(key -> parsed.add(BodyCircumferenceType.fromKey(key)));
        return parsed;
    }

    private void applyValues(
            BodyCircumferenceRecord record,
            EnumMap<BodyCircumferenceType, BigDecimal> values) {
        values.forEach((type, value) -> setValue(record, type, value));
    }

    private BigDecimal getValue(
            BodyCircumferenceRecord record,
            BodyCircumferenceType type) {
        return switch (type) {
            case CHEST -> record.getChestCm();
            case WAIST -> record.getWaistCm();
            case HIP -> record.getHipCm();
            case ARM -> record.getArmCm();
            case THIGH -> record.getThighCm();
            case CALF -> record.getCalfCm();
        };
    }

    private void setValue(
            BodyCircumferenceRecord record,
            BodyCircumferenceType type,
            BigDecimal value) {
        switch (type) {
            case CHEST -> record.setChestCm(value);
            case WAIST -> record.setWaistCm(value);
            case HIP -> record.setHipCm(value);
            case ARM -> record.setArmCm(value);
            case THIGH -> record.setThighCm(value);
            case CALF -> record.setCalfCm(value);
        }
    }

    private boolean isEmpty(BodyCircumferenceRecord record) {
        return Arrays.stream(BodyCircumferenceType.values())
                .allMatch(type -> getValue(record, type) == null);
    }

    private BodyCircumferenceRecord requireRecord(Long userId, Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Circumference record id is required");
        }
        BodyCircumferenceRecord record = mapper.findById(userId, id);
        if (record == null) {
            throw new NoSuchElementException("Circumference record not found");
        }
        return record;
    }

    private void requireDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }
    }
}
