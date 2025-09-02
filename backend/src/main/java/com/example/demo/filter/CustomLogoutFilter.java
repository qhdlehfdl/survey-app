package com.example.demo.filter;

import com.example.demo.auth.dto.response.LogOutResponseDto;
import com.example.demo.auth.service.RefreshTokenBlacklistService;
import com.example.demo.token.JwtProvider;
import com.example.demo.auth.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenBlacklistService blacklistService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String requestUri = request.getRequestURI();
        // 처리할 logout 경로들 (필요하면 더 추가)
        if (!(requestUri.equals("/api/auth/logout"))) {
            filterChain.doFilter(request, response);
            return;
        }

        // POST만 허용
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 쿠키에서 refreshToken 추출
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("refreshToken".equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }

        System.out.println("logout: "+ refreshToken);

        if (refreshToken == null || refreshToken.isBlank()) {
            sendResponseEntity(LogOutResponseDto.invalidRefreshToken(), response);
            return;
        }

        boolean invalid = false;
        Integer userId = null;

        try {

            userId = jwtProvider.validateRefreshToken(refreshToken);
            if (userId == null) invalid = true;

            if (blacklistService.isBlacklisted(refreshToken)) invalid = true;

            if (userId != null) {
                refreshTokenService.deleteToken(userId);
            }

            // refresh 토큰을 블랙리스트에 넣어 재사용 방지
            Duration remaining = jwtProvider.getRemainingValidity(refreshToken);
            if (remaining != null && !remaining.isZero()) {
                blacklistService.blacklistToken(refreshToken, remaining);
            }
        } catch (Exception ex) {
            // 내부 오류 발생 시 500 에러 응답 (LogOutResponseDto에 databaseError가 없다면 안전한 500 반환)
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(),
                    java.util.Map.of("code", "DBE", "message", "Internal server error"));
            return;
        }

        // invalid 플래그에 따라 응답
        if (!invalid) {
            sendResponseEntity(LogOutResponseDto.success(), response);
            System.out.println("logout success");
        } else {
            sendResponseEntity(LogOutResponseDto.invalidRefreshToken(), response);
            System.out.println("logout unsuccess");

        }
    }

    private void sendResponseEntity(ResponseEntity<?> respEntity, HttpServletResponse response) throws IOException {
        // 상태 코드
        response.setStatus(respEntity.getStatusCodeValue());

        // 헤더 복사 (Set-Cookie 등)
        respEntity.getHeaders().forEach((name, values) -> {
            for (String v : values) {
                response.addHeader(name, v);
            }
        });

        // 바디 직렬화 (있을 경우)
        Object body = respEntity.getBody();
        if (body != null) {
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), body);
            response.getWriter().flush();
        }
    }
}
