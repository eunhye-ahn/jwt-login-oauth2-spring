package com.jwtstudy.jwt_oauth.jwt;

import com.jwtstudy.jwt_oauth.domain.Role;
import com.jwtstudy.jwt_oauth.exception.CustomException;
import com.jwtstudy.jwt_oauth.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * [WHAT] JWT 토큰 생성 / 검증 / 파싱 담당 컴포넌트
 * [WHY] 토큰 관련 로직을 한 곳에서 관리하기 위해 분리
 *          JwtFilter, UserService 등에서 주입받아 사용
 */

@Component
@RequiredArgsConstructor
public class JwtProvider {

    //[WHAT] application.yaml에서 비밀키, AT/RT 만료시간 주입
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    // [WHAT] 문자열 비밀키 → JWT 서명용 키 객체로 변환
    // [WHY]  JJWT 라이브러리는 SecretKey 타입을 요구함
    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    };

    // [WHAT] Access Token 생성
    // [WHY]  subject : userId로 유저 구분
    //        claim : role 권한 정보 포함
    //        만료시간 : 현재시간 + accessExpiration
    public String generateAccessToken(Long userId, Role role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role",role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+accessExpiration))
                .signWith(getSigningKey())  //서명-위조 방지
                .compact();                 //최종 토큰 문자열 반환
    }

    // [WHAT] Refresh Token 생성
    // [WHY]  AT 재발급용이므로 role 정보 불필요
    //        subject(userId)와 만료시간만 포함
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // [WHAT] AT 파싱 → Claims(payload) 반환
    // [WHY]  서명 검증 + 만료 여부를 파싱 시점에 함께 처리
    //        ExpiredJwtException : 만료된 토큰 → EXPIRED_ACCESS_TOKEN
    //        JwtException        : 위변조 등 그 외 → INVALID_TOKEN
    private Claims parseClaims(String token){
        try{
            return Jwts.parser()
                    .verifyWith(getSigningKey()) //서명검증
                    .build()
                    .parseSignedClaims(token)  //토큰파싱
                    .getPayload();              //데이터 반환
        }catch (ExpiredJwtException e){
            throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        }catch (JwtException e){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    // [WHAT] RT 파싱 → Claims(payload) 반환
    // [WHY]  AT와 만료 에러코드가 다름 (EXPIRED_REFRESH_TOKEN)
    //        별도 메서드로 분리해서 에러코드 구분
    private Claims parseRefreshClaims(String token){
        try{
            return Jwts.parser()
                    .verifyWith(getSigningKey()) //서명검증
                    .build()
                    .parseSignedClaims(token)  //토큰파싱
                    .getPayload();              //데이터 반환
        }catch (ExpiredJwtException e){
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }catch (JwtException e){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }


    // [WHAT] AT 유효성 검증
    // [WHY]  null 체크 후 parseClaims 호출
    //        parseClaims 내부에서 서명 위변조 / 만료 여부까지 검증
    public void validateToken(String token) {
        if (token == null) throw new CustomException(ErrorCode.INVALID_TOKEN);
        parseClaims(token);
    }

    // [WHAT] RT 유효성 검증
    // [WHY]  AT와 동일한 구조, RT 전용 파싱 메서드 사용
    public void validateRefreshToken(String token) {
        if(token == null) throw new CustomException(ErrorCode.INVALID_TOKEN);
        parseRefreshClaims(token);
    }


    // [WHAT] AT의 subject(userId) 추출
    public Long getUserIdFromToken(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }
    // [WHAT] AT의 커스텀 클레임에서 role 추출
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // [WHAT] RT 만료시간을 LocalDateTime으로 반환
    // [WHY]  DB에 RT 저장 시 만료시간 컬럼에 넣기 위해
    //        refreshExpiration은 ms 단위라 /1000으로 초 변환
    public LocalDateTime getRefreshTokenExpiry(){
        return LocalDateTime.now().plusSeconds(refreshExpiration/1000);
    }
}
