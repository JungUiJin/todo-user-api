package com.example.demo.user.dto;

import java.time.LocalDateTime;

import com.example.demo.global.format.DateTimePattern;
import com.example.demo.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 응답.
 */
@Schema(description = "사용자 정보")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "생성 일시", type = "string", example = "2026-10-17T10:00:00")
        @JsonFormat(pattern = DateTimePattern.LOCAL_DATE_TIME)
        LocalDateTime createdAt,

        @Schema(description = "수정 일시", type = "string", example = "2026-10-17T10:00:00")
        @JsonFormat(pattern = DateTimePattern.LOCAL_DATE_TIME)
        LocalDateTime updatedAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
