package com.example.demo.handler;

import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseDto;
import com.example.demo.common.ResponseMessage;
import com.example.demo.common.ResponseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Qualifier("LoginFailureHandler")
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        ResponseDto dto = new ResponseDto(ResponseCode.SIGN_IN_FAIL, ResponseMessage.SIGN_IN_FAIL);

        ResponseEntity<ResponseDto> responseEntity = ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(dto);

        // ResponseEntity를 HttpServletResponse로 변환 후 전송
        ResponseWriter.writeResponseEntity(responseEntity, response, objectMapper);
    }

}
