package com.example.demo.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDto {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 3, max = 20, message = "아이디는 3자 이상 20자 이하로 입력해야 합니다.")
    private String userId;

    @NotBlank(message = "비밀번호 필수 입력 항목입니다.")
    @Size(min = 3, max = 20, message = "비밀번호 3자 이상 20자 이하로 입력해야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 3, max = 20, message = "아이디는 3자 이상 20자 이하로 입력해야 합니다.")
    private String nickname;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String email;
}
