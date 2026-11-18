package com.example.demo.global.dto;

import java.util.List;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

/**
 * 목록 조회 공통 응답.
 * Spring Data의 PageImpl을 그대로 직렬화하면 응답 구조가 보장되지 않으므로 필요한 필드만 노출한다.
 */
@Schema(description = "페이지 단위 목록 응답")
public record PageResponse<T>(
        @Schema(description = "현재 페이지의 데이터 목록")
        List<T> content,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "전체 데이터 수", example = "1")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "1")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNext
) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
