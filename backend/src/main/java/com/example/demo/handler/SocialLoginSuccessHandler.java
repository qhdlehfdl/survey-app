package com.example.demo.handler;

import com.example.demo.auth.dto.response.SignInResponseDto;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.auth.service.RefreshTokenService;
import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseMessage;
import com.example.demo.common.ResponseWriter;
import com.example.demo.token.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@Qualifier("SocialLoginSuccessHandler")
@RequiredArgsConstructor
public class SocialLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        Integer userId = userRepository.findByUsername(username).getId();

//        String accessToken = jwtProvider.createJwt(userId, username, "ROLE_" + role, true);
        String refreshToken = jwtProvider.createJwt(userId, username, "ROLE_" + role, false);

        refreshTokenService.saveToken(userId, refreshToken);
//
//        ResponseEntity<SignInResponseDto> responseEntity =
//                SignInResponseDto.success(accessToken, refreshToken);
//
//        // 5. ResponseEntity를 HttpServletResponse로 변환 후 전송
//        ResponseWriter.writeResponseEntity(responseEntity, response, objectMapper);

        /* 로그인 성공했는데 여기서 바로 access, refresh발급하고 응답 보내지 않는이유
        -> 바로 응답보내도 되지만 프론트에서 맨처음에 '카카오로 로그인' 눌렀을때 fetch가 아니라
        리디렉션으로 백엔드로 넘김. 그래서 여기에서 응답을 보내도 프론트가 응답이 온지 모르는 상황
        여기서 응답보내도 되지만 그렇게하려면 방식이 좀 짜침
         */


        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/api/auth");
        refreshCookie.setMaxAge(10); //10초 -> 프론트에서 발급 후 헤더 전환 로직 진행 예정

        response.addCookie(refreshCookie);
        response.sendRedirect("http://localhost:3000/cookie");
    }
}
