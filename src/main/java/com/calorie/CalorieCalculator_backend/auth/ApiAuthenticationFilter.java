package com.calorie.CalorieCalculator_backend.auth;

import com.calorie.CalorieCalculator_backend.entity.AuthenticatedSession;
import com.calorie.CalorieCalculator_backend.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiAuthenticationFilter extends OncePerRequestFilter {
    private final AuthService authService;

    public ApiAuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !path.startsWith("/api/")
                || "/api/auth/wechat".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            AuthenticatedSession session =
                    authService.authenticateBearer(request.getHeader("Authorization"));
            request.setAttribute(CurrentUserAttributes.USER_ID, session.userId());
            request.setAttribute(CurrentUserAttributes.TOKEN_HASH, session.tokenHash());
            filterChain.doFilter(request, response);
        } catch (AuthException exception) {
            response.setStatus(exception.status().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"unauthorized\"}");
        }
    }
}
