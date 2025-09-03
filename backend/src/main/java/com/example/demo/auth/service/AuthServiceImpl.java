package com.example.demo.auth.service;

import com.example.demo.auth.dto.request.SignInRequestDto;
import com.example.demo.auth.dto.request.SignUpRequestDto;
import com.example.demo.auth.dto.response.*;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.survey.dto.response.SurveySimpleResponseDto;
import com.example.demo.survey.service.SurveyService;
import com.example.demo.token.JwtProvider;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenBlacklistService blacklistService;
    private final SurveyService surveyService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto) {

        try {
            boolean existedUserId = userRepository.existsByUsername(dto.getUserId());
            if(existedUserId) return SignUpResponseDto.duplicateId();

            boolean existedNickname = userRepository.existsByNickname(dto.getNickname());
            if(existedNickname) return SignUpResponseDto.duplicateNickname();

            boolean existedEmail = userRepository.existsByEmail(dto.getEmail());
            if(existedEmail) return SignUpResponseDto.duplicateEmail();

            String encodedPassword = passwordEncoder.encode(dto.getPassword());

            User user = new User(dto, encodedPassword);
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            return SignUpResponseDto.databaseError();
        }

        return SignUpResponseDto.success();
    }

    @Override
    public ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto) {

        String accessToken = null;
        String refreshToken = null;

        try {
            String userId = dto.getUserId();
            User user = userRepository.findByUsername(userId);
            if(user == null) return SignInResponseDto.signInFail();

            String password = dto.getPassword();
            String encodedPassword = user.getPassword();

            boolean isMatched = passwordEncoder.matches(password, encodedPassword);
            if(!isMatched) return SignInResponseDto.signInFail();

            Integer id = user.getId();
            String username = user.getUsername();
            String role = user.getRoleType().name();

            accessToken = jwtProvider.createJwt(id, username, role, true);
            refreshToken = jwtProvider.createJwt(id, username, role, false);

            refreshTokenService.saveToken(id, refreshToken);
        } catch (Exception e) {
            e.printStackTrace();
            return SignInResponseDto.databaseError();
        }

        return SignInResponseDto.success(accessToken, refreshToken);
    }

    @Override
    public ResponseEntity<? super RefreshTokenResponseDto> refreshToken(HttpServletRequest request) {

        String refreshToken = null;

        if(request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        System.out.println("refreshToken : " + refreshToken);

        if (refreshToken == null) return RefreshTokenResponseDto.invalidRefreshToken();

        Boolean isValid;
        try {
            isValid = jwtProvider.validateJwt(refreshToken, false);

        } catch (ExpiredJwtException e) {
            e.printStackTrace();
            return RefreshTokenResponseDto.expiredRefreshToken();
        } catch (Exception e) {
            e.printStackTrace();
            return RefreshTokenResponseDto.invalidRefreshToken();
        }


        if (!isValid) return RefreshTokenResponseDto.invalidRefreshToken();

        Integer userId = jwtProvider.getSubject(refreshToken);
        String username = jwtProvider.getUsername(refreshToken);
        String role = jwtProvider.getRole(refreshToken);

        //redis에서 refresh token 가져옴
        Optional<String> refreshTokenOpt = refreshTokenService.getToken(userId);
        if(refreshTokenOpt.isEmpty()) return RefreshTokenResponseDto.invalidRefreshToken();

        String storedRefreshToken = refreshTokenOpt.get();

        if(!storedRefreshToken.equals(refreshToken)) return RefreshTokenResponseDto.invalidRefreshToken();

        //이미 사용된 토큰
        if (blacklistService.isBlacklisted(refreshToken)) return RefreshTokenResponseDto.invalidRefreshToken();

        System.out.println("storedRefreshToken : "+storedRefreshToken);

        String newAccessToken = jwtProvider.createJwt(userId, username, role, true);
        String newRefreshToken = jwtProvider.createJwt(userId, username ,role, false);

        //블랙리스트에 현재 refresh token 추가
        Duration remaining = jwtProvider.getRemainingValidity(refreshToken);
        blacklistService.blacklistToken(refreshToken, remaining);

        refreshTokenService.saveToken(userId, newRefreshToken);

        return RefreshTokenResponseDto.success(newAccessToken, newRefreshToken);
    }

    @Override
    public ResponseEntity<? super LogOutResponseDto> logout(HttpServletRequest request) {

        boolean invalid = false;
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if(cookie.getName().equals("refreshToken")){
                    refreshToken = cookie. getValue();
                    break;
                }
            }
        }

        if(refreshToken == null) invalid = true;

        Integer userId = null;
        Boolean isValid;
        try{
            isValid = jwtProvider.validateJwt(refreshToken, false);

            if(!isValid) invalid = true;

            //이미 그 전에 로그아웃한 경우 -> 블랙리스트에 refresh token 들어가 있음
            if (blacklistService.isBlacklisted(refreshToken)) invalid = true;

            //redis에서 refresh token 삭제
            refreshTokenService.deleteToken(userId);

            //블랙리스트에 refresh token 추가
            Duration remaining = jwtProvider.getRemainingValidity(refreshToken);
            if (!remaining.isZero()) {
                blacklistService.blacklistToken(refreshToken, remaining);
            }
        }catch (Exception e){
            e.printStackTrace();
            return LogOutResponseDto.databaseError();
        }

        if(!invalid) return LogOutResponseDto.success();
        else return LogOutResponseDto.invalidRefreshToken();
    }

    @Override
    public ResponseEntity<? super GetMyProfileResponseDto> getMyProfile(Integer userId, Integer lastId, Integer size) {

        try {

            Optional<User> userOpt = userRepository.findById(userId);
            if(userOpt.isEmpty()) return GetMyProfileResponseDto.notExistedUser();
            String nickname = userOpt.get().getNickname();

            int fetchSize = (size == null || size <= 0) ? 5 : size;

            Integer effectiveLastId = (lastId == null || lastId <= 0) ? null : lastId;

            //fetchSize+1 -> 한개 더 가져와서 hasNext 판정
            List<SurveySimpleResponseDto> surveys = surveyService.getSurveyListByUser(userId, effectiveLastId, fetchSize+1);

            boolean hasNext = false;
            Integer nextCursor = null;

            if(surveys.size() > fetchSize){
                hasNext = true;
                nextCursor = surveys.get(fetchSize).getId(); //마지막 survey id
                surveys = surveys.subList(0,fetchSize);
            }

            return GetMyProfileResponseDto.success(nickname, surveys, hasNext, nextCursor);
        } catch (Exception e) {
            e.printStackTrace();
            return GetMyProfileResponseDto.databaseError();
        }
    }
}
