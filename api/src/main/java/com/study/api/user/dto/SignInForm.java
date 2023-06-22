package com.study.api.user.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class SignInForm {
    @NotBlank(message = "이메일은 필수 값입니다.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$", message = "이메일 형식이 아닙니다.")
    private String userEmail;

    @NotBlank(message = "비밀번호는 필수 값입니다.")
    private String password;
}
