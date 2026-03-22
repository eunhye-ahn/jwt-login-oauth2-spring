package com.jwtstudy.jwt_oauth.service;

import com.jwtstudy.jwt_oauth.domain.RefreshToken;
import com.jwtstudy.jwt_oauth.domain.User;
import com.jwtstudy.jwt_oauth.dto.*;
import com.jwtstudy.jwt_oauth.exception.CustomException;
import com.jwtstudy.jwt_oauth.exception.ErrorCode;
import com.jwtstudy.jwt_oauth.jwt.JwtProvider;
import com.jwtstudy.jwt_oauth.repository.RefreshTokenRepository;
import com.jwtstudy.jwt_oauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    //회원가입
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {

        //중복확인
        if(userRepository.existsByEmail(request.getEmail())){
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        //비밀번호 암호화
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtProvider.generateAccessToken(savedUser.getId(),savedUser.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(savedUser.getId());

        refreshTokenRepository.save(RefreshToken.build(user, refreshToken, jwtProvider.getRefreshTokenExpiry()));

        return new RegisterResponseDto(accessToken, refreshToken);
    }

    //로그인
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        //유저조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new CustomException(ErrorCode.INVALID_PASSWORD));

        //비밀번호불일치
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        //일치하면 토큰 발급
        String accessToken = jwtProvider.generateAccessToken(user.getId(),user.getRole());
        //RT 있으면 delte하고 새로 저장
//        if(refreshTokenRepository.existsByUser(user)) {
//            refreshTokenRepository.deleteByUser(user);
//            refreshTokenRepository.flush();
//        }

        //RT 업데이트 발급
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        //같은 유저 RT 덮어쓰기
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUser(user)
                        .map(existing -> existing.updateToken(refreshToken, jwtProvider.getRefreshTokenExpiry()))
                                .orElse(RefreshToken.build(user, refreshToken, jwtProvider.getRefreshTokenExpiry())
                                );
        refreshTokenRepository.save(refreshTokenEntity);


        return new LoginResponseDto(accessToken, refreshToken);
    }

    //로그아웃
    @Transactional
    public void logout(Long userId){
        //리프레시 토큰 삭제
        User user = userRepository.findById(userId).orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));
        refreshTokenRepository.deleteByUser(user);
    }

    //유저조회
    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));
        return new UserInfoResponseDto(user.getEmail(), user.getName());
    }

}
