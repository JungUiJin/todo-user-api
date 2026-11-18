package com.example.demo.global.config;

import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 문서 기본 정보 설정.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Todo & User API")
                        .description("""
                                사용자(User)와 할 일(Todo)을 관리하는 REST API입니다.

                                - 할 일은 항상 소유 사용자 경로(`/api/v1/users/{userId}/todos`)를 통해 접근합니다.
                                - 모든 일시 필드는 `yyyy-MM-dd'T'HH:mm:ss` 형식의 로컬 시간, 날짜 필드는 `yyyy-MM-dd` 형식입니다.
                                - 에러 응답은 `{ code, message, timestamp }` 공통 형식을 따릅니다.
                                """)
                        .version("v0.1"))
                // 상대 경로로 지정해 Swagger UI에 접속한 호스트 기준으로 API를 호출한다
                .servers(List.of(new Server().url("/").description("현재 접속한 서버")));
    }
}
