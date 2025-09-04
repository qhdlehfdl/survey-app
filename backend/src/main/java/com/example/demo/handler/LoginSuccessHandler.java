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
@Qualifier("LoginSuccessHandler")
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        Integer userId = userRepository.findByUsername(username).getId();

        String accessToken = jwtProvider.createJwt(userId, username, "ROLE_"+role, true);
        String refreshToken = jwtProvider.createJwt(userId, username, "ROLE_"+role, false);

        refreshTokenService.saveToken(userId, refreshToken);

        ResponseEntity<SignInResponseDto> responseEntity =
                SignInResponseDto.success(accessToken, refreshToken);

        // 5. ResponseEntity를 HttpServletResponse로 변환 후 전송
        ResponseWriter.writeResponseEntity(responseEntity, response, objectMapper);
    }
}
