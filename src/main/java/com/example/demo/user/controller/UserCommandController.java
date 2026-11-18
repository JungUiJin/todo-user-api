package com.example.demo.user.controller;

import java.net.URI;

import com.example.demo.global.exception.ErrorResponse;
import com.example.demo.user.dto.UserCreateRequest;
import com.example.demo.user.dto.UserResponse;
import com.example.demo.user.dto.UserUpdateRequest;
import com.example.demo.user.service.UserCommandService;
import com.example.demo.user.service.UserQueryService;
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
 * 사용자 생성/수정/삭제 API.
 * 커맨드 실행 후 커밋된 최신 상태를 응답하기 위해 UserQueryService로 결과를 조회한다.
 */
@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserCommandController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @Operation(summary = "사용자 등록", description = "이메일과 이름으로 사용자를 등록합니다. 이메일은 중복될 수 없습니다. (U-01)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공. Location 헤더에 생성된 사용자 URI를 반환합니다."),
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT - 입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "DUPLICATE_EMAIL - 이미 등록된 이메일",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        Long userId = userCommandService.createUser(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + userId))
                .body(userQueryService.getUser(userId));
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자 이름을 수정합니다. (U-02)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT - 입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND - 사용자가 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@Parameter(description = "사용자 ID", example = "1")
                                                   @PathVariable Long userId,
                                                   @Valid @RequestBody UserUpdateRequest request) {
        userCommandService.updateUser(userId, request);
        return ResponseEntity.ok(userQueryService.getUser(userId));
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다. 사용자가 소유한 할 일도 함께 삭제됩니다. (U-03)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND - 사용자가 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "사용자 ID", example = "1")
                                           @PathVariable Long userId) {
        userCommandService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
