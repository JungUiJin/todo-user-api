# 프로젝트 코딩 컨벤션

## 1. 프레임워크

- Java Spring Boot 최신 버전을 사용한다.

## 2. 의존성 주입

- 반드시 `@RequiredArgsConstructor`와 `private final` 필드를 사용한 **생성자 주입**만 사용한다.
- 필드 주입(`@Autowired` 필드)과 Setter 주입은 사용하지 않는다.

```java
@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserCommandRepository userCommandRepository;
}
```

## 3. 아키텍처 및 네이밍 (CQRS 패턴 지향)

생성·수정·삭제와 조회의 책임을 클래스 단위로 분리한다.

| 구분 | 네이밍 규칙 | 예시 |
| --- | --- | --- |
| 생성 / 수정 / 삭제 | `도메인 + Command + (Controller/Service/Repository)` | `UserCommandController`, `UserCommandService` |
| 조회 | `도메인 + Query + (Controller/Service/Repository)` | `UserQueryController`, `UserQueryService` |

## 4. 언어

- 코드 내 주석, 커밋 메시지, PRD 등 **모든 문서화와 설명은 한국어**로 작성한다.

## 참고 문서

- 제품 요구사항 정의서: [`docs/PRD.md`](docs/PRD.md)
