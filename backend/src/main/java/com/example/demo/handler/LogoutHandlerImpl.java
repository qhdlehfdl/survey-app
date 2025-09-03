package com.example.demo.handler;

import com.example.demo.auth.service.RefreshTokenService;
import com.example.demo.token.JwtProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

        } catch (IOException e) {
            throw new RuntimeException("Failed to read refresh token");
        }
    }
}
