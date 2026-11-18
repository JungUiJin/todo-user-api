package com.example.demo.user.controller;

import com.example.demo.global.dto.PageResponse;
import com.example.demo.global.exception.ErrorResponse;
import com.example.demo.user.dto.UserResponse;
import com.example.demo.user.service.UserQueryService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 조회 API.
 */
@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserQueryService userQueryService;

    @Operation(summary = "사용자 단건 조회", description = "ID로 사용자를 조회합니다. (U-04)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT - 사용자 ID가 숫자가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND - 사용자가 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@Parameter(description = "사용자 ID", example = "1")
                                                @PathVariable Long userId) {
        return ResponseEntity.ok(userQueryService.getUser(userId));
    }

    @Operation(summary = "사용자 목록 조회",
            description = "사용자 목록을 페이지 단위로 조회합니다. 기본값은 page=0, size=20, id 오름차순이며 size 최대값은 100입니다. (U-05)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT - 존재하지 않는 정렬 필드 등 잘못된 파라미터",
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
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            // 페이징 파라미터 문서는 위 @Parameters로 대체한다
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(userQueryService.getUsers(pageable));
    }
}
