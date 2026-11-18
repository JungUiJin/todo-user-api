# 제품 요구사항 정의서 (PRD)

## Todo & User API 서버

| 항목 | 내용 |
| --- | --- |
| 문서 버전 | v0.1 |
| 작성일 | 2026-10-17 |
| 프레임워크 | Java Spring Boot (최신 버전) |
| 아키텍처 | 계층형 아키텍처 + CQRS 네이밍 컨벤션 |

---

## 1. 개요

### 1.1 목적
사용자(User)가 자신의 할 일(Todo)을 등록·조회·수정·삭제할 수 있는 간단한 REST API 서버를 구축한다.
본 프로젝트는 `CLAUDE.md`에 정의된 코딩 컨벤션(생성자 주입, CQRS 네이밍)을 실제 코드에 적용하는 학습·검증용 프로젝트이다.

### 1.2 범위
- **포함 (In Scope)**
  - 사용자 등록, 조회, 수정, 삭제
  - 사용자별 할 일 등록, 조회, 수정, 완료 처리, 삭제
  - 할 일 목록 필터링(완료 여부) 및 페이지네이션
  - 서버 상태 확인(Health Check)
- **제외 (Out of Scope)**
  - 인증/인가 (JWT, 세션 로그인 등) — 추후 버전에서 검토
  - 프론트엔드 화면
  - 알림, 파일 첨부, 할 일 공유 기능
  - 할 일 수정(T-02) 시 설명·마감일 등 기존 값을 비우는(null로 초기화하는) 기능

---

## 2. 도메인 정의

### 2.1 User (사용자)

| 필드 | 타입 | 제약 조건 | 설명 |
| --- | --- | --- | --- |
| `id` | Long | PK, 자동 증가 | 사용자 식별자 |
| `email` | String | 필수, 고유, 이메일 형식 | 로그인 및 식별용 이메일 |
| `name` | String | 필수, 1~50자 | 사용자 이름 |
| `createdAt` | LocalDateTime | 자동 생성 | 생성 일시 |
| `updatedAt` | LocalDateTime | 자동 갱신 | 수정 일시 |

### 2.2 Todo (할 일)

| 필드 | 타입 | 제약 조건 | 설명 |
| --- | --- | --- | --- |
| `id` | Long | PK, 자동 증가 | 할 일 식별자 |
| `userId` | Long | FK (User), 필수 | 소유 사용자 |
| `title` | String | 필수, 1~100자 | 할 일 제목 |
| `description` | String | 선택, 최대 1000자 | 상세 설명 |
| `completed` | boolean | 기본값 `false` | 완료 여부 |
| `dueDate` | LocalDate | 선택 | 마감일 |
| `createdAt` | LocalDateTime | 자동 생성 | 생성 일시 |
| `updatedAt` | LocalDateTime | 자동 갱신 | 수정 일시 |

### 2.3 연관 관계
- User 1 : N Todo
  - `Todo.user`를 `@ManyToOne(fetch = LAZY)` 단방향으로 매핑한다. (`todos.user_id` FK)
  - User 엔티티는 할 일 컬렉션을 갖지 않는다. 할 일 목록은 항상 페이징 조회(T-06)로 가져온다.
- 사용자 삭제 시 해당 사용자의 할 일도 함께 삭제한다.
  - `CascadeType.REMOVE` 대신 `UserCommandService`에서 단일 JPQL `DELETE`로 할 일을 일괄 삭제한 뒤 사용자를 삭제한다. (할 일 N건을 하나씩 로딩·삭제하는 비용 방지, FK 제약 때문에 할 일을 먼저 삭제)

---

## 3. 기능 요구사항

### 3.1 User

| ID | 요구사항 | 구분 |
| --- | --- | --- |
| U-01 | 이메일과 이름으로 사용자를 등록할 수 있다. 이미 존재하는 이메일이면 등록을 거부한다. | Command |
| U-02 | 사용자 이름을 수정할 수 있다. | Command |
| U-03 | 사용자를 삭제할 수 있다. 소유한 할 일도 함께 삭제된다. | Command |
| U-04 | ID로 사용자 단건을 조회할 수 있다. | Query |
| U-05 | 사용자 목록을 페이지 단위로 조회할 수 있다. | Query |

### 3.2 Todo

| ID | 요구사항 | 구분 |
| --- | --- | --- |
| T-01 | 특정 사용자의 할 일을 등록할 수 있다. 존재하지 않는 사용자면 거부한다. | Command |
| T-02 | 할 일의 제목, 설명, 마감일을 수정할 수 있다. | Command |
| T-03 | 할 일의 완료 상태를 변경(완료/미완료)할 수 있다. | Command |
| T-04 | 할 일을 삭제할 수 있다. | Command |
| T-05 | 할 일 단건을 조회할 수 있다. | Query |
| T-06 | 특정 사용자의 할 일 목록을 조회할 수 있다. 완료 여부로 필터링하고 페이지네이션을 지원한다. | Query |

> **[확정] 404 에러 판별 규칙** — 할 일은 반드시 소유 사용자 경로(`/users/{userId}/todos/...`)를 통해 접근한다.
> - 경로의 사용자가 존재하지 않으면 `404 USER_NOT_FOUND`를 반환한다. (목록 조회도 빈 목록이 아닌 404)
> - 사용자는 존재하지만 할 일이 없거나 다른 사용자의 할 일이면 `404 TODO_NOT_FOUND`를 반환한다.
>
> **[확정] T-02 부분 수정 규칙** — 요청에 포함되지 않거나 `null`인 필드는 무시하고 기존 값을 유지한다. 필드 비우기는 v0.1 범위에서 제외한다. (1.2 범위 참고)
>
> - T-06 목록 조회에서 `completed`를 생략하면 전체, `true`/`false`면 해당 상태만 조회한다. 기본 정렬은 `id` 오름차순이다.

---

## 4. API 엔드포인트 명세

- 기본 경로: `/api/v1`
- 요청/응답 형식: `application/json`
- 시간 형식: 모든 일시 필드는 ISO-8601 로컬 시간 `yyyy-MM-dd'T'HH:mm:ss` (예: `2026-10-17T10:00:00`), 날짜 필드는 `yyyy-MM-dd`를 사용한다.
- 각 엔드포인트는 CQRS 컨벤션에 따라 담당 컨트롤러가 나뉜다.

### 4.1 User API

| 메서드 | URI | 설명 | 성공 응답 | 담당 컨트롤러 | 요구사항 |
| --- | --- | --- | --- | --- | --- |
| `POST` | `/api/v1/users` | 사용자 등록 | `201 Created` | `UserCommandController` | U-01 |
| `PATCH` | `/api/v1/users/{userId}` | 사용자 정보 수정 | `200 OK` | `UserCommandController` | U-02 |
| `DELETE` | `/api/v1/users/{userId}` | 사용자 삭제 | `204 No Content` | `UserCommandController` | U-03 |
| `GET` | `/api/v1/users/{userId}` | 사용자 단건 조회 | `200 OK` | `UserQueryController` | U-04 |
| `GET` | `/api/v1/users?page=0&size=20` | 사용자 목록 조회 | `200 OK` | `UserQueryController` | U-05 |

### 4.2 Todo API

| 메서드 | URI | 설명 | 성공 응답 | 담당 컨트롤러 | 요구사항 |
| --- | --- | --- | --- | --- | --- |
| `POST` | `/api/v1/users/{userId}/todos` | 할 일 등록 | `201 Created` | `TodoCommandController` | T-01 |
| `PATCH` | `/api/v1/users/{userId}/todos/{todoId}` | 할 일 수정 | `200 OK` | `TodoCommandController` | T-02 |
| `PATCH` | `/api/v1/users/{userId}/todos/{todoId}/completion` | 완료 상태 변경 | `200 OK` | `TodoCommandController` | T-03 |
| `DELETE` | `/api/v1/users/{userId}/todos/{todoId}` | 할 일 삭제 | `204 No Content` | `TodoCommandController` | T-04 |
| `GET` | `/api/v1/users/{userId}/todos/{todoId}` | 할 일 단건 조회 | `200 OK` | `TodoQueryController` | T-05 |
| `GET` | `/api/v1/users/{userId}/todos?completed=false&page=0&size=20` | 할 일 목록 조회 | `200 OK` | `TodoQueryController` | T-06 |

### 4.3 기타

| 메서드 | URI | 설명 | 성공 응답 | 담당 컨트롤러 |
| --- | --- | --- | --- | --- |
| `GET` | `/health` | 서버 상태 확인 | `200 OK` | `HealthCheckController` (기존) |

### 4.4 요청/응답 예시

**사용자 등록** — `POST /api/v1/users`
```json
// 요청
{ "email": "user@example.com", "name": "홍길동" }

// 응답 (201 Created, Location: /api/v1/users/1)
{ "id": 1, "email": "user@example.com", "name": "홍길동", "createdAt": "2026-10-17T10:00:00", "updatedAt": "2026-10-17T10:00:00" }
```

**목록 조회 (페이지네이션 공통 형식)** — `GET /api/v1/users?page=0&size=20`
```json
// 응답 (200 OK)
{
  "content": [ { "id": 1, "email": "user@example.com", "name": "홍길동", "createdAt": "2026-10-17T10:00:00", "updatedAt": "2026-10-17T10:00:00" } ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false
}
```
> `size`의 최대값은 100이다. 정렬은 `sort=id,desc` 형식으로 지정하며, 존재하지 않는 필드로 정렬하면 `400 INVALID_INPUT`을 반환한다.

**할 일 완료 상태 변경** — `PATCH /api/v1/users/1/todos/3/completion`
```json
// 요청
{ "completed": true }

// 응답 (200 OK)
{ "id": 3, "title": "PRD 작성", "completed": true, "dueDate": "2026-10-20", "updatedAt": "2026-10-17T11:00:00" }
```

### 4.5 공통 에러 응답

```json
{ "code": "USER_NOT_FOUND", "message": "사용자를 찾을 수 없습니다.", "timestamp": "2026-10-17T10:00:00" }
```

| HTTP 상태 | 에러 코드 | 발생 조건 |
| --- | --- | --- |
| `400 Bad Request` | `INVALID_INPUT` | 요청 값 검증 실패, JSON 파싱 실패, 파라미터 타입 불일치·누락, 잘못된 정렬 필드 |
| `404 Not Found` | `RESOURCE_NOT_FOUND` | 존재하지 않는 API 경로 |
| `404 Not Found` | `USER_NOT_FOUND` | 존재하지 않는 사용자 |
| `404 Not Found` | `TODO_NOT_FOUND` | 존재하지 않거나 다른 사용자의 할 일 |
| `405 Method Not Allowed` | `METHOD_NOT_ALLOWED` | 해당 경로에서 지원하지 않는 HTTP 메서드 |
| `409 Conflict` | `DUPLICATE_EMAIL` | 이미 등록된 이메일 |
| `415 Unsupported Media Type` | `UNSUPPORTED_MEDIA_TYPE` | 요청 본문의 Content-Type이 `application/json`이 아님 |
| `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | 처리되지 않은 서버 내부 예외 |

---

## 5. 비기능 요구사항

| 항목 | 요구사항 |
| --- | --- |
| 의존성 주입 | 모든 빈은 `@RequiredArgsConstructor` + `private final` 생성자 주입만 사용한다. (필드 주입·Setter 주입 금지) |
| CQRS 분리 | 생성/수정/삭제는 `*Command*`, 조회는 `*Query*` 클래스로 분리한다. Query 서비스는 `@Transactional(readOnly = true)`를 적용한다. |
| 시간 형식 | 응답의 모든 `LocalDateTime` 필드에 `@JsonFormat(pattern = DateTimePattern.LOCAL_DATE_TIME)`을 적용해 `yyyy-MM-dd'T'HH:mm:ss` 로컬 시간으로 통일한다. |
| 입력 검증 | Bean Validation(`@Valid`, `@NotBlank`, `@Email` 등)으로 요청 DTO를 검증한다. |
| 예외 처리 | `@RestControllerAdvice` 기반 전역 예외 처리기로 에러 응답 형식을 통일한다. |
| 데이터베이스 | 개발 환경은 H2(인메모리), 운영 환경은 MySQL 또는 PostgreSQL을 사용한다. JPA(Hibernate) 기반으로 한다. |
| 테스트 | Service 단위 테스트와 Controller 슬라이스 테스트(`@WebMvcTest`)를 작성한다. |
| 문서화 | 코드 주석, 커밋 메시지, 문서는 모두 한국어로 작성한다. API 문서는 springdoc-openapi(Swagger UI)로 제공한다. (Swagger UI: `/swagger-ui.html`, OpenAPI 스펙: `/v3/api-docs`) 각 API에는 발생 가능한 에러 응답만 명시한다. |

---

## 6. 디렉토리 구조 (예상)

도메인별로 패키지를 나누고, 각 도메인 안에서 계층(controller/service/repository)마다 Command와 Query 클래스를 분리한다.

```
claude/
├── CLAUDE.md
├── docs/
│   └── PRD.md
├── build.gradle
└── src/
    ├── main/
    │   ├── java/com/example/demo/
    │   │   ├── DemoApplication.java
    │   │   │
    │   │   ├── global/                              # 공통 모듈
    │   │   │   ├── config/
    │   │   │   │   ├── JpaAuditingConfig.java       # JPA Auditing 활성화
    │   │   │   │   └── SwaggerConfig.java           # OpenAPI 문서 기본 정보
    │   │   │   ├── controller/
    │   │   │   │   └── HealthCheckController.java   # (기존) 헬스 체크
    │   │   │   ├── dto/
    │   │   │   │   ├── HealthCheckResponse.java
    │   │   │   │   └── PageResponse.java            # 목록 조회 공통 응답
    │   │   │   ├── format/
    │   │   │   │   └── DateTimePattern.java         # 시간 형식 상수
    │   │   │   ├── entity/
    │   │   │   │   └── BaseTimeEntity.java          # createdAt / updatedAt 공통 필드
    │   │   │   └── exception/
    │   │   │       ├── ErrorCode.java
    │   │   │       ├── ErrorResponse.java
    │   │   │       ├── BusinessException.java
    │   │   │       └── GlobalExceptionHandler.java
    │   │   │
    │   │   ├── user/
    │   │   │   ├── controller/
    │   │   │   │   ├── UserCommandController.java   # POST / PATCH / DELETE
    │   │   │   │   └── UserQueryController.java     # GET
    │   │   │   ├── service/
    │   │   │   │   ├── UserCommandService.java
    │   │   │   │   └── UserQueryService.java
    │   │   │   ├── repository/
    │   │   │   │   ├── UserCommandRepository.java
    │   │   │   │   └── UserQueryRepository.java
    │   │   │   ├── entity/
    │   │   │   │   └── User.java
    │   │   │   └── dto/
    │   │   │       ├── UserCreateRequest.java
    │   │   │       ├── UserUpdateRequest.java
    │   │   │       └── UserResponse.java
    │   │   │
    │   │   └── todo/
    │   │       ├── controller/
    │   │       │   ├── TodoCommandController.java   # POST / PATCH / DELETE
    │   │       │   └── TodoQueryController.java     # GET
    │   │       ├── service/
    │   │       │   ├── TodoCommandService.java
    │   │       │   └── TodoQueryService.java
    │   │       ├── repository/
    │   │       │   ├── TodoCommandRepository.java
    │   │       │   └── TodoQueryRepository.java
    │   │       ├── entity/
    │   │       │   └── Todo.java
    │   │       └── dto/
    │   │           ├── TodoCreateRequest.java
    │   │           ├── TodoUpdateRequest.java
    │   │           ├── TodoCompletionRequest.java
    │   │           └── TodoResponse.java
    │   │
    │   └── resources/
    │       └── application.yml
    │
    └── test/java/com/example/demo/
        ├── user/
        │   ├── controller/
        │   └── service/
        └── todo/
            ├── TodoFixture.java                  # 테스트용 엔티티 생성 헬퍼
            ├── controller/
            ├── repository/                       # @DataJpaTest (H2 실DB 쿼리 검증)
            └── service/
```

### 6.1 CQRS 클래스 책임 정리

| 계층 | Command 클래스 | Query 클래스 |
| --- | --- | --- |
| Controller | `POST`, `PATCH`, `DELETE` 요청 처리 | `GET` 요청 처리 |
| Service | `@Transactional` — 생성·수정·삭제 비즈니스 로직 | `@Transactional(readOnly = true)` — 조회 로직, DTO 변환 |
| Repository | 엔티티 저장·삭제 (`JpaRepository` 상속) | 조회 전용 메서드 (필터링, 페이지네이션 쿼리) |

> **참고:** Command Service에서도 수정·삭제 대상 엔티티를 찾기 위해 조회가 필요하다. 이때는 Command Repository의 `findById`를 사용하고, 화면 응답용 조회는 Query 계층을 사용한다.

---

## 7. 마일스톤

| 단계 | 내용 | 상태 |
| --- | --- | --- |
| 1단계 | 프로젝트 초기 설정 (`build.gradle`, `DemoApplication`, `application.yml`, H2 연동) | 완료 |
| 2단계 | 공통 모듈 (`BaseTimeEntity`, 전역 예외 처리) | 완료 |
| 3단계 | User 도메인 Command/Query 구현 및 테스트 | 완료 |
| 4단계 | Todo 도메인 Command/Query 구현 및 테스트 | 완료 |
| 5단계 | Swagger 문서화 및 전체 API 점검 | 완료 |

---

## 8. 미결 사항

- 인증 도입 시 URI의 `{userId}`를 인증 정보(토큰)로 대체할지 여부
- ~~Query Repository 구현 방식: Spring Data JPA 메서드 쿼리로 충분한지, QueryDSL을 도입할지 여부~~ → QueryDSL 도입으로 확정 (#2). 동적 조건이 필요한 목록 조회는 QueryDSL 커스텀 저장소, 단순 단건 조회는 메서드 쿼리를 유지
- ~~빌드 도구: Gradle과 Maven 중 최종 선택~~ → Gradle로 확정 (1단계에서 `build.gradle` 기반으로 구성)
