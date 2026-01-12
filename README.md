<div align="center">
  # 🗺️ 바로가기 (Barogagi)
  
  **AI 기반 스마트 일정 추천 플래너**
  
  [![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
  [![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?logo=hibernate&logoColor=white)](https://hibernate.org/orm/)
  [![QueryDSL](https://img.shields.io/badge/QueryDSL-5.0.0-blue)](https://querydsl.com/)
  [![MyBatis](https://img.shields.io/badge/MyBatis-3.0.3-black)](https://mybatis.org/mybatis-3/)
  [![MariaDB](https://img.shields.io/badge/MariaDB-10.x-003545?logo=mariadb&logoColor=white)](https://mariadb.org/)
  [![JWT](https://img.shields.io/badge/JWT-0.11.5-000000?logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
  [![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger&logoColor=white)](https://springdoc.org/)
</div>

---

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시작하기](#-시작하기)
- [프로젝트 구조](#-프로젝트-구조)
- [개발 가이드](#-개발-가이드)
- [스크립트](#-스크립트)
- [컨벤션 가이드](#-컨벤션-가이드)
- [API 명세](#-api-명세)
- [로드맵](#-로드맵)
- [기여하기](#-기여하기)
- [팀](#-팀)

---

## 🎯 프로젝트 소개

> **"어디 갈지 고민하는 시간, 이제 그만!"**

친구들과 만나기로 했는데 어디를 갈지 정하느라 시간만 가고, 검색해도 원하는 결과가 안 나오는 경험, 누구나 있으시죠?

**바로가기**는 키워드 기반으로 하루 일정을 자동으로 추천해주는 스마트 플래너입니다. 레고 블록을 쌓듯이 큰 일정만 선택하면, AI가 세부 장소와 루트까지 알아서 짜드립니다!

### 🤔 왜 바로가기를 만들었나요?

- 🔍 **검색의 고통**: "모밀집" 검색했는데 라멘집만 나옴
- ⏰ **시간의 낭비**: 어디 갈지 알아보는데 만남 시간의 절반 소모
- 🗺️ **루트의 혼란**: 효율적인 동선을 짜는 게 너무 어려움
- 👥 **의견 조율 지옥**: 친구들 취향 맞추기가 전쟁

### ✨ 바로가기의 해결책

- ⚡ **키워드 기반 즉시 추천**: "카페 → 식사 → 데이트" 선택만으로 완성
- 🧩 **레고식 일정 구성**: 큰 틀만 사용자가, 세부는 AI가
- 🗺️ **스마트 루트 최적화**: 이동 시간을 고려한 효율적 동선
- 🎯 **맞춤형 추천**: 그룹 키워드 기반 모두가 만족하는 장소

---

## ✨ 주요 기능

### 🎨 홈 화면
- **트렌딩 핫플레이스**: 지금 인기 있는 장소를 실시간 랭킹으로 표시
- **롤링 애니메이션**: Framer Motion으로 구현한 부드러운 순위 변동
- **드래그 캐러셀**: 터치/드래그로 탐색하는 트렌딩 일정 목록

### 📅 스마트 일정 생성
- **키워드 기반 추천**: "카페", "맛집", "데이트" 등 키워드 선택
- **AI 자동 구성**: 선택한 키워드에 맞는 최적의 장소 추천
- **시간표 뷰**: 에브리타임처럼 직관적인 일정 시각화
- **태그 시스템**: 일정별 카테고리 분류 및 필터링

### 🗺️ 루트 최적화
- **이동 시간 계산**: 장소 간 실제 이동 시간 고려
- **효율적 동선**: 최단 거리 기반 순서 자동 조정
- **지도 시각화**: 트리플처럼 전체 루트를 지도로 표시

### 👥 그룹 기능
- **멤버 초대**: 친구들과 일정 공유
- **공동 키워드**: 그룹원들의 선호도 통합 분석
- **알림 시스템**: "이동할 시간이에요!" 실시간 알림

### 🔐 사용자 인증
- **이메일 회원가입**: 안전한 이메일 기반 계정 생성
- **OAuth 로그인**: Google, Kakao, Naver 소셜 로그인
- **계정 찾기**: 아이디/비밀번호 찾기 및 재설정

---

## 🛠️ 기술 스택
### Backend
| 카테고리        | 기술                          | 버전      | 설명                      |
| ----------- | --------------------------- | ------- | ----------------------- |
| Core        | Java                        | 17      | LTS 기반 백엔드 언어           |
| Framework   | Spring Boot                 | 3.2.1   | REST API 및 애플리케이션 프레임워크 |
| Web         | Spring Web                  | 3.2.1   | RESTful API 개발          |
| Persistence | Spring Data JPA             | 3.2.1   | ORM 기반 데이터 접근           |
| Query       | QueryDSL                    | 5.0.0   | 타입 안전 동적 쿼리             |
| SQL Mapper  | MyBatis                     | 3.0.3   | SQL 기반 데이터 처리           |
| Database    | MariaDB                     | 3.1.4   | 관계형 데이터베이스              |
| Security    | Spring Security             | 3.2.1   | 인증 및 인가 처리              |
| Auth        | OAuth2 Client               | 3.2.1   | 소셜 로그인                  |
| Auth        | JWT (jjwt)                  | 0.11.5  | 토큰 기반 인증                |
| Validation  | Spring Validation           | 3.2.1   | 요청 값 검증                 |
| API Docs    | Springdoc OpenAPI (Swagger) | 2.1.0   | API 문서 자동화              |
| Encryption  | Jasypt                      | 3.0.4   | 민감 정보 암호화               |
| Messaging   | Nurigo SMS SDK              | 4.3.0   | SMS 인증                  |
| Build Tool  | Maven                       | 3.x     | 의존성 및 빌드 관리             |
| Dev Tools   | Spring Boot DevTools        | 3.2.1   | 개발 생산성 향상               |
| Utility     | Lombok                      | 1.18.38 | 보일러플레이트 코드 제거           |

---

## 🚀 시작하기

### ⚙️ 설치 및 실행

1. **저장소 클론**
   ```bash
   git clone https://github.com/T-BluePot/barogagi-back.git
   cd barogagi-back
   ```

2. **환경 설정**
   ```bash
   # application.yml 또는 application.properties 설정
   # (DB, OAuth, JWT, Jasypt 관련 값 필요)
   ```

3. **의존성 설치 및 빌드**
   ```bash
   ./mvnw clean install
   ```

4. **개발 서버 실행**
   ```bash
   ./mvnw spring-boot:run
   또는
   java -jar target/barogagi-1.0-SNAPSHOT.jar
   ```
---

## 📁 프로젝트 구조

```
barogagi-back/
├── .github/                    # GitHub 워크플로우/설정
├── src/
│   └── main/
│       ├── java/com/
│       │   └── barogagi
│       │       ├── config/         # ai 관련 코드
│       │       ├── approval/       # 인증 관련 코드
│       │       ├── config/         # 설정 클래스
│       │       ├── kakaoplace/     # 카카오 장소 검색 API 관련 코드
│       │       ├── logging/        # 로그 관련 코드
│       │       ├── mainPage/       # 메인페이지 API 관련 코드
│       │       ├── member/         # 회원 API 관련 코드
│       │       ├── naverblog/      # 네이버 블로그 API 관련 코드
│       │       ├── plan/           # 계획 API 관련 코드
│       │       ├── region/         # 장소 API 관련 코드
│       │       ├── response/       # API 응답 객체
│       │       ├── schedule/       # 일정 API 관련 코드
│       │       ├── sendSms/        # 메시지 발송
│       │       ├── tag/            # 태그 API 관련 코드
│       │       ├── terms/          # 약관 API 관련 코드
│       │       ├── util/           # 유틸 API 관련 코드
│       │   ├── Application.java    # 메인 실행 클래스
│       │   ├── SwaggerConfig       # Swagger 관련 코드
│       └── resources/
│           ├── mapper                     # 쿼리
│           └── application.properties/    # 환경설정 (DB, OAuth, JWT 등)
├── .gitignore                   # Git 무시 파일 목록
├── Dockerfile                   # Docker 이미지 빌드 설정
├── README.md                    # 프로젝트 설명 및 실행 방법 :contentReference[oaicite:1]{index=1}
└── pom.xml                     # Maven 의존성 및 빌드 설정 :contentReference[oaicite:2]{index=2}
```

---

## 📐 컨벤션 가이드

### 커밋 메시지

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type**
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅, 세미콜론 누락 등
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드
- `chore`: 빌드, 패키지 매니저 설정 등
- `design`: CSS 등 UI 디자인 변경

**예시**
```bash
feat(auth): 이메일 로그인 기능 구현

- 이메일/비밀번호 검증 로직 추가
- 로그인 실패 시 에러 메시지 표시
- 로딩 상태 UI 구현

Closes #123
```

### 브랜치 전략

```
main              # 프로덕션 배포
├── develop       # 개발 통합
    ├── feat/     # 기능 개발
    ├── fix/      # 버그 수정
    ├── design/   # UI/UX 작업
    └── docs/     # 문서 작업
```

**브랜치 네이밍**
- `feature/login-page`
- `fix/ranking-animation`
- `design/main-page`
- `docs/readme`

---

## 📡 API 명세
- [http://](http://localhost:8080/swagger-ui/index.html#/)
  
---

## 🤝 기여하기

바로가기 프로젝트에 기여해주셔서 감사합니다!

### 기여 프로세스

1. **Fork** 이 저장소
2. **브랜치 생성** (`git checkout -b feature/AmazingFeature`)
3. **커밋** (`git commit -m 'feat: Add some AmazingFeature'`)
4. **Push** (`git push origin feature/AmazingFeature`)
5. **Pull Request 생성**

### 개발 미팅

- **주기**: 2주에 1회
- **요일**: 유동적 (매주 월요일 논의)
- **방식**: 온라인 또는 오프라인

### 다음 회의 TODO

- [ ] 컨벤션 가이드 최종 확정
- [ ] API 명세서 작성
- [ ] 플로우 차트 작성
- [ ] Use Case Diagram 작성
- [ ] GitHub 프로젝트 설정
- [ ] README 업데이트

---

## 👥 팀

**T-BluePot** - 열정 넘치는 개발자들의 모임

| 역할 | 담당자 | GitHub |
|------|--------|--------|
| 프론트엔드 | 은우 | [@jeong-eun-woo](https://github.com/jeong-eun-woo) |
| 프론트엔드 | 서림 | [@서림](https://github.com/서림) |
| 백엔드 | 효경 | [@효경](https://github.com/dksgyrud1349) |
| 백엔드 | 다민 | [@효경](https://github.com/다민) |

---

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 🙏 감사의 말

이 프로젝트는 다음 오픈소스 프로젝트들의 도움을 받았습니다:

- [Java 17](https://openjdk.org/projects/jdk/17/) - 백엔드 언어, LTS 버전
Spring Boot 3.2.1 - 애플리케이션 프레임워크
Spring Security - 인증/인가 처리
JPA / Hibernate - ORM 기반 데이터 처리
QueryDSL - 타입 안전 동적 쿼리
MyBatis - SQL 기반 데이터 접근
MariaDB - 관계형 데이터베이스
JWT - 토큰 기반 인증
Springdoc OpenAPI - API 문서화
---

<div align="center">
  
  **⭐ 이 프로젝트가 마음에 드셨다면 Star를 눌러주세요! ⭐**
  
  Made with ❤️ by T-BluePot Team
  
  © 2025 Barogagi. All rights reserved.
  
</div>
