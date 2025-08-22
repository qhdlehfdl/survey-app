package com.example.demo.auth.dto.response;

import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseDto;
import com.example.demo.common.ResponseMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

public class LogOutResponseDto extends ResponseDto {

    private LogOutResponseDto(){super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);}

    public static ResponseEntity<LogOutResponseDto> success(){
        LogOutResponseDto result = new LogOutResponseDto();

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Strict").build();



        return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, cookie.toString()).body(result);
    }

    public static ResponseEntity<ResponseDto> invalidRefreshToken(){
        ResponseDto result = new ResponseDto( ResponseCode.INVALID_REFRESH_TOKEN, ResponseMessage.INVALID_REFRESH_TOKEN);

        //refresh token 삭제 -> 사실상 로그아웃 완료 처리
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true)
                .path("/api/auth/")
                .maxAge(0)
                .sameSite("Strict").build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).header(HttpHeaders.SET_COOKIE, cookie.toString()).body(result);
    }
}
