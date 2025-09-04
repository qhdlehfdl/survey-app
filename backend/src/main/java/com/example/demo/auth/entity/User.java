package com.example.demo.auth.entity;

import com.example.demo.auth.dto.request.SignUpRequestDto;
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

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name="is_lock")
    private Boolean isLock;

    @Column(name = "is_social")
    private Boolean isSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type")
    private RoleType roleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type")
    private SocialType socialType;


    public User(SignUpRequestDto dto, String password) {
        this.username = dto.getUserId();
        this.password = password;
        this.nickname = dto.getNickname();
        this.email = dto.getEmail();
        this.isLock = false;
        this.isSocial = false;
        this.roleType = RoleType.USER;
        this.socialType = SocialType.NONE;
    }

    //비밀번호는 외부에서 세팅
    public User(String username, String email, String nickname, SocialType socialType) {
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.isLock = false;
        this.isSocial = true;
        this.roleType = RoleType.USER;
        this.socialType = socialType;
    }
}
