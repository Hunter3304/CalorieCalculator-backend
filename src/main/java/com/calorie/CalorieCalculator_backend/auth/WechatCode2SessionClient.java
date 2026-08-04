package com.calorie.CalorieCalculator_backend.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Component
public class WechatCode2SessionClient implements WechatCodeExchanger {
    private final String appId;
    private final String appSecret;
    private final RestClient restClient;

    public WechatCode2SessionClient(
            @Value("${wechat.app-id:}") String appId,
            @Value("${wechat.app-secret:}") String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(8));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl("https://api.weixin.qq.com")
                .build();
    }

    @Override
    public WechatIdentity exchange(String code) {
        if (appId.isBlank() || appSecret.isBlank()) {
            throw new AuthException(HttpStatus.SERVICE_UNAVAILABLE,
                    "WeChat login is not configured");
        }
        try {
            WechatResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sns/jscode2session")
                            .queryParam("appid", appId)
                            .queryParam("secret", appSecret)
                            .queryParam("js_code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .body(WechatResponse.class);
            if (response == null
                    || response.errcode() != null && response.errcode() != 0
                    || response.openid() == null
                    || response.openid().isBlank()) {
                throw new AuthException(HttpStatus.UNAUTHORIZED, "WeChat login code was rejected");
            }
            return new WechatIdentity(response.openid());
        } catch (AuthException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new AuthException(HttpStatus.BAD_GATEWAY,
                    "WeChat login service is temporarily unavailable");
        }
    }

    private record WechatResponse(
            @JsonProperty("openid") String openid,
            @JsonProperty("errcode") Integer errcode) {
    }
}
