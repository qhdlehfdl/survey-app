package com.example.demo.auth.dto.response;

import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseDto;
import com.example.demo.common.ResponseMessage;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Duration;

@Getter
public class RefreshTokenResponseDto extends ResponseDto {

    private String newToken;

    private RefreshTokenResponseDto(String newToken) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.newToken = newToken;
    }

    public static ResponseEntity<RefreshTokenResponseDto> success(String newAccessToken, String newRefreshToken) {
        RefreshTokenResponseDto result = new RefreshTokenResponseDto(newAccessToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true).secure(true)
                .path("/api/auth")
                .maxAge(Duration.ofDays(30))
                .sameSite("Strict").build();

        return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, cookie.toString()).body(result);
    }

    public static ResponseEntity<ResponseDto> invalidRefreshToken(){
        ResponseDto result = new ResponseDto(ResponseCode.INVALID_REFRESH_TOKEN, ResponseMessage.INVALID_REFRESH_TOKEN);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    public static ResponseEntity<ResponseDto> expiredRefreshToken(){
        ResponseDto result = new ResponseDto(ResponseCode.EXPIRED_REFRESH_TOKEN, ResponseMessage.EXPIRED_REFRESH_TOKEN);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
}
