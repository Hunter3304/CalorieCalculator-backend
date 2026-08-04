package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.dto.CalendarMetadataDto;
import com.calorie.CalorieCalculator_backend.entity.FoodItem;
import com.calorie.CalorieCalculator_backend.mapper.DailyRecordMapper;
import com.calorie.CalorieCalculator_backend.mapper.FoodMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DailyRecordServiceTest {
    private static final Long USER_ID = 42L;
    private final DailyRecordMapper mapper = mock(DailyRecordMapper.class);
    private final FoodMapper foodMapper = mock(FoodMapper.class);
    private final DailyRecordService service = new DailyRecordService(mapper, foodMapper);
    private final LocalDate today = LocalDate.of(2026, 7, 21);

    @Test
    void limitsCalendarToOneYearAndSevenFutureDays() {
        when(mapper.findEarliestRecordDate(USER_ID)).thenReturn(LocalDate.of(2024, 1, 1));
        when(mapper.findRecordedDatesBetween(
                USER_ID, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 28)))
                .thenReturn(List.of(LocalDate.of(2026, 7, 20)));

        CalendarMetadataDto metadata =
                service.getCalendarMetadata(USER_ID, YearMonth.of(2026, 7), today);

        assertEquals(LocalDate.of(2025, 7, 21), metadata.getMinDate());
        assertEquals(LocalDate.of(2026, 7, 28), metadata.getMaxDate());
        assertEquals(List.of(LocalDate.of(2026, 7, 20)), metadata.getRecordedDates());
        verify(mapper).findRecordedDatesBetween(
                USER_ID, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 28));
    }

    @Test
    void usesFirstRecordWhenItIsWithinTheLastYear() {
        when(mapper.findEarliestRecordDate(USER_ID)).thenReturn(LocalDate.of(2026, 6, 10));
        when(mapper.findRecordedDatesBetween(
                USER_ID, LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(LocalDate.of(2026, 6, 10)));

        CalendarMetadataDto metadata =
                service.getCalendarMetadata(USER_ID, YearMonth.of(2026, 6), today);

        assertEquals(LocalDate.of(2026, 6, 10), metadata.getMinDate());
        assertEquals(List.of(LocalDate.of(2026, 6, 10)), metadata.getRecordedDates());
    }

    @Test
    void usesTodayAsMinimumWhenThereAreNoRecords() {
        when(mapper.findEarliestRecordDate(USER_ID)).thenReturn(null);

        CalendarMetadataDto metadata =
                service.getCalendarMetadata(USER_ID, YearMonth.of(2026, 5), today);

        assertEquals(today, metadata.getMinDate());
        assertEquals(List.of(), metadata.getRecordedDates());
    }

    @Test
    void keepsTodaySelectableWhenTheFirstRecordIsInTheFuture() {
        when(mapper.findEarliestRecordDate(USER_ID)).thenReturn(LocalDate.of(2026, 7, 25));
        when(mapper.findRecordedDatesBetween(
                USER_ID, LocalDate.of(2026, 7, 21), LocalDate.of(2026, 7, 28)))
                .thenReturn(List.of(LocalDate.of(2026, 7, 25)));

        CalendarMetadataDto metadata =
                service.getCalendarMetadata(USER_ID, YearMonth.of(2026, 7), today);

        assertEquals(today, metadata.getMinDate());
    }

    @Test
    void rejectsDatesOutsideTheSelectableRange() {
        when(mapper.findEarliestRecordDate(USER_ID)).thenReturn(LocalDate.of(2026, 6, 10));

        assertThrows(IllegalArgumentException.class,
                () -> service.validateRecordDate(
                        USER_ID, LocalDate.of(2026, 6, 9), today));
        assertThrows(IllegalArgumentException.class,
                () -> service.validateRecordDate(
                        USER_ID, LocalDate.of(2026, 7, 29), today));
    }

    @Test
    void addingARecordRequiresAnAccessibleFoodAndScopesUsage() {
        FoodItem food = new FoodItem();
        when(mapper.findEarliestRecordDate(USER_ID)).thenReturn(today);
        when(foodMapper.getFoodById(7L, USER_ID)).thenReturn(food);

        service.addRecord(USER_ID, today, 7, 125.0);

        verify(mapper).insertRecord(USER_ID, today, 7, 125.0);
        verify(foodMapper).recordFoodUsage(USER_ID, 7);
        assertThrows(NoSuchElementException.class,
                () -> service.addRecord(99L, LocalDate.now(), 7, 125.0));
    }
}
