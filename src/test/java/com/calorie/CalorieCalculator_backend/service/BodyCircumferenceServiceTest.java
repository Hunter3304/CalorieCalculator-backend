package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceRequest;
import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceSnapshotDto;
import com.calorie.CalorieCalculator_backend.dto.BodyCircumferenceTrendDto;
import com.calorie.CalorieCalculator_backend.entity.BodyCircumferenceRecord;
import com.calorie.CalorieCalculator_backend.mapper.BodyCircumferenceMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BodyCircumferenceServiceTest {
    private final BodyCircumferenceMapper mapper = mock(BodyCircumferenceMapper.class);
    private final BodyCircumferenceService service = new BodyCircumferenceService(mapper);
    private final LocalDate today = LocalDate.of(2026, 7, 27);

    @Test
    void resolvesEachMeasurementFromItsOwnSourceDate() {
        BodyCircumferenceRecord chest = record(1, "2026-07-20");
        chest.setChestCm(new BigDecimal("92.5"));
        BodyCircumferenceRecord waist = record(2, "2026-07-23");
        waist.setWaistCm(new BigDecimal("78.0"));
        when(mapper.findLatestForColumn(today, "chest_cm")).thenReturn(chest);
        when(mapper.findLatestForColumn(today, "waist_cm")).thenReturn(waist);

        BodyCircumferenceSnapshotDto snapshot = service.getSnapshot(today);

        assertEquals(new BigDecimal("92.5"), snapshot.chest().valueCm());
        assertEquals(LocalDate.of(2026, 7, 20), snapshot.chest().sourceDate());
        assertEquals(new BigDecimal("78.0"), snapshot.waist().valueCm());
        assertEquals(LocalDate.of(2026, 7, 23), snapshot.waist().sourceDate());
        assertNull(snapshot.hip().valueCm());
        assertFalse(snapshot.chest().recordedOnSelectedDate());
    }

    @Test
    void treatsAllBlankCreateAsNoOp() {
        BodyCircumferenceRequest request = blankRequest(null);
        when(mapper.findByDate(today)).thenReturn(null);

        BodyCircumferenceSnapshotDto snapshot = service.save(today, request);

        verify(mapper, never()).insert(any());
        verify(mapper, never()).update(any());
        assertNull(snapshot.recordId());
        assertNull(snapshot.chest().valueCm());
    }

    @Test
    void insertsOnlyProvidedMeasurements() {
        BodyCircumferenceRequest request = new BodyCircumferenceRequest(
                null, new BigDecimal("78.5"), null, null, null, null, null);
        when(mapper.findByDate(today)).thenReturn(null);

        service.save(today, request);

        verify(mapper).insert(argThat(record ->
                record.getChestCm() == null
                        && new BigDecimal("78.5").equals(record.getWaistCm())
                        && record.getHipCm() == null));
    }

    @Test
    void clearsOneOverrideAndKeepsTheSparseRecord() {
        BodyCircumferenceRecord existing = record(3, today.toString());
        existing.setChestCm(new BigDecimal("92.0"));
        existing.setWaistCm(new BigDecimal("78.0"));
        when(mapper.findByDate(today)).thenReturn(existing);

        service.save(today, blankRequest(List.of("chest")));

        assertNull(existing.getChestCm());
        assertEquals(new BigDecimal("78.0"), existing.getWaistCm());
        verify(mapper).update(existing);
        verify(mapper, never()).deleteById(3);
    }

    @Test
    void removesRowWhenItsLastOverrideIsCleared() {
        BodyCircumferenceRecord existing = record(4, today.toString());
        existing.setCalfCm(new BigDecimal("36.0"));
        when(mapper.findByDate(today)).thenReturn(existing);

        service.save(today, blankRequest(List.of("calf")));

        verify(mapper).deleteById(4);
        verify(mapper, never()).update(any());
    }

    @Test
    void validatesDatePrecisionAndMeasurementType() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validateRecordDate(today.plusDays(1), today));
        assertThrows(IllegalArgumentException.class,
                () -> service.validateValue(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> service.validateValue(new BigDecimal("80.55")));
        assertThrows(IllegalArgumentException.class,
                () -> service.getTrend("unknown", today.minusDays(6), today, today));
    }

    @Test
    void buildsFieldSpecificTrendWithBlankAndCarriedDates() {
        LocalDate start = LocalDate.of(2026, 7, 23);
        BodyCircumferenceRecord first = record(5, "2026-07-25");
        first.setWaistCm(new BigDecimal("78.0"));
        when(mapper.findFirstDateForColumn("waist_cm")).thenReturn(first.getRecordDate());
        when(mapper.findLatestForColumn(start.minusDays(1), "waist_cm")).thenReturn(null);
        when(mapper.findBetweenForColumn(start, today, "waist_cm")).thenReturn(List.of(first));

        BodyCircumferenceTrendDto trend = service.getTrend("waist", start, today, today);

        assertEquals(5, trend.points().size());
        assertNull(trend.points().get(0).valueCm());
        assertNull(trend.points().get(1).valueCm());
        assertTrue(trend.points().get(2).recorded());
        assertEquals(new BigDecimal("78.0"), trend.points().get(4).valueCm());
        assertFalse(trend.points().get(4).recorded());
        assertEquals(first.getRecordDate(), trend.points().get(4).sourceDate());
    }

    @Test
    void rejectsInvalidTrendRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getTrend("waist", today, today.plusDays(1), today));
        assertThrows(IllegalArgumentException.class,
                () -> service.getTrend("waist", today, today.minusDays(1), today));
        assertThrows(IllegalArgumentException.class,
                () -> service.getTrend("waist", today.minusDays(365), today, today));
    }

    @Test
    void deleteRequiresARealRecord() {
        when(mapper.findById(99)).thenReturn(null);
        assertThrows(NoSuchElementException.class, () -> service.delete(99));
    }

    private BodyCircumferenceRequest blankRequest(List<String> clearFields) {
        return new BodyCircumferenceRequest(
                null, null, null, null, null, null, clearFields);
    }

    private BodyCircumferenceRecord record(int id, String date) {
        BodyCircumferenceRecord record = new BodyCircumferenceRecord();
        record.setId(id);
        record.setRecordDate(LocalDate.parse(date));
        return record;
    }
}
