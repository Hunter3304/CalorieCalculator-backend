package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.auth.AuthException;
import com.calorie.CalorieCalculator_backend.auth.WechatCodeExchanger;
import com.calorie.CalorieCalculator_backend.auth.WechatIdentity;
import com.calorie.CalorieCalculator_backend.dto.AuthSessionDto;
import com.calorie.CalorieCalculator_backend.entity.AuthenticatedSession;
import com.calorie.CalorieCalculator_backend.mapper.AuthMapper;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final Instant now = Instant.parse("2026-08-05T00:00:00Z");
    private final AuthMapper mapper = mock(AuthMapper.class);
    private final WechatCodeExchanger exchanger = mock(WechatCodeExchanger.class);
    private final AuthService service = new AuthService(
            exchanger, mapper, Duration.ofDays(30),
            Clock.fixed(now, ZoneOffset.UTC), new SecureRandom());

    @Test
    void exchangesCodeAndStoresOnlyTheTokenHash() {
        when(exchanger.exchange("one-time-code")).thenReturn(new WechatIdentity("openid-1"));
        when(mapper.upsertUser("openid-1")).thenReturn(7L);

        AuthSessionDto session = service.login("one-time-code");

        assertNotNull(session.token());
        assertFalse(session.token().isBlank());
        assertEquals(now.plus(Duration.ofDays(30)), session.expiresAt());
        verify(mapper).insertSession(eq(7L), argThat(hash ->
                hash.length() == 64 && !hash.contains(session.token())), eq(session.expiresAt()));
        verify(mapper).deleteExpiredSessions(now);
    }

    @Test
    void authenticatesAValidBearerTokenWithoutReturningOpenid() {
        String rawToken = "raw-secret-token";
        String hash = AuthService.sha256(rawToken);
        when(mapper.findValidUserId(hash, now)).thenReturn(8L);

        AuthenticatedSession session = service.authenticateBearer("Bearer " + rawToken);

        assertEquals(8L, session.userId());
        assertEquals(hash, session.tokenHash());
    }

    @Test
    void rejectsMissingExpiredAndInvalidLoginCodes() {
        assertThrows(AuthException.class, () -> service.login(" "));
        assertThrows(AuthException.class, () -> service.authenticateBearer(null));
        when(mapper.findValidUserId(anyString(), eq(now))).thenReturn(null);
        assertThrows(AuthException.class,
                () -> service.authenticateBearer("Bearer expired-token"));
    }
}
