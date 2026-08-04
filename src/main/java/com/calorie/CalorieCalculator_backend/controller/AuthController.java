package com.calorie.CalorieCalculator_backend.controller;

import com.calorie.CalorieCalculator_backend.auth.CurrentUserAttributes;
import com.calorie.CalorieCalculator_backend.dto.AuthSessionDto;
import com.calorie.CalorieCalculator_backend.dto.WechatLoginRequest;
import com.calorie.CalorieCalculator_backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/wechat")
    public ResponseEntity<AuthSessionDto> login(@RequestBody WechatLoginRequest request) {
        return ResponseEntity.ok(authService.login(request == null ? null : request.code()));
    }

    @DeleteMapping("/session")
    public ResponseEntity<Void> logout(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId,
            @RequestAttribute(CurrentUserAttributes.TOKEN_HASH) String tokenHash) {
        authService.logout(userId, tokenHash);
        return ResponseEntity.noContent().build();
    }
}
