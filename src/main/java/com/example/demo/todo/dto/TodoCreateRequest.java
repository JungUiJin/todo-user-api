package com.example.demo.todo.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 할 일 등록 요청.
 */
@Schema(description = "할 일 등록 요청")
public record TodoCreateRequest(
        @Schema(description = "할 일 제목", example = "PRD 작성", maxLength = 100)
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @Schema(description = "상세 설명 (선택)", example = "마일스톤 5단계 문서화", maxLength = 1000, nullable = true)
        @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
        String description,

        @Schema(description = "마감일 (선택, yyyy-MM-dd)", example = "2026-10-20", nullable = true)
        LocalDate dueDate
) {
}
