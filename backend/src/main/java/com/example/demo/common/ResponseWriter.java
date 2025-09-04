package com.example.demo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public final class ResponseWriter {
    private ResponseWriter(){}

    public static void writeResponseEntity(ResponseEntity<?> entity,
                                           HttpServletResponse response,
                                           ObjectMapper objectMapper) throws IOException {
        // 헤더(쿠키 포함) 복사
        entity.getHeaders().forEach((name, values) -> values.forEach(v -> response.addHeader(name, v)));
        // 상태 코드
        response.setStatus(entity.getStatusCodeValue());
        // body
        if (entity.getBody() != null) {
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), entity.getBody());
            response.getWriter().flush();
        }
    }
}
