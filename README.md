# Back-End

허밍 기반 AI 작곡 서비스 백엔드 (Spring Boot 3.5, Java 21)

## 로컬 실행 (AWS 없이)

실제 인프라를 Docker 컨테이너로 대체해 로컬에서 전체 흐름을 실행할 수 있습니다.

| 운영 | 로컬 |
|---|---|
| AWS RDS (MySQL) | MySQL 8.4 컨테이너 |
| AWS S3 | Adobe S3Mock (S3 호환, Presigned URL 지원) |
| AI 서버 (MusicGen) | 가짜 AI 서버 (`local/fake-ai`, 3초 후 사인파 WAV 반환) |

```bash
# 1. 인프라 실행
docker compose up -d

# 2. 애플리케이션 실행 (local 프로필)
./gradlew bootRun --args='--spring.profiles.active=local'
```

- API: http://localhost:8080
- S3Mock: http://localhost:9090 (버킷 `humming-local` 자동 생성)
- 종료: `docker compose down` (데이터까지 삭제하려면 `docker compose down -v`)

### 요청 흐름 예시

```bash
# 회원가입 → accessToken 발급
curl -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"email":"test@test.com","password":"password1","name":"tester"}'

# 허밍 업로드 → filePath (humming/{userId}/...)
curl -X POST localhost:8080/api/upload -H "Authorization: Bearer $TOKEN" -F file=@hum.mp3

# 작곡 요청 → jobId (QUEUED로 즉시 응답)
curl -X POST localhost:8080/api/compose -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"hummingMain":"<filePath>","key":"C","tempo":"120","duration":"10","style":"pop","mood":"happy"}'

# 상태 폴링 → SUCCEEDED 시 musicUrl(Presigned URL) 반환
curl localhost:8080/api/job/<jobId> -H "Authorization: Bearer $TOKEN"
```

## 테스트

외부 인프라 없이 H2 인메모리 DB와 Mockito로 실행됩니다.

```bash
./gradlew test
```

## 운영 설정

`src/main/resources/application.properties.example`을 참고해 `application.properties`를 작성합니다. (git 추적 제외)
