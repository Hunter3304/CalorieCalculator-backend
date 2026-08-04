package com.calorie.CalorieCalculator_backend.controller;

import com.calorie.CalorieCalculator_backend.auth.CurrentUserAttributes;
import com.calorie.CalorieCalculator_backend.dto.AccountDto;
import com.calorie.CalorieCalculator_backend.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<AccountDto> getAccount(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId) {
        return ResponseEntity.ok(accountService.getAccount(userId));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(
            @RequestAttribute(CurrentUserAttributes.USER_ID) Long userId) {
        accountService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }
}
