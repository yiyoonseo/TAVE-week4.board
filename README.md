# 게시판 API 서버

게시글 CRUD API에 **Spring Cache**를 적용하고, 실행 환경(local / prod)에 따라 DB·캐시·헬스체크 정책이 바뀌도록 구성한 Spring Boot 프로젝트입니다.

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Java [버전] |
| Framework | Spring Boot [버전], Spring Web, Validation |
| ORM | Spring Data JPA (JPA Auditing) |
| DB | H2 (local) / MySQL 8 (prod) |
| Cache | Caffeine (local) / Redis 7 (prod) |
| Monitoring | Spring Boot Actuator |
| Build | Gradle |

## 프로젝트 구조

```
com.tave_week4.board
 ├─ BoardApplication
 ├─ global
 │   ├─ config       CacheConfig, JpaConfig
 │   ├─ entity       BaseTimeEntity (생성·수정 시각 자동 기록)
 │   └─ exception    PostNotFoundException, GlobalExceptionHandler, ErrorResponse
 └─ post
     ├─ controller   PostController
     ├─ service      PostService (캐시 적용)
     ├─ repository   PostRepository
     ├─ domain       Post
     └─ dto          PostCreateRequest, PostUpdateRequest, PostResponse, PostSummaryResponse
```

## API 명세

| Method | URL | 설명 | 성공 응답 | 캐시 |
| --- | --- | --- | --- | --- |
| POST | `/api/posts` | 게시글 작성 | 201 Created | - |
| GET | `/api/posts?page=0&size=10` | 목록 조회 (최신순) | 200 OK | - |
| GET | `/api/posts/{id}` | 단건 조회 | 200 OK | `@Cacheable` |
| PUT | `/api/posts/{id}` | 게시글 수정 | 200 OK | `@CacheEvict` |
| DELETE | `/api/posts/{id}` | 게시글 삭제 | 204 No Content | `@CacheEvict` |

**에러 응답**

| 상황 | 상태 코드 | code |
| --- | --- | --- |
| 존재하지 않는 게시글 | 404 | `POST_NOT_FOUND` |
| 필수값 누락 / 길이 초과 | 400 | `INVALID_INPUT` |

```json
{ "code": "POST_NOT_FOUND", "message": "게시글을 찾을 수 없습니다. id=99" }
```

## 프로파일별 설정

| 항목 | local | prod |
| --- | --- | --- |
| DB | H2 인메모리 | MySQL (접속 정보는 환경변수) |
| 캐시 | Caffeine (최대 500개, 10분 만료) | Redis (TTL 10분) |
| 헬스체크 상세 | 전체 노출 | 상태만 노출 |
| SQL 로그 | 출력 | 미출력 |

비밀번호 등 민감 정보는 설정 파일에 쓰지 않고 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경변수로 주입합니다.

## 캐싱 전략

조회가 가장 잦고 키가 단순한 **게시글 단건 조회만** 캐시하고, 수정·삭제 시 해당 키를 즉시 무효화합니다.

- **목록은 캐시하지 않음**: 페이지·크기·정렬 조합마다 키가 생기고, 글 하나만 바뀌어도 모든 페이지를 무효화해야 해서 복잡도 대비 효과가 낮다고 판단했습니다.
- **엔티티 대신 DTO를 캐시**: 지연 로딩 프록시와 직렬화 문제를 피하기 위해 `PostResponse`(Serializable)를 저장합니다.
- **수정 시 갱신 대신 삭제**: `@CachePut` 대신 `@CacheEvict`로 지워서, 다음 조회 때 DB의 최신값이 다시 캐시되도록 했습니다.
- **구현체만 교체**: Spring Cache 추상화 덕분에 local(Caffeine)과 prod(Redis)를 바꿔도 서비스 코드는 그대로입니다.

**캐시 동작 확인 로그**

```
# 첫 번째 조회: DB 조회 후 캐시 저장
Creating cache entry for key '1' in cache(s) [post]

# 두 번째 조회: 쿼리 없이 캐시에서 응답
Cache entry for key '1' found in cache(s) [post]
```

## 헬스체크

| 엔드포인트 | 용도 |
| --- | --- |
| `/actuator/health` | 전체 상태 (local: 상세 포함, prod: 상태만) |
| `/actuator/health/liveness` | 애플리케이션이 살아있는지 |
| `/actuator/health/readiness` | 트래픽을 받을 준비가 됐는지 |

local 응답 예시 (일부)

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "H2" } },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

## 실행 방법

**local** (별도 설치 없이 실행)

```bash
./gradlew bootRun
```

**prod** (Docker로 MySQL, Redis 실행 후)

```powershell
docker compose up -d

$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:mysql://localhost:3306/board"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
./gradlew bootRun
```

macOS / Linux에서는 `SPRING_PROFILES_ACTIVE=prod DB_URL=... ./gradlew bootRun` 형태로 실행합니다.

## 트러블슈팅

| 문제 | 원인 | 해결 |
| --- | --- | --- |
| 서비스·리포지토리 빈을 찾지 못함 | `global`, `post` 패키지가 `BoardApplication` 패키지 밖에 있어 컴포넌트 스캔 범위를 벗어남 | 두 패키지를 `com.tave_week4.board` 하위로 이동 |
| `Cannot find cache named 'com/tave_week4/board/post'` | 패키지 이동 리팩터링 시 "문자열에서 검색" 옵션 때문에 `cacheNames = "post"` 문자열까지 경로로 치환됨 | 캐시 이름을 `"post"`로 복구하고, 전체 검색으로 다른 문자열 치환 여부 확인 |
| 없는 글 조회 시 500 에러 | 예외를 처리하는 곳이 없어 서버 오류로 응답됨 | `@RestControllerAdvice`로 404 응답 처리 |
| [겪은 문제] | [원인] | [해결] |

