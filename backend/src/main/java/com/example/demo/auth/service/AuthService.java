package com.example.demo.auth.service;

import com.example.demo.auth.dto.request.SignInRequestDto;
import com.example.demo.auth.dto.request.SignUpRequestDto;
import com.example.demo.auth.dto.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto);
    ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto);
    ResponseEntity<? super RefreshTokenResponseDto> refreshToken(HttpServletRequest request);
    ResponseEntity<? super LogOutResponseDto> logout(HttpServletRequest request);
    ResponseEntity<? super GetMyProfileResponseDto> getMyProfile(Integer userId, Integer lastId, Integer size);
}
