package com.calorie.CalorieCalculator_backend.service;

import com.calorie.CalorieCalculator_backend.dto.AccountDto;
import com.calorie.CalorieCalculator_backend.entity.AppUser;
import com.calorie.CalorieCalculator_backend.mapper.AuthMapper;
import com.calorie.CalorieCalculator_backend.mapper.BodyCircumferenceMapper;
import com.calorie.CalorieCalculator_backend.mapper.BodyWeightMapper;
import com.calorie.CalorieCalculator_backend.mapper.DailyRecordMapper;
import com.calorie.CalorieCalculator_backend.mapper.FoodMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class AccountService {
    private final AuthMapper authMapper;
    private final DailyRecordMapper dailyRecordMapper;
    private final BodyWeightMapper bodyWeightMapper;
    private final BodyCircumferenceMapper circumferenceMapper;
    private final FoodMapper foodMapper;

    public AccountService(AuthMapper authMapper, DailyRecordMapper dailyRecordMapper,
                          BodyWeightMapper bodyWeightMapper,
                          BodyCircumferenceMapper circumferenceMapper,
                          FoodMapper foodMapper) {
        this.authMapper = authMapper;
        this.dailyRecordMapper = dailyRecordMapper;
        this.bodyWeightMapper = bodyWeightMapper;
        this.circumferenceMapper = circumferenceMapper;
        this.foodMapper = foodMapper;
    }

    public AccountDto getAccount(Long userId) {
        AppUser user = authMapper.findUser(userId);
        if (user == null) {
            throw new NoSuchElementException("Account not found");
        }
        return new AccountDto(user.getCreatedAt());
    }

    @Transactional
    public void deleteAccount(Long userId) {
        if (authMapper.findUser(userId) == null) {
            throw new NoSuchElementException("Account not found");
        }
        dailyRecordMapper.deleteByUser(userId);
        bodyWeightMapper.deleteByUser(userId);
        circumferenceMapper.deleteByUser(userId);
        foodMapper.deleteCustomFoodsByUser(userId);
        authMapper.deleteSessionsByUser(userId);
        if (authMapper.deleteUser(userId) == 0) {
            throw new NoSuchElementException("Account not found");
        }
    }
}
