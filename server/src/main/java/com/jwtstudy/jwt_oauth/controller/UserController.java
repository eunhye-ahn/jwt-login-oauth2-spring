package com.jwtstudy.jwt_oauth.controller;

import com.jwtstudy.jwt_oauth.dto.*;
import com.jwtstudy.jwt_oauth.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final HttpServletRequest httpServletRequest;

    @PostMapping("/auth/signUp")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto request, HttpServletResponse httpServletResponse) {
        RegisterResponseDto registerResponse = userService.register(request);

        Cookie cookie = new Cookie("refreshToken", registerResponse.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60*60*24*7);
        httpServletResponse.addCookie(cookie);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registerResponse);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request, HttpServletResponse httpServletResponse){
        LoginResponseDto loginResponse = userService.login(request);

        //브라우저- 쿠키담기
        Cookie cookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60*60*24*7);
        httpServletResponse.addCookie(cookie);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(loginResponse);
    }

    @GetMapping("/user/userInfo")
    public ResponseEntity<?> userInfo(@AuthenticationPrincipal Long userId) {
        UserInfoResponseDto response = userService.getUserInfo(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Long userId,
                                    HttpServletResponse response) {

        //브라우저- 쿠키삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        //db- rt삭제
        userService.logout(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
