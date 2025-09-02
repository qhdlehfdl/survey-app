package com.example.demo.auth.entity;

import com.example.demo.auth.dto.request.SignUpRequestDto;
import com.example.demo.auth.dto.response.SignUpResponseDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name = "is_social")
    private Boolean isSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type")
    private SocialType socialType;

    public enum SocialType{
        NONE, KAKAO
    }

    public User(SignUpRequestDto dto, String password) {
        this.userId = dto.getUserId();
        this.password = password;
        this.nickname = dto.getNickname();
        this.email = dto.getEmail();
    }
}
