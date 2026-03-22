package com.jwtstudy.jwt_oauth.service;

import ch.qos.logback.core.spi.ErrorCodes;
import com.jwtstudy.jwt_oauth.domain.RefreshToken;
import com.jwtstudy.jwt_oauth.domain.User;
import com.jwtstudy.jwt_oauth.dto.ReissueResponseDto;
import com.jwtstudy.jwt_oauth.exception.CustomException;
import com.jwtstudy.jwt_oauth.exception.ErrorCode;
import com.jwtstudy.jwt_oauth.jwt.JwtProvider;
import com.jwtstudy.jwt_oauth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    //at 재발급
    public ReissueResponseDto reissue(String refreshTokenStr) {
        //rt 유효성검사
            jwtProvider.validateRefreshToken(String.valueOf(refreshTokenStr));
            //db에서 rt 존재확인
            RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_REFRESH_TOKEN));
            //userId 추출
            User user = refreshToken.getUser();

            //새 at 발급
            String response = jwtProvider.generateAccessToken(user.getId(),user.getRole());

            return new ReissueResponseDto(response);
    }
}
