package com.example.demo.auth.service;

import com.example.demo.auth.dto.response.CustomUserDetails;
import com.example.demo.auth.entity.SocialType;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //oauth 로그인
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        Map<String, Object> attributes;
        String username ;
        String email;
        String nickname;

        if(registrationId.equals(SocialType.KAKAO.name())){
            attributes = oAuth2User.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            username = registrationId + "-" + attributes.get("id").toString();
            email = (String) kakaoAccount.get("email");
            nickname = (String) profile.get("nickname");
        }else{
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 입니다.");
        }

        User user = userRepository.findByUsername(username);

        if (user == null) {
            user = new User(username,email,nickname, SocialType.valueOf(registrationId));
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        }else{
            user.setNickname(nickname);
            user.setEmail(email);
        }
        userRepository.save(user);

        return new CustomUserDetails(user, attributes);
    }
}
