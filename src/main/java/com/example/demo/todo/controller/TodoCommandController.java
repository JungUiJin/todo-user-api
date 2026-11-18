package com.example.demo.todo.controller;

import java.net.URI;

import com.example.demo.global.exception.ErrorResponse;
import com.example.demo.todo.dto.TodoCompletionRequest;
import com.example.demo.todo.dto.TodoCreateRequest;
import com.example.demo.todo.dto.TodoResponse;
import com.example.demo.todo.dto.TodoUpdateRequest;
import com.example.demo.todo.service.TodoCommandService;
import com.example.demo.todo.service.TodoQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 할 일 생성/수정/삭제 API.
 * 커맨드 실행 후 커밋된 최신 상태를 응답하기 위해 TodoQueryService로 결과를 조회한다.
 */
@Tag(name = "Todo", description = "할 일 관리 API. 사용자가 없으면 USER_NOT_FOUND, 할 일이 없거나 다른 사용자의 할 일이면 TODO_NOT_FOUND를 반환합니다.")
@RestController
@RequestMapping("/api/v1/users/{userId}/todos")
@RequiredArgsConstructor
public class TodoCommandController {

    private final TodoCommandService todoCommandService;
    private final TodoQueryService todoQueryService;

    @Operation(summary = "할 일 등록", description = "사용자의 할 일을 미완료 상태로 등록합니다. (T-01)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공. Location 헤더에 생성된 할 일 URI를 반환합니다."),
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT - 입력값 검증 실패 (제목 누락, 마감일 형식 오류 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND - 사용자가 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(@Parameter(description = "사용자 ID", example = "1")
                                                   @PathVariable Long userId,
                                                   @Valid @RequestBody TodoCreateRequest request) {
        Long todoId = todoCommandService.createTodo(userId, request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + userId + "/todos/" + todoId))
                .body(todoQueryService.getTodo(userId, todoId));
    }

    @Operation(summary = "할 일 수정",
            description = "제목, 설명, 마감일을 부분 수정합니다. 생략하거나 null인 필드는 기존 값을 유지합니다. (T-02)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT - 입력값 검증 실패 (공백 제목 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND 또는 TODO_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(@Parameter(description = "사용자 ID", example = "1")
                                                   @PathVariable Long userId,
                                                   @Parameter(description = "할 일 ID", example = "3")
                                                   @PathVariable Long todoId,
                                                   @Valid @RequestBody TodoUpdateRequest request) {
        todoCommandService.updateTodo(userId, todoId, request);
        return ResponseEntity.ok(todoQueryService.getTodo(userId, todoId));
    }

    @Operation(summary = "할 일 완료 상태 변경", description = "할 일을 완료 또는 미완료 상태로 변경합니다. (T-03)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT - completed 값 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND 또는 TODO_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{todoId}/completion")
    public ResponseEntity<TodoResponse> changeCompletion(@Parameter(description = "사용자 ID", example = "1")
                                                         @PathVariable Long userId,
                                                         @Parameter(description = "할 일 ID", example = "3")
                                                         @PathVariable Long todoId,
                                                         @Valid @RequestBody TodoCompletionRequest request) {
        todoCommandService.changeCompletion(userId, todoId, request);
        return ResponseEntity.ok(todoQueryService.getTodo(userId, todoId));
    }

    @Operation(summary = "할 일 삭제", description = "할 일을 삭제합니다. (T-04)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND 또는 TODO_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(@Parameter(description = "사용자 ID", example = "1")
                                           @PathVariable Long userId,
                                           @Parameter(description = "할 일 ID", example = "3")
                                           @PathVariable Long todoId) {
        todoCommandService.deleteTodo(userId, todoId);
        return ResponseEntity.noContent().build();
    }
}
