package com.example.demo.handler;

import com.example.demo.auth.service.RefreshTokenService;
import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseMessage;
import com.example.demo.token.JwtProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
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
    private final JwtProvider jwtProvider;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        try {

            String refreshToken = null;

            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if("refreshToken".equals(cookie.getName())){
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }


            if(refreshToken == null) return;

            Boolean isValid = jwtProvider.validateJwt(refreshToken, false);
            if(!isValid) return;

            Integer userId = jwtProvider.getSubject(refreshToken);

            refreshTokenService.deleteToken(userId);

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

            ObjectMapper objectMapper = new ObjectMapper();

            String json = objectMapper.writeValueAsString(responseBody);
            response.getWriter().write(json);
            response.getWriter().flush();

        } catch (IOException e) {
            throw new RuntimeException("Failed to read refresh token");
        }
    }
}
