package com.calorie.CalorieCalculator_backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WechatCode2SessionClientTest {
    private static final MediaType WECHAT_JSON =
            MediaType.parseMediaType("application/json; encoding=utf-8");

    @Test
    void acceptsWechatJsonWithNonStandardEncodingParameter() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        WechatCode2SessionClient client = new WechatCode2SessionClient(
                "test-app-id", "test-secret", new ObjectMapper(), builder.build());

        server.expect(queryParam("js_code", "one-time-code"))
                .andRespond(withSuccess(
                        "{\"session_key\":\"not-stored\",\"openid\":\"openid-1\"}",
                        WECHAT_JSON));

        assertEquals("openid-1", client.exchange("one-time-code").openid());
        server.verify();
    }

    @Test
    void preservesRejectedCodeBehaviorForWechatErrorJson() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        WechatCode2SessionClient client = new WechatCode2SessionClient(
                "test-app-id", "test-secret", new ObjectMapper(), builder.build());

        server.expect(queryParam("js_code", "expired-code"))
                .andRespond(withSuccess(
                        "{\"errcode\":40029,\"errmsg\":\"invalid code\"}",
                        WECHAT_JSON));

        AuthException exception = assertThrows(
                AuthException.class, () -> client.exchange("expired-code"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.status());
        server.verify();
    }
}
