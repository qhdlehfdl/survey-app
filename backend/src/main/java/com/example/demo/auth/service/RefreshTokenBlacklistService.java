package com.example.demo.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenBlacklistService {

    //블랙리스트? -> token rotation을 통해 만료기간은 남아있지만 사용안되는 토큰 악용되지 않게
    private static final String BLACKLIST_PREFIX = "blacklist:refresh:";

    private final StringRedisTemplate redisTemplate;

    public void blacklistToken(String token, Duration ttl) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "true", ttl);
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX+token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
