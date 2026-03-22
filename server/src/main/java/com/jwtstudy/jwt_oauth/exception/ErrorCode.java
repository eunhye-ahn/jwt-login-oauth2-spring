package com.jwtstudy.jwt_oauth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    //인증실패(401)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다"),
    NOT_FOUND_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없습니다"),

    //user
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "중복된 이메일 입니다"),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.NOT_FOUND, "이메일/비밀번호가 불일치합니다"),

    //공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다"), //id,pw 유효성검사
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    //enum은 lombok 어노테이션이 안먹힘 -> 생성자 직접 정의
    ErrorCode(HttpStatus status, String message){
        this.status = status;
        this.message = message;
    }
}

/**
 * 401 → 토큰 관련 (만료, 없음)
 *       인터셉터가 처리
 *
 * 400 → 잘못된 요청 (비번 틀림, 유효성 실패)
 * 409 → 중복 (이메일 중복)
 * 403 → 권한 없음 => 따로 처리안함
 * 500 → 서버 에러
 *       전부 catch로 넘어가서 호출부에서 처리
 */
