package com.example.demo.handler;

import com.example.demo.auth.dto.response.SignInResponseDto;
import com.example.demo.auth.service.RefreshTokenService;
import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseMessage;
import com.example.demo.token.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@Qualifier("LoginSuccessHandler")
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        Integer userId = (Integer) authentication.getPrincipal();
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        String accessToken = jwtProvider.createJwt(userId, username, role, true);
        String refreshToken = jwtProvider.createJwt(userId, username, role, false);

        refreshTokenService.saveToken(userId, refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(Duration.ofDays(30))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, Object> body = new HashMap<>();
        body.put("code", ResponseCode.SUCCESS);
        body.put("message", ResponseMessage.SUCCESS);
        body.put("token", accessToken);
        body.put("expirationTime", 1200);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");

        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);
        response.getWriter().flush();

    }
}
