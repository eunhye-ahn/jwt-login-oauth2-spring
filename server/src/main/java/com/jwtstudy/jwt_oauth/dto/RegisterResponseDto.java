package com.jwtstudy.jwt_oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponseDto {
    private String accessToken;
    private String refreshToken;
}
