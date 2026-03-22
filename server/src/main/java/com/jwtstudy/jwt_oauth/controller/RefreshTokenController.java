package com.jwtstudy.jwt_oauth.controller;

import com.jwtstudy.jwt_oauth.dto.ReissueResponseDto;
import com.jwtstudy.jwt_oauth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefreshTokenController {
    private final RefreshTokenService refreshTokenService;
    
    @PostMapping("/auth/reissue")
    public ResponseEntity<?> reissue(@CookieValue(name="refreshToken") String refreshToken) {
        System.out.println("reissue:"+refreshToken);
        ReissueResponseDto response = refreshTokenService.reissue(refreshToken);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
