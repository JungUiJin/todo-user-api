package com.example.demo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 사용자 정보 수정 요청.
 */
@Schema(description = "사용자 정보 수정 요청")
public record UserUpdateRequest(
        @Schema(description = "변경할 사용자 이름", example = "김철수", maxLength = 50)
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
        String name
) {
}
