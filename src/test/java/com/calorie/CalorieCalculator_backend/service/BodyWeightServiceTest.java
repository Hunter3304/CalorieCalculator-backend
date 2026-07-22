package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.dto.BodyWeightSnapshotDto;
import com.calorie.CalorieCalculator_backend.dto.BodyWeightTrendDto;
import com.calorie.CalorieCalculator_backend.entity.BodyWeightRecord;
import com.calorie.CalorieCalculator_backend.mapper.BodyWeightMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BodyWeightServiceTest {
    private final BodyWeightMapper mapper = mock(BodyWeightMapper.class);
    private final BodyWeightService service = new BodyWeightService(mapper);
    private final LocalDate today = LocalDate.of(2026, 7, 22);

    @Test
    void returnsCarriedSnapshotWithSourceRecord() {
        BodyWeightRecord source = record(4, "2026-07-20", "65.5");
        when(mapper.findFirstRecordDate()).thenReturn(source.getRecordDate());
        when(mapper.findLatestOnOrBefore(today)).thenReturn(source);

        BodyWeightSnapshotDto snapshot = service.getSnapshot(today);

        assertEquals(4, snapshot.recordId());
        assertEquals(LocalDate.of(2026, 7, 20), snapshot.sourceDate());
        assertEquals(new BigDecimal("65.5"), snapshot.weightKg());
        assertFalse(snapshot.recordedOnSelectedDate());
    }

    @Test
    void returnsEmptySnapshotBeforeFirstRecord() {
        when(mapper.findFirstRecordDate()).thenReturn(LocalDate.of(2026, 7, 20));
        when(mapper.findLatestOnOrBefore(LocalDate.of(2026, 7, 19))).thenReturn(null);

        BodyWeightSnapshotDto snapshot = service.getSnapshot(LocalDate.of(2026, 7, 19));

        assertNull(snapshot.recordId());
        assertNull(snapshot.weightKg());
        assertFalse(snapshot.recordedOnSelectedDate());
    }

    @Test
    void rejectsFutureAndInvalidPrecision() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validateRecordDate(today.plusDays(1), today));
        assertThrows(IllegalArgumentException.class,
                () -> service.validateWeight(new BigDecimal("65.55")));
        assertThrows(IllegalArgumentException.class,
                () -> service.validateWeight(BigDecimal.ZERO));
    }

    @Test
    void upsertsAValidSelectedDate() {
        LocalDate date = today.minusDays(2);
        BigDecimal weight = new BigDecimal("65.5");
        BodyWeightRecord saved = record(7, "2026-07-20", "65.5");
        when(mapper.findFirstRecordDate()).thenReturn(date);
        when(mapper.findLatestOnOrBefore(date)).thenReturn(saved);

        BodyWeightSnapshotDto snapshot = service.save(date, weight);

        verify(mapper).upsert(date, weight);
        assertTrue(snapshot.recordedOnSelectedDate());
    }

    @Test
    void updateAndDeleteRequireARealRecord() {
        when(mapper.findById(99)).thenReturn(null);

        assertThrows(NoSuchElementException.class,
                () -> service.update(99, new BigDecimal("70.0")));
        assertThrows(NoSuchElementException.class, () -> service.delete(99));
    }

    @Test
    void fillsMissingTrendDatesAndMarksRealRecords() {
        LocalDate start = LocalDate.of(2026, 7, 18);
        BodyWeightRecord baseline = record(1, "2026-07-17", "66.0");
        BodyWeightRecord changed = record(2, "2026-07-20", "65.5");
        when(mapper.findFirstRecordDate()).thenReturn(LocalDate.of(2026, 7, 17));
        when(mapper.findLatestOnOrBefore(start)).thenReturn(baseline);
        when(mapper.findBetween(start, today)).thenReturn(List.of(changed));

        BodyWeightTrendDto trend = service.getTrend(start, today, today);

        assertEquals(5, trend.points().size());
        assertEquals(new BigDecimal("66.0"), trend.points().get(0).weightKg());
        assertFalse(trend.points().get(0).recorded());
        assertEquals(LocalDate.of(2026, 7, 20), trend.points().get(2).date());
        assertTrue(trend.points().get(2).recorded());
        assertEquals(new BigDecimal("65.5"), trend.points().get(4).weightKg());
    }

    @Test
    void leavesDatesBeforeFirstRecordEmpty() {
        LocalDate start = LocalDate.of(2026, 7, 18);
        BodyWeightRecord first = record(2, "2026-07-20", "65.5");
        when(mapper.findFirstRecordDate()).thenReturn(first.getRecordDate());
        when(mapper.findLatestOnOrBefore(start)).thenReturn(null);
        when(mapper.findBetween(start, today)).thenReturn(List.of(first));

        BodyWeightTrendDto trend = service.getTrend(start, today, today);

        assertEquals(5, trend.points().size());
        assertNull(trend.points().get(0).weightKg());
        assertNull(trend.points().get(1).weightKg());
        assertEquals(LocalDate.of(2026, 7, 20), trend.points().get(2).date());
    }

    @Test
    void rejectsFutureOrOversizedTrendRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getTrend(today.minusDays(6), today.plusDays(1), today));
        assertThrows(IllegalArgumentException.class,
                () -> service.getTrend(today.minusDays(365), today, today));
    }

    private BodyWeightRecord record(int id, String date, String weight) {
        return new BodyWeightRecord(id, LocalDate.parse(date), new BigDecimal(weight));
    }
}