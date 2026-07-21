package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.dto.CalendarMetadataDto;
import com.calorie.CalorieCalculator_backend.mapper.DailyRecordMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DailyRecordServiceTest {
    private final DailyRecordMapper mapper = mock(DailyRecordMapper.class);
    private final DailyRecordService service = new DailyRecordService(mapper);
    private final LocalDate today = LocalDate.of(2026, 7, 21);

    @Test
    void limitsCalendarToOneYearAndSevenFutureDays() {
        when(mapper.findEarliestRecordDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(mapper.findRecordedDatesBetween(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 28)))
                .thenReturn(List.of(LocalDate.of(2026, 7, 20)));

        CalendarMetadataDto metadata = service.getCalendarMetadata(YearMonth.of(2026, 7), today);

        assertEquals(LocalDate.of(2025, 7, 21), metadata.getMinDate());
        assertEquals(LocalDate.of(2026, 7, 28), metadata.getMaxDate());
        assertEquals(List.of(LocalDate.of(2026, 7, 20)), metadata.getRecordedDates());
        verify(mapper).findRecordedDatesBetween(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 28));
    }

    @Test
    void usesFirstRecordWhenItIsWithinTheLastYear() {
        when(mapper.findEarliestRecordDate()).thenReturn(LocalDate.of(2026, 6, 10));
        when(mapper.findRecordedDatesBetween(LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(LocalDate.of(2026, 6, 10)));

        CalendarMetadataDto metadata = service.getCalendarMetadata(YearMonth.of(2026, 6), today);

        assertEquals(LocalDate.of(2026, 6, 10), metadata.getMinDate());
        assertEquals(List.of(LocalDate.of(2026, 6, 10)), metadata.getRecordedDates());
    }

    @Test
    void usesTodayAsMinimumWhenThereAreNoRecords() {
        when(mapper.findEarliestRecordDate()).thenReturn(null);

        CalendarMetadataDto metadata = service.getCalendarMetadata(YearMonth.of(2026, 5), today);

        assertEquals(today, metadata.getMinDate());
        assertEquals(List.of(), metadata.getRecordedDates());
    }
    @Test
    void keepsTodaySelectableWhenTheFirstRecordIsInTheFuture() {
        when(mapper.findEarliestRecordDate()).thenReturn(LocalDate.of(2026, 7, 25));
        when(mapper.findRecordedDatesBetween(LocalDate.of(2026, 7, 21), LocalDate.of(2026, 7, 28)))
                .thenReturn(List.of(LocalDate.of(2026, 7, 25)));

        CalendarMetadataDto metadata = service.getCalendarMetadata(YearMonth.of(2026, 7), today);

        assertEquals(today, metadata.getMinDate());
    }


    @Test
    void rejectsDatesOutsideTheSelectableRange() {
        when(mapper.findEarliestRecordDate()).thenReturn(LocalDate.of(2026, 6, 10));

        assertThrows(IllegalArgumentException.class,
                () -> service.validateRecordDate(LocalDate.of(2026, 6, 9), today));
        assertThrows(IllegalArgumentException.class,
                () -> service.validateRecordDate(LocalDate.of(2026, 7, 29), today));
    }
}
