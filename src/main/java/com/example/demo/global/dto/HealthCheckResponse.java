package com.example.demo.global.dto;

import java.time.LocalDateTime;

import com.example.demo.global.format.DateTimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 헬스 체크 응답.
 */
@Schema(description = "헬스 체크 응답")
public record HealthCheckResponse(
        @Schema(description = "서버 상태", example = "UP")
        String status,

        @Schema(description = "응답 일시", type = "string", example = "2026-10-17T10:00:00")
        @JsonFormat(pattern = DateTimePattern.LOCAL_DATE_TIME)
        LocalDateTime timestamp
) {

    public static HealthCheckResponse up() {
        return new HealthCheckResponse("UP", LocalDateTime.now());
    }
}
