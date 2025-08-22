package com.example.demo.auth.controller;

import com.example.demo.auth.dto.request.SignInRequestDto;
import com.example.demo.auth.dto.request.SignUpRequestDto;
import com.example.demo.auth.dto.response.*;
import com.example.demo.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthCotroller {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<? super SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto dto) {
        ResponseEntity<? super SignUpResponseDto> response = authService.signUp(dto);
        return response;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<? super SignInResponseDto> signIn(@Valid @RequestBody SignInRequestDto dto) {
        ResponseEntity<? super SignInResponseDto> response = authService.signIn(dto);
        return response;
    }

    @PostMapping("/refresh")
    public ResponseEntity<? super RefreshTokenResponseDto> refreshToken(HttpServletRequest request) {
        ResponseEntity<? super RefreshTokenResponseDto> response = authService.refreshToken(request);
        return response;
    }

    @PostMapping("/logout")
    public ResponseEntity<? super LogOutResponseDto> logout(HttpServletRequest request) {
        ResponseEntity<? super LogOutResponseDto> result = authService.logout(request);
        return result;
    }

    @GetMapping("/profile")
    public ResponseEntity<? super GetMyProfileResponseDto> getMyProfile(@AuthenticationPrincipal Integer userId, @RequestParam(required = false) Integer lastId, @RequestParam(required = false, defaultValue = "5") Integer size){
        ResponseEntity<? super GetMyProfileResponseDto> result = authService.getMyProfile(userId, lastId, size);
        return result;
    }
}
