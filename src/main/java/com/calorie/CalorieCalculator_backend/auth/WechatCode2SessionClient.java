package com.calorie.CalorieCalculator_backend.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public WechatCode2SessionClient(
            @Value("${wechat.app-id:}") String appId,
            @Value("${wechat.app-secret:}") String appSecret) {
        this(appId, appSecret, new ObjectMapper(), createRestClient());
    }

    WechatCode2SessionClient(
            String appId,
            String appSecret,
            ObjectMapper objectMapper,
            RestClient restClient) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    private static RestClient createRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(8));
        return RestClient.builder()
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
            String responseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sns/jscode2session")
                            .queryParam("appid", appId)
                            .queryParam("secret", appSecret)
                            .queryParam("js_code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .body(String.class);
            if (responseBody == null || responseBody.isBlank()) {
                throw new AuthException(HttpStatus.BAD_GATEWAY,
                        "WeChat login service is temporarily unavailable");
            }
            WechatResponse response = objectMapper.readValue(responseBody, WechatResponse.class);
            if (response.errcode() != null && response.errcode() != 0
                    || response.openid() == null
                    || response.openid().isBlank()) {
                throw new AuthException(HttpStatus.UNAUTHORIZED, "WeChat login code was rejected");
            }
            return new WechatIdentity(response.openid());
        } catch (AuthException exception) {
            throw exception;
        } catch (RestClientException | JsonProcessingException exception) {
            throw new AuthException(HttpStatus.BAD_GATEWAY,
                    "WeChat login service is temporarily unavailable");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WechatResponse(
            @JsonProperty("openid") String openid,
            @JsonProperty("errcode") Integer errcode) {
    }
}
