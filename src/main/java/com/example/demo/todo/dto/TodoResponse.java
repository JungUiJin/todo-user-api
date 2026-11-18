package com.example.demo.todo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.global.format.DateTimePattern;
import com.example.demo.todo.entity.Todo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 할 일 응답.
 */
@Schema(description = "할 일 정보")
public record TodoResponse(
        @Schema(description = "할 일 ID", example = "3")
        Long id,

        @Schema(description = "소유 사용자 ID", example = "1")
        Long userId,

        @Schema(description = "할 일 제목", example = "PRD 작성")
        String title,

        @Schema(description = "상세 설명", example = "마일스톤 5단계 문서화", nullable = true)
        String description,

        @Schema(description = "완료 여부", example = "false")
        boolean completed,

        @Schema(description = "마감일", example = "2026-10-20", nullable = true)
        LocalDate dueDate,

        @Schema(description = "생성 일시", type = "string", example = "2026-10-17T10:00:00")
        @JsonFormat(pattern = DateTimePattern.LOCAL_DATE_TIME)
        LocalDateTime createdAt,

        @Schema(description = "수정 일시", type = "string", example = "2026-10-17T11:00:00")
        @JsonFormat(pattern = DateTimePattern.LOCAL_DATE_TIME)
        LocalDateTime updatedAt
) {

    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                // 지연 로딩 프록시라도 식별자 조회는 추가 쿼리를 발생시키지 않는다
                todo.getUser().getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getDueDate(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}
