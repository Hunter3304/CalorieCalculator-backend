package com.calorie.CalorieCalculator_backend.auth;

import com.calorie.CalorieCalculator_backend.entity.AuthenticatedSession;
import com.calorie.CalorieCalculator_backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ApiAuthenticationFilterTest {
    private final AuthService authService = mock(AuthService.class);
    private final ApiAuthenticationFilter filter = new ApiAuthenticationFilter(authService);

    @Test
    void attachesTheServerDerivedUserToProtectedRequests() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/records/2026-08-05");
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authService.authenticateBearer("Bearer token"))
                .thenReturn(new AuthenticatedSession(42L, "hash"));

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(42L, request.getAttribute(CurrentUserAttributes.USER_ID));
        assertEquals("hash", request.getAttribute(CurrentUserAttributes.TOKEN_HASH));
        assertEquals(200, response.getStatus());
    }

    @Test
    void rejectsMissingOrExpiredSessionsWith401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/weights/2026-08-05");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authService.authenticateBearer(null))
                .thenThrow(new AuthException(HttpStatus.UNAUTHORIZED, "Authentication is required"));

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        verify(authService).authenticateBearer(null);
    }

    @Test
    void allowsTheWechatLoginEndpointWithoutAnExistingSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/wechat");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        verifyNoInteractions(authService);
    }
}
