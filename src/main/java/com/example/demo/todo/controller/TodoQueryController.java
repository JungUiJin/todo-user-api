package com.example.demo.todo.controller;

import com.example.demo.global.dto.PageResponse;
import com.example.demo.global.exception.ErrorResponse;
import com.example.demo.todo.dto.TodoResponse;
import com.example.demo.todo.service.TodoQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 할 일 조회 API.
 */
@Tag(name = "Todo", description = "할 일 관리 API. 사용자가 없으면 USER_NOT_FOUND, 할 일이 없거나 다른 사용자의 할 일이면 TODO_NOT_FOUND를 반환합니다.")
@RestController
@RequestMapping("/api/v1/users/{userId}/todos")
@RequiredArgsConstructor
public class TodoQueryController {

    private final TodoQueryService todoQueryService;

    @Operation(summary = "할 일 단건 조회", description = "사용자의 할 일을 조회합니다. (T-05)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND 또는 TODO_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{todoId}")
    public ResponseEntity<TodoResponse> getTodo(@Parameter(description = "사용자 ID", example = "1")
                                                @PathVariable Long userId,
                                                @Parameter(description = "할 일 ID", example = "3")
                                                @PathVariable Long todoId) {
        return ResponseEntity.ok(todoQueryService.getTodo(userId, todoId));
    }

    @Operation(summary = "할 일 목록 조회",
            description = "사용자의 할 일 목록을 페이지 단위로 조회합니다. completed로 완료 여부를 필터링할 수 있습니다. "
                    + "기본값은 page=0, size=20, id 오름차순이며 size 최대값은 100입니다. (T-06)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT - completed 값이 boolean이 아니거나 잘못된 정렬 필드",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND - 사용자가 존재하지 않음 (빈 목록 대신 404 반환)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "page", in = ParameterIn.QUERY, description = "페이지 번호 (0부터 시작)",
                    schema = @Schema(type = "integer", defaultValue = "0", minimum = "0")),
            @Parameter(name = "size", in = ParameterIn.QUERY, description = "페이지 크기 (최대 100, 초과 시 100으로 제한)",
                    schema = @Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "100")),
            @Parameter(name = "sort", in = ParameterIn.QUERY, description = "정렬 기준. `필드,asc|desc` 형식이며 여러 개 지정할 수 있습니다. (예: id,desc)",
                    array = @ArraySchema(schema = @Schema(type = "string", defaultValue = "id,ASC")))
    })
    @GetMapping
    public ResponseEntity<PageResponse<TodoResponse>> getTodos(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "완료 여부 필터 (생략 시 전체, true: 완료만, false: 미완료만)")
            @RequestParam(required = false) Boolean completed,
            // 페이징 파라미터 문서는 위 @Parameters로 대체한다
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(todoQueryService.getTodos(userId, completed, pageable));
    }
}
