package com.example.demo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 사용자 등록 요청.
 */
@Schema(description = "사용자 등록 요청")
public record UserCreateRequest(
        @Schema(description = "이메일 (중복 불가)", example = "user@example.com", maxLength = 255)
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
        String email,

        @Schema(description = "사용자 이름", example = "홍길동", maxLength = 50)
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
        String name
) {
}
