package com.example.demo.global.exception;

import lombok.Getter;

/**
 * 비즈니스 규칙 위반 시 발생시키는 최상위 예외.
 * 도메인 서비스에서는 이 예외에 적절한 ErrorCode를 담아 던진다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
