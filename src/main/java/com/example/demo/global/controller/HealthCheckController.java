package com.example.demo.global.controller;

import com.example.demo.global.dto.HealthCheckResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
public class HealthCheckController {

    @Operation(summary = "헬스 체크", description = "서버가 정상 동작 중이면 status=UP을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "서버 정상 동작 중")
    @GetMapping("/health")
    public ResponseEntity<HealthCheckResponse> health() {
        return ResponseEntity.ok(HealthCheckResponse.up());
    }
}
