# VOYZ 프로젝트 - AI 기반 소상공인 마케팅 에이전트

## 🎯 프로젝트 개요
VOYZ는 소상공인을 위한 AI 기반 마케팅 에이전트 시스템입니다. 특일(기념일), 날씨 데이터, 고객 정보를 활용하여 맞춤형 마케팅 제안을 제공하며, 추가로 레스토랑 주문 및 리뷰 시스템을 포함합니다.

## 🏗️ 시스템 아키텍처
```
📱 Android App (Frontend) - Kotlin, Jetpack Compose (메인 비즈니스 앱)
    ↕️ REST API
🌐 Spring Boot (Backend) - Java, JPA, Oracle DB
    ↕️ HTTP
🤖 FastAPI (ML Service) - Python, OpenAI GPT-4o-mini
🍽️ Web App (WEBAPP) - React, Vite (레스토랑 주문 시스템 + PWA)
```

## 🔧 기술 스택

### Frontend (Android)
- **언어**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **네트워킹**: Retrofit2, OkHttp
- **상태관리**: ViewModel, DataStore
- **비동기**: Coroutines, Flow

### Web App (WEBAPP)
- **프레임워크**: React 18, Vite
- **결제**: TossPayments SDK, PayPal SDK
- **알림**: Web Push API, Service Worker
- **스타일링**: CSS Variables, 반응형 디자인

### Backend (Spring Boot)
- **프레임워크**: Spring Boot 3.5.3
- **데이터베이스**: Oracle DB, MyBatis
- **인증**: JWT Token
- **문서화**: Swagger/OpenAPI 3.0
- **외부 API**: 기상청 API, 특일 API

### ML Service (FastAPI)
- **프레임워크**: FastAPI
- **AI 모델**: OpenAI GPT-4o-mini
- **데이터 검증**: Pydantic
- **비동기**: asyncio

## 📁 프로젝트 구조
```
VOYZ/
├── WEBAPP/          # React 웹앱 (PWA)
├── backend/         # Spring Boot API 서버
├── frontend/        # Android 앱
├── ml/             # FastAPI ML 서비스
└── docs/           # 프로젝트 문서
```

## 🚀 주요 기능

### 1. 인증 및 사용자 관리
- JWT 기반 로그인/회원가입
- 업장 정보 관리
- 자동 로그인 기능

### 2. AI 마케팅 제안
- GPT-4o-mini 기반 특일 분석
- 날씨 데이터 연동 마케팅 인사이트
- 고객 맞춤형 컨텐츠 생성

### 3. 캘린더 시스템
- 특일 제안 및 일정 관리
- 리마인더 기능
- 드래그 제스처 지원

### 4. 결제 시스템 (Web App)
- 토스페이먼츠 결제
- PayPal 국제 결제
- 리뷰 시스템 연동

### 5. 푸시 알림
- 웹 푸시 알림 (PWA)
- 리뷰 작성 리마인더
- Service Worker 기반

## 📊 데이터베이스 설계
- **Users**: 사용자 및 업장 정보
- **Weather**: 날씨 데이터
- **SpecialDay**: 특일 정보
- **Marketing**: 마케팅 제안
- **Calendar**: 일정 관리
- **Reminder**: 리마인더

## 🔗 API 엔드포인트

### Spring Boot APIs
- `/api/auth/**` - 인증 관련
- `/api/calendar/**` - 캘린더 및 리마인더
- `/api/weather/**` - 날씨 데이터
- `/api/users/**` - 사용자 관리
- `/api/match/**` - 데이터 매칭

### ML Service APIs
- `POST /api/match/specialDay` - 특일-고객 매칭
- `POST /api/content/generate` - AI 컨텐츠 생성
- `POST /api/category/classify` - 특일 카테고리 분류
- `POST /api/suggest/create` - 특일 제안 생성

## 🛠️ 개발 도구
- **IDE**: Android Studio, IntelliJ IDEA, VS Code
- **빌드**: Gradle (Android), Maven (Spring), Vite (React)
- **버전 관리**: Git
- **API 테스트**: Swagger UI, Postman

## 📱 배포 환경
- **Android**: APK/AAB 빌드
- **Web App**: Vercel, Netlify, GitHub Pages
- **Backend**: AWS, Azure, Google Cloud
- **ML Service**: FastAPI + uvicorn

## 🔒 보안
- JWT 토큰 인증
- CORS 설정
- SQL Injection 방지
- XSS 방지
- 입력 데이터 검증

## 📈 성능 최적화
- 비동기 처리 (@EnableAsync, Coroutines)
- 데이터베이스 인덱싱
- API 응답 캐싱
- 이미지 최적화
- 코드 스플리팅 (React)

## 🧪 테스트
- 단위 테스트: JUnit (Spring), Jest (React)
- 통합 테스트: Spring Boot Test
- E2E 테스트: Android UI Test

---

**⚠️ 참고**: 이 문서는 작성 시점 기준이며, 최신 코드와 다를 수 있습니다. 정확한 정보는 소스 코드를 참조하시기 바랍니다.