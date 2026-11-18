package com.example.demo.global.exception;

import java.time.LocalDateTime;

import com.example.demo.global.format.DateTimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공통 에러 응답 형식.
 */
@Schema(description = "공통 에러 응답")
public record ErrorResponse(
        @Schema(description = "에러 코드 (ErrorCode)", example = "USER_NOT_FOUND")
        String code,

        @Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다.")
        String message,

        @Schema(description = "발생 일시", type = "string", example = "2026-10-17T10:00:00")
        @JsonFormat(pattern = DateTimePattern.LOCAL_DATE_TIME)
        LocalDateTime timestamp
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message, LocalDateTime.now());
    }
}
