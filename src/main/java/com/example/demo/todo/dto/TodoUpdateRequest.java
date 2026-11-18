package com.example.demo.todo.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 할 일 수정 요청 (부분 수정).
 * 전달하지 않은(null) 필드는 기존 값을 유지한다.
 */
@Schema(description = "할 일 수정 요청 (부분 수정). 생략하거나 null인 필드는 기존 값을 유지하며, 값을 비우는 기능은 지원하지 않습니다.")
public record TodoUpdateRequest(
        // null은 허용하되, 값이 있으면 공백만으로 이루어질 수 없다
        @Schema(description = "변경할 제목 (생략 시 유지, 공백 불가)", example = "PRD 최종 검토", maxLength = 100, nullable = true)
        @Pattern(regexp = ".*\\S.*", message = "제목은 공백일 수 없습니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @Schema(description = "변경할 설명 (생략 시 유지)", example = "Swagger 문서 점검 포함", maxLength = 1000, nullable = true)
        @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
        String description,

        @Schema(description = "변경할 마감일 (생략 시 유지, yyyy-MM-dd)", example = "2026-10-25", nullable = true)
        LocalDate dueDate
) {
}
