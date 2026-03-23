package com.jwtstudy.jwt_oauth.oauth2;

import com.jwtstudy.jwt_oauth.domain.RefreshToken;
import com.jwtstudy.jwt_oauth.domain.User;
import com.jwtstudy.jwt_oauth.exception.CustomException;
import com.jwtstudy.jwt_oauth.exception.ErrorCode;
import com.jwtstudy.jwt_oauth.jwt.JwtProvider;
import com.jwtstudy.jwt_oauth.repository.RefreshTokenRepository;
import com.jwtstudy.jwt_oauth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * [WHAT] OAuth2 로그인 성공 후 처리하는 핸들러
 *
 *              loadUser()에서 DB저장까지만 했고,
 *                       JWT발급은 여기서
 *
 * [흐름] 로그인성공 > principal에서 email꺼내기
 *          > email로 db조회 > jwt발급 > response 헤더에 토큰 담기, 쿠키 세팅
 */
@Component
@RequiredArgsConstructor
public class SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication)
            throws IOException, ServletException {
        //구글 유저 정보로 db 유저 정보 꺼내기
        /**
         * 시큐리티가 내부적으로 authentication에 구글이 준 유저 정보를 넣는다
         *      -> authentication.getPrincipal() 타입 -> Object
         *      -> getAttribute 메서드로 유저 정보를 꺼내기 위해 OAuth2User로 변환
         *

         */
        OAuth2User oAuth2User =  (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        System.out.println(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //그 정보 넣어서 AT,RT발급하기 + 재발급 시 덮어쓰기 로직 추가
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUser(user)
                .map(existing -> existing.updateToken(refreshToken,jwtProvider.getRefreshTokenExpiry()))
                .orElse(RefreshToken.build(user,refreshToken,jwtProvider.getRefreshTokenExpiry()));
        refreshTokenRepository.save(refreshTokenEntity);

        /**
         * [WHY] OAuth2는 리다이렉트로 동작
         *      리다이렉트 : fetch와 달리 헤더와 바디 없이 주소만 바꿔주는 것 (302)
         *      -> 헤더를 JS가 읽을 수 없어서 AT전달 불가
         *      -> 대신 쿼리파라미터에 AT를 실어서 리액트로 전달
         * [주의] RT는 HttpOnly 쿠키에 담겨있어서 자동으로 전달됨
         */
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60*60*24*7);
        response.addCookie(cookie);

        response.sendRedirect("http://localhost:5173/callback?accessToken=" + accessToken);

    }
}
