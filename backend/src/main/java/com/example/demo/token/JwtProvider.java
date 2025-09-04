package com.example.demo.token;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${secret-key}")
    private String secretKey;

    public String createJwt(Integer Id, String username, String role, Boolean isAccess){

        Date expiredDate = isAccess ? Date.from(Instant.now().plus(20, ChronoUnit.MINUTES)) : Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        String type = isAccess ? "access" : "refresh";

        String jwt = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .setSubject(Id.toString())
                .claim("role", role)
                .claim("type", type)
                .setIssuedAt(new Date()).setExpiration(expiredDate)
                .compact();

        return jwt;
    }

    public Boolean validateJwt(String jwt, Boolean isAccess){
        Claims claims = null;

        try{
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt).getBody();

            String type = claims.get("type", String.class);
            if(type == null) return false;

            if(isAccess && !type.equals("access")) return false;
            if(!isAccess && !type.equals("refresh")) return false;

            return true;
        }catch(ExpiredJwtException e){
            e.printStackTrace();
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().get("sub", String.class);
    }

    public String getRole(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().get("role", String.class);
    }

    public Integer getSubject(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Integer.parseInt(claims.getSubject());
    }

    //refresh 토큰 남은 시간 리턴
    public Duration getRemainingValidity(String refreshToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        Date exp = claims.getExpiration();
        long seconds = Duration.between(Instant.now(), exp.toInstant()).getSeconds();

        return Duration.ofSeconds(Math.max(seconds, 0));
    }
}
