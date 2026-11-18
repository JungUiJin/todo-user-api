package com.example.demo.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 할 일 완료 상태 변경 요청.
 */
@Schema(description = "할 일 완료 상태 변경 요청")
public record TodoCompletionRequest(
        @Schema(description = "완료 여부 (true: 완료, false: 미완료)", example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "완료 여부는 필수입니다.")
        Boolean completed
) {
}
