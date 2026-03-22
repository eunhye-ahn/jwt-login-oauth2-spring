package com.jwtstudy.jwt_oauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class RegisterRequestDto {
    private String name;

    @NotBlank
    @Email
    //-> methodArgumentNotVaildException 발생
    private String email;

    @NotBlank
    @Pattern(
        regexp="^(?=.*[a-z])(?=.*\\\\d)(?=.*[!@#$%^&*]){8,20}"
    )

    @NotBlank
    private String password;
}
