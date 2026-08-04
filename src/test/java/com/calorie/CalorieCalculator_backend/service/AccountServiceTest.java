package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.entity.AppUser;
import com.calorie.CalorieCalculator_backend.mapper.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {
    private static final Long USER_ID = 42L;
    private final AuthMapper authMapper = mock(AuthMapper.class);
    private final DailyRecordMapper dailyMapper = mock(DailyRecordMapper.class);
    private final BodyWeightMapper weightMapper = mock(BodyWeightMapper.class);
    private final BodyCircumferenceMapper circumferenceMapper = mock(BodyCircumferenceMapper.class);
    private final FoodMapper foodMapper = mock(FoodMapper.class);
    private final AccountService service = new AccountService(
            authMapper, dailyMapper, weightMapper, circumferenceMapper, foodMapper);

    @Test
    void returnsOnlyNonSensitiveAccountMetadata() {
        AppUser user = new AppUser();
        user.setId(USER_ID);
        user.setCreatedAt(Instant.parse("2026-08-05T00:00:00Z"));
        when(authMapper.findUser(USER_ID)).thenReturn(user);

        var account = service.getAccount(USER_ID);

        assertEquals(user.getCreatedAt(), account.createdAt());
    }

    @Test
    void deletesEveryPersonalDataTypeForOnlyTheCurrentUser() {
        when(authMapper.findUser(USER_ID)).thenReturn(new AppUser());
        when(authMapper.deleteUser(USER_ID)).thenReturn(1);

        service.deleteAccount(USER_ID);

        verify(dailyMapper).deleteByUser(USER_ID);
        verify(weightMapper).deleteByUser(USER_ID);
        verify(circumferenceMapper).deleteByUser(USER_ID);
        verify(foodMapper).deleteCustomFoodsByUser(USER_ID);
        verify(authMapper).deleteSessionsByUser(USER_ID);
        verify(authMapper).deleteUser(USER_ID);
    }

    @Test
    void missingAccountDoesNotDeleteAnyRows() {
        when(authMapper.findUser(USER_ID)).thenReturn(null);

        assertThrows(NoSuchElementException.class, () -> service.deleteAccount(USER_ID));

        verifyNoInteractions(dailyMapper, weightMapper, circumferenceMapper, foodMapper);
        verify(authMapper, never()).deleteUser(anyLong());
    }
}
