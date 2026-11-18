package com.example.demo.global.format;

/**
 * API 응답에서 사용하는 시간 형식 상수.
 * 어노테이션 속성에서 사용할 수 있도록 컴파일 타임 상수로 정의한다.
 */
public final class DateTimePattern {

    // ISO-8601 로컬 일시 (예: 2026-10-17T10:00:00)
    public static final String LOCAL_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";

    private DateTimePattern() {
    }
}
