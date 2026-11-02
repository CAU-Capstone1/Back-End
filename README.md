# Back-End

허밍 기반 AI 작곡 서비스 백엔드

사용자가 허밍(mp3)을 업로드하고 스타일·무드·키·템포를 지정하면, AI 서버(MusicGen)가 생성한 BGM을 저장하고 결과 URL을 제공합니다.

```
클라이언트 ──(JWT)──▶ Spring Boot API ──▶ S3 (허밍·결과 음원)
                          │    ▲
                          │    └──── RDS (사용자·작업 상태·메타데이터)
                          ▼
                   작곡 작업 스레드 풀 ──(WebClient)──▶ AI 서버 (MusicGen)
```

1. 허밍 업로드 → S3 `humming/{userId}/` 에 저장, key 반환
2. 작곡 요청 → Job 생성 후 즉시 `202 QUEUED` 응답, 생성은 백그라운드에서 수행
3. 상태 폴링 → `QUEUED → PROCESSING → SUCCEEDED / FAILED`
4. 완료 시 결과 음원의 Presigned URL 반환

---

## 실행 방법

애플리케이션 코드는 하나이고, **어떤 설정 파일(프로필)을 읽느냐**에 따라 연결 대상이 달라집니다.

| | AWS (운영) | 로컬 |
|---|---|---|
| 설정 파일 | `application.properties` | `application-local.properties` |
| 실행 프로필 | 기본 (지정 안 함) | `local` |
| DB | AWS RDS (MySQL) | MySQL 8.4 컨테이너 |
| 파일 저장소 | AWS S3 | Adobe S3Mock 컨테이너 |
| AI 서버 | Hugging Face Space ([CAU-Capstone1/AI](https://github.com/CAU-Capstone1/AI)) | 같은 AI 저장소를 로컬에서 실행 |
| S3 인증 | EC2 IAM Role | 더미 값 (S3Mock은 검증하지 않음) |

### 1. AWS 환경에서 실행

**필요한 AWS 리소스**

- **RDS (MySQL 8)**: `humming` 데이터베이스
- **S3 버킷**: 서울 리전(`ap-northeast-2`)
- **EC2**: S3 버킷 읽기/쓰기 권한(`s3:GetObject`, `s3:PutObject`)이 있는 IAM Role 연결, Java 21 설치
- **AI 서버**: [CAU-Capstone1/AI](https://github.com/CAU-Capstone1/AI)를 Hugging Face Space(GPU)에 배포 (AWS 외부)

**설정 파일 작성**

`src/main/resources/application.properties.example`을 복사해 `application.properties`를 만들고 `<...>` 값을 채웁니다. 이 파일은 비밀값을 담고 있어 git에서 제외됩니다.

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

| 항목 | 설명 |
|---|---|
| `spring.datasource.*` | RDS 엔드포인트, 계정 |
| `jwt.secret` | Base64 인코딩된 **64바이트 이상** 값 (HS512 서명), 예: `openssl rand -base64 64` |
| `ai.base-url` | AI 서버 주소 |
| `ai.callback-secret` | AI 서버가 `/callback` 호출 시 `X-Callback-Secret` 헤더로 보내는 값 |
| `spring.cloud.aws.s3.bucket` | S3 버킷 이름 |
| `spring.servlet.multipart.max-file-size` | 업로드 최대 용량 (기본 1MB이므로 설정 필요) |

> `spring.jpa.hibernate.ddl-auto=validate` 설정이므로 RDS에 테이블이 미리 있어야 합니다.
> 처음 배포할 때는 한 번만 `update`로 실행해 테이블을 생성한 뒤 `validate`로 되돌립니다.

**빌드 및 실행 (EC2)**

```bash
./gradlew bootJar
java -jar build/libs/HummingService-0.0.1-SNAPSHOT.jar
```

### 2. 로컬 환경에서 실행 (AWS 없이)

DB와 S3는 Docker 컨테이너로 대체하고, AI 서버는 [CAU-Capstone1/AI](https://github.com/CAU-Capstone1/AI) 저장소를 그대로 로컬에서 실행해 연결합니다.

필요한 것: Java 21, Docker, Python 3.10, ffmpeg

```
Spring Boot (localhost:8080, local 프로필)
 ├──▶ MySQL 컨테이너   localhost:3307   ← RDS 대신
 ├──▶ S3Mock 컨테이너  localhost:9090   ← S3 대신
 └──▶ AI 서버          localhost:7860   ← Hugging Face Space 대신 (AI 저장소를 직접 실행)
```

**① AI 서버 실행** (AI 저장소, 별도 터미널)

AI 저장소의 코드는 수정하지 않고 그대로 실행합니다. 아래 두 방법 중 하나를 사용합니다.

```bash
git clone https://github.com/CAU-Capstone1/AI.git
cd AI

# 방법 A: Docker (Hugging Face Space와 같은 환경)
docker build -t humming-ai .
docker run --rm -p 7860:7860 humming-ai

# 방법 B: Python 가상환경
python3.10 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
uvicorn app:app --host 0.0.0.0 --port 7860
```

`curl localhost:7860` 응답이 `MusicGen-Melody API running...` 이면 준비된 것입니다.

> - 첫 작곡 요청 시 MusicGen-melody 모델(수 GB)을 내려받고 로딩하므로 오래 걸립니다.
> - NVIDIA GPU가 없으면 CPU로 생성하며, 한 곡에 수 분이 걸릴 수 있습니다.
> - AI 서버는 요청을 한 번에 하나씩 처리합니다. local 프로필은 이에 맞춰 응답 타임아웃 900초, 동시 생성 1개로 설정되어 있습니다.

**② 인프라 실행 및 백엔드 실행** (Back-End 저장소)

```bash
# MySQL, S3Mock 실행
docker compose up -d

# 백엔드 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

| 서비스 | 주소 |
|---|---|
| API | http://localhost:8080 |
| MySQL | localhost:3307 (humming / humming) |
| S3Mock | http://localhost:9090 (버킷 `humming-local` 자동 생성) |
| AI 서버 | http://localhost:7860 |

종료: AI 서버는 `Ctrl+C`, 인프라는 `docker compose down` (데이터까지 삭제하려면 `docker compose down -v`)

**요청 흐름 예시**

```bash
# 회원가입 → accessToken 발급
curl -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"email":"test@test.com","password":"password1","name":"tester"}'

# 허밍 업로드 → filePath (humming/{userId}/...)
# 실제 녹음한 mp3를 사용해야 합니다 (AI 서버가 ffmpeg로 변환하므로 오디오가 아니면 400)
curl -X POST localhost:8080/api/upload -H "Authorization: Bearer $TOKEN" -F file=@hum.mp3

# 작곡 요청 → jobId (QUEUED로 즉시 응답)
curl -X POST localhost:8080/api/compose -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"hummingMain":"<filePath>","key":"C","tempo":"120","duration":"10","style":"pop","mood":"happy"}'

# 상태 폴링 → SUCCEEDED 시 musicUrl(Presigned URL) 반환
curl localhost:8080/api/job/<jobId> -H "Authorization: Bearer $TOKEN"
```

---

## 테스트

외부 인프라 없이 H2 인메모리 DB와 Mockito로 실행됩니다.

```bash
./gradlew test
```

보안 설정(401/403/404), 로그인 응답의 비밀번호 비노출, 비동기 작곡 처리의 성공·실패 기록, 콜백 시크릿 검증 등을 검증합니다.

---

## API

| Method | Path | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/auth/register` | - | 회원가입 (가입 후 토큰 발급) |
| POST | `/api/auth/login` | - | 로그인 |
| POST | `/api/upload` | JWT | 허밍 mp3 업로드 |
| GET | `/api/audio/signed-url?key=` | JWT | 본인 업로드 파일의 Presigned URL |
| POST | `/api/compose` | JWT | 작곡 요청 (202, 비동기) |
| GET | `/api/job/{jobId}` | JWT | 작업 상태·결과 URL 조회 (본인 작업만) |
| POST | `/callback` | 시크릿 헤더 | AI 서버 → 백엔드 진행 상태 콜백 |

**작곡 요청 제한**

- 길이(`duration`): 1~30초 (범위 밖이면 400)
- 사용자당 동시 진행 작업: 최대 2개 (초과 시 429)
- 전체 대기열이 가득 차면 503
- 진행률(`progress`): 요청 시작 10 → 음원 생성 완료 80 → 저장 완료 100
- 서버가 재시작되면 중단된 작업은 `FAILED`로 정리되며, 다시 요청해야 합니다.

---

## 기술 스택

**Backend**

| 구분 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Build | Gradle 8.14 |
| 인증 | Spring Security, JWT (jjwt 0.11.5, HS512), BCrypt |
| 데이터 | Spring Data JPA (Hibernate), MySQL 8 |
| AI 서버 연동 | Spring WebFlux `WebClient` (Reactor Netty, multipart, 타임아웃) |
| 비동기 처리 | Spring `@Async` + 전용 `ThreadPoolTaskExecutor` |
| 파일 저장 | AWS SDK for Java v2 (S3, S3 Presigner), Spring Cloud AWS 3.0 |

**Infra (운영)**

| 구분 | 기술 |
|---|---|
| 서버 | EC2 (IAM Role 기반 S3 접근) |
| DB | RDS for MySQL |
| 파일 저장소 | S3 (Presigned URL로 결과 음원 제공) |
| AI | Hugging Face Space (FastAPI, MusicGen-melody, Basic Pitch) |

**Local / Test**

| 구분 | 기술 |
|---|---|
| 로컬 인프라 | Docker Compose, MySQL 8.4, Adobe S3Mock 5.2 |
| 테스트 | JUnit 5, Mockito, AssertJ, Spring Security Test, MockMvc, H2 |
