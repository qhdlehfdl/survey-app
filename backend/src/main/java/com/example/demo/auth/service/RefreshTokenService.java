package com.example.demo.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refreshToken:";
    private final StringRedisTemplate redisTemplate;

    public void saveToken(Integer userId, String refreshToken) {
        String key = KEY_PREFIX + userId;

        //만료기간 지나면 저절로 redis에서 삭제
        redisTemplate.opsForValue()
                .set(key, refreshToken, Duration.ofDays(30));
    }

    public Optional<String> getToken(Integer userId) {
        String key = KEY_PREFIX + userId;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void deleteToken(Integer userId){
        String key = KEY_PREFIX+userId;
        redisTemplate.delete(key);
    }
}
