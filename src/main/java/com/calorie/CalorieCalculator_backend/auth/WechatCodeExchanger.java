package com.calorie.CalorieCalculator_backend.auth;

public interface WechatCodeExchanger {
    WechatIdentity exchange(String code);
}
