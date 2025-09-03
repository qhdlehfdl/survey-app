package com.example.demo.handler;

import com.example.demo.auth.service.RefreshTokenService;
import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseMessage;
import com.example.demo.token.JwtProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class LogoutHandlerImpl implements LogoutHandler {

    private final RefreshTokenService refreshTokenService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        try {
            String body = new BufferedReader(new InputStreamReader(request.getInputStream()))
                    .lines().reduce("", String::concat);

            if(!StringUtils.hasText(body)) return;

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);
            String refreshToken = jsonNode.has("refreshToken") ? jsonNode.get("refreshToken").asText():null;

            if(refreshToken == null) return;

            Boolean isValid = JwtProvider.validateJwt(refreshToken, false);
            if(!isValid) return;

            refreshTokenService.deleteToken((Integer) authentication.getPrincipal());

            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true).secure(true)
                    .path("/api/auth")
                    .maxAge(0)
                    .sameSite("Strict").build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("code", ResponseCode.SUCCESS);
            responseBody.put("message", ResponseMessage.SUCCESS);

            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json;charset=UTF-8");

            String json = objectMapper.writeValueAsString(responseBody);
            response.getWriter().write(json);
            response.getWriter().flush();

        } catch (IOException e) {
            throw new RuntimeException("Failed to read refresh token");
        }
    }
}
