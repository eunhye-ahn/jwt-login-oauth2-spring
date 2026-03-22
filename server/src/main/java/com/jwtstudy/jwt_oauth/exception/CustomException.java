package com.jwtstudy.jwt_oauth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

//    public CustomException(ErrorCode errorCode) {
////        super(errorCode.getMessage());
//        this.errorCode = errorCode;
//    }
}
