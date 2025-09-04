package com.example.demo.auth.service;

import com.example.demo.auth.dto.response.CustomOAuth2User;
import com.example.demo.auth.entity.RoleType;
import com.example.demo.auth.entity.SocialType;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService extends DefaultOAuth2UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUM = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    //자체 로그인
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndIsLockAndIsSocial(username, false, false)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoleType().name())
                .accountLocked(user.getIsLock())
                .build();
    }

    //소셜 로그인
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes;
        List<GrantedAuthority> authorities;

        String username;
        String role = RoleType.USER.name();
        String email;
        String nickname;
        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        if(registrationId.equals(SocialType.KAKAO.name())){
            attributes = oAuth2User.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            username = registrationId + "_" + attributes.get("id").toString();
            email = (String) kakaoAccount.get("email");
            nickname = (String) profile.get("nickname");
        }else{
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 입니다.");
        }

        User user = userRepository.findByUsername(username);

        if (user == null) {
            int attempts = 0;
            final int MAX_TRIES = 5;
            while (true) {
                attempts++;
                String candidateNickname = nickname + "_" + randomSuffix(RANDOM.nextInt(10)); // 10글자 이하
                user = new User(username, email, candidateNickname, SocialType.valueOf(registrationId));
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

                try {
                    userRepository.save(user);
                    break; // 성공
                } catch (DataIntegrityViolationException ex) {
                    // 닉네임 unique 제약에 걸림(매우 드묾). 재시도 또는 실패 처리
                    if (attempts >= MAX_TRIES)
                        throw ex;
                }
            }
        }else{
            if (email != null && !email.equals(user.getEmail()))
                user.setEmail(email);
        }
        userRepository.save(user);

        authorities = List.of(new SimpleGrantedAuthority(role));

        return new CustomOAuth2User(attributes, authorities, username);
    }

    private String randomSuffix(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUM.charAt(RANDOM.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}
