package com.example.demo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화 설정.
 * 메인 클래스가 아닌 별도 설정 클래스로 분리하여 @WebMvcTest 등 슬라이스 테스트에서 JPA 빈 누락 오류를 방지한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
