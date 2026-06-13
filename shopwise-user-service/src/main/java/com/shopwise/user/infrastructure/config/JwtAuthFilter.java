package com.shopwise.user.infrastructure.config;

import com.shopwise.user.infrastructure.exception.BusinessException;
import com.shopwise.user.infrastructure.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    // Token gerektirmeyen endpoint'ler
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/users/login",
            "/api/v1/users"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Public endpoint kontrolü
        if (isPublicPath(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "Token bulunamadı");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            sendUnauthorized(response, "Geçersiz token");
            return;
        }

        // Token geçerli SecurityContext'e set et
        Long userId = jwtService.extractUserId(token);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(userId), null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path, String method) {
        return (path.equals("/api/v1/users") && method.equals("POST")) ||
                path.equals("/api/v1/users/login");
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"errorCode\":\"UNAUTHORIZED\",\"message\":\"" + message + "\"}"
        );
    }
}