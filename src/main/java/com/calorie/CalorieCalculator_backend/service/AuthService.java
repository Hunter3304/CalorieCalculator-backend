package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.auth.AuthException;
import com.calorie.CalorieCalculator_backend.auth.WechatCodeExchanger;
import com.calorie.CalorieCalculator_backend.auth.WechatIdentity;
import com.calorie.CalorieCalculator_backend.dto.AuthSessionDto;
import com.calorie.CalorieCalculator_backend.entity.AuthenticatedSession;
import com.calorie.CalorieCalculator_backend.mapper.AuthMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthService {
    private static final int TOKEN_BYTES = 32;
    private final WechatCodeExchanger exchanger;
    private final AuthMapper authMapper;
    private final Duration sessionLifetime;
    private final Clock clock;
    private final SecureRandom secureRandom;

    @org.springframework.beans.factory.annotation.Autowired
    public AuthService(
            WechatCodeExchanger exchanger,
            AuthMapper authMapper,
            @Value("${auth.session-days:30}") long sessionDays) {
        this(exchanger, authMapper, Duration.ofDays(sessionDays), Clock.systemUTC(), new SecureRandom());
    }

    AuthService(
            WechatCodeExchanger exchanger,
            AuthMapper authMapper,
            Duration sessionLifetime,
            Clock clock,
            SecureRandom secureRandom) {
        if (sessionLifetime.isZero() || sessionLifetime.isNegative()) {
            throw new IllegalArgumentException("Session lifetime must be positive");
        }
        this.exchanger = exchanger;
        this.authMapper = authMapper;
        this.sessionLifetime = sessionLifetime;
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    @org.springframework.transaction.annotation.Transactional
    public AuthSessionDto login(String code) {
        if (code == null || code.isBlank()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "WeChat login code is required");
        }
        WechatIdentity identity = exchanger.exchange(code.trim());
        if (identity == null || identity.openid() == null || identity.openid().isBlank()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "WeChat login code was rejected");
        }

        Long userId = authMapper.upsertUser(identity.openid());
        if (userId == null) {
            throw new AuthException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create app user");
        }
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        Instant expiresAt = clock.instant().plus(sessionLifetime);
        authMapper.insertSession(userId, sha256(token), expiresAt);
        authMapper.deleteExpiredSessions(clock.instant());
        return new AuthSessionDto(token, expiresAt);
    }

    public AuthenticatedSession authenticateBearer(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        String tokenHash = sha256(token);
        Long userId = authMapper.findValidUserId(tokenHash, clock.instant());
        if (userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Session is invalid or expired");
        }
        return new AuthenticatedSession(userId, tokenHash);
    }

    public void logout(Long userId, String tokenHash) {
        authMapper.deleteSession(tokenHash, userId);
    }

    static String sha256(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
