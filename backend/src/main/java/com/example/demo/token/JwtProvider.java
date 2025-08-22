package com.example.demo.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${secret-key}")
    private String accessKey;

    @Value("${refresh-key}")
    private String refreshKey;

    public String createAccessKey(Integer Id){

        Date expiredDate = Date.from(Instant.now().plus(20, ChronoUnit.MINUTES));
        Key key = Keys.hmacShaKeyFor(accessKey.getBytes(StandardCharsets.UTF_8));

        String jwt = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, accessKey)
                .setSubject(Id.toString()).setIssuedAt(new Date()).setExpiration(expiredDate)
                .compact();

        return jwt;
    }

    public String createRefreshKey(Integer Id){

        Date expiredDate = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        Key key = Keys.hmacShaKeyFor(refreshKey.getBytes(StandardCharsets.UTF_8));

        String jwt = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256,refreshKey)
                .setSubject(Id.toString()).setIssuedAt(new Date()).setExpiration(expiredDate)
                .compact();

        return jwt;
    }

    public Integer validateAccessToken(String jwt){
        Claims claims = null;

        try{
            claims = Jwts.parserBuilder()
                    .setSigningKey(accessKey)
                    .build()
                    .parseClaimsJws(jwt).getBody();
        }catch(ExpiredJwtException e){
            e.printStackTrace();
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

        Integer id = Integer.valueOf(claims.getSubject());

        return id;
    }

    public Integer validateRefreshToken(String jwt){
        Claims claims = null;

        try{
            claims = Jwts.parserBuilder()
                    .setSigningKey(refreshKey)
                    .build()
                    .parseClaimsJws(jwt).getBody();
        }catch(ExpiredJwtException e){
            e.printStackTrace();
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

        Integer id = Integer.valueOf(claims.getSubject());

        return id;
    }

    //refresh 토큰 남은 시간 리턴
    public Duration getRemainingValidity(String refreshToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(refreshKey)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        Date exp = claims.getExpiration();
        long seconds = Duration.between(Instant.now(), exp.toInstant()).getSeconds();

        return Duration.ofSeconds(Math.max(seconds, 0));
    }
}
