# VOYZ 프로젝트 - 외국인 고객 유치 요식업 마케팅 플랫폼

## 개발 참고사항
- 디자인이나, 주석에 이모지 남발하지 않기
- frontend나, WEBAPP은 항상 공통 디자인 참조하고 디자인 일관성 유지하려고 노력하기
- 커밋메세지에는 Claude 관련 내용 제외

## 🎯 프로젝트 개요
VOYZ는 외국인 고객 유치와 서비스 향상을 돕는 요식업 마케팅 플랫폼입니다. 한국 주요 이벤트(K-POP 콘서트, 축제) 및 주요 방문국의 연휴/이벤트 정보를 활용한 외국인 방문 예측, QR 기반 스마트 메뉴 시스템, 다국어 리뷰 통합, LLM 기반 메뉴 컨시어지를 제공하는 통합 솔루션입니다.

## 🏗️ 시스템 아키텍처
```
📱 Android App (Frontend) - Kotlin, Jetpack Compose (메인 비즈니스 앱)
    ↕️ REST API
🌐 Spring Boot (Backend) - Java, MyBatis, Oracle DB
    ↕️ HTTP
🤖 FastAPI (ML Service) - Python, OpenAI GPT-4o-mini, Google Cloud Vision API
🍽️ Web App (WEBAPP) - React, Vite (레스토랑 주문 시스템 + PWA)
```

## 🔧 기술 스택

### Frontend (Android)
- **언어**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **네트워킹**: Retrofit2, OkHttp
- **상태관리**: ViewModel, DataStore
- **비동기**: Coroutines, Flow
- **차트**: MPAndroidChart (파이차트 구현)
- **유틸리티**: NationalityFlagMapper (국가별 플래그 이모지 매핑)

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
- **외부 API**: 기상청 API, 공공데이터포털 특일 API
- **비동기 처리**: @EnableAsync, AsyncMatchingService

### ML Service (FastAPI)
- **프레임워크**: FastAPI
- **AI 모델**: OpenAI GPT-4o-mini
- **OCR**: Google Cloud Vision API
- **번역**: Google Cloud Translation API
- **데이터 검증**: Pydantic
- **비동기**: asyncio, uvicorn

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

### 1. 외국인 방문 예측 시스템 (MVP)
- K-POP 콘서트, 한국 축제 일정 자동 수집
- 주요 방문국(일본, 중국 등) 연휴/이벤트 정보 수집
- 입국 통계 데이터 연동으로 방문 예상 시기 알림
- 사장님용 앱에 일정 예측 캘린더 제공
- "일본 연휴라 일본인 많이 올 예정" 스마트 알림

### 2. 사장님 대응 준비 시스템 (MVP)
- 예상 방문객 국가별 선호 메뉴 제안
- 이벤트별 프로모션 자동 생성 및 추천
- "중국인 단체 예약 시 이 메뉴 추천" 맞춤 제안
- AI 기반 특일 분석 및 카테고리 분류
- 신뢰도(confidence) 기반 제안 우선순위화

### 3. QR 기반 스마트 메뉴 시스템 (차별화 기능)
- OCR 기반 메뉴판 자동 인식 (촬영만으로 간편 등록)
- 테이블 QR 스캔으로 즉시 접속 가능한 웹앱
- 자동 언어 감지 및 실시간 메뉴 번역 (한/영/중/일)
- 메뉴 이미지와 상세 설명 제공
- 메뉴에 대한 리뷰 즉시 확인
- 주문/결제 원스톱 처리 (토스페이먼츠, PayPal)

### 4. LLM 기반 메뉴 컨시어지 (프리미엄 기능)
- 메뉴에 대한 실시간 질의응답 (다국어 지원)
- "이거 맵나요?", "땅콩 들어있나요?" 즉시 답변
- 모든 언어 지원 GPT-4o-mini 기반 채팅
- 외국인 고객 맞춤 메뉴 추천

### 5. 글로벌 리뷰 통합 (차별화 기능)
- AI 기반 다국어 리뷰 자동 번역
- 일본인이 쓴 리뷰를 중국인이 중국어로 확인 가능
- 국가별 리뷰 필터링
- 사장님께 리뷰 트렌드 분석 및 인사이트 제공
- 웹 푸시 알림 및 리뷰 작성 리마인더

### 6. 인증 및 운영 관리
- JWT 기반 로그인/회원가입
- 업장 정보 관리
- 매출 그래프 및 데이터 분석 대시보드
- 일별/월별 외국인 방문 통계
- 국가별 선호 메뉴 분석

## 📊 데이터베이스 설계
- **Users**: 사용자 및 업장 정보
- **SpecialDay**: K-POP 콘서트, 축제 등 특일 정보
- **SpecialDayMatch**: 특일-업종 매칭 데이터
- **SpecialDaySuggest**: AI 생성 외국인 방문 예측 및 제안
- **Marketing**: 국가별 맞춤 마케팅 제안
- **Calendar**: 외국인 방문 예측 캘린더
- **Reminder**: 이벤트 및 프로모션 리마인더
- **Menus**: 다국어 메뉴 정보 (OCR 등록)
- **Orders/OrdersItems**: 외국인 고객 주문 관리
- **Tables**: QR 코드 테이블 관리
- **Reviews**: 다국어 리뷰 및 번역 데이터
- **VisitorStats**: 국가별 방문 통계
- **NationalityAnalytics**: 국가별 방문객 분석 데이터
- **Token**: JWT 토큰 관리

## 🔗 API 엔드포인트

### Spring Boot APIs
- `/api/auth/**` - 인증 관련 (로그인, 회원가입, 토큰 갱신)
- `/api/calendar/**` - 캘린더 및 리마인더
- `/api/weather/**` - 날씨 데이터
- `/api/users/**` - 사용자 관리
- `/api/match/**` - 특일 데이터 매칭
- `/api/menus/**` - 메뉴 관리 (CRUD, OCR, 번역)
- `/api/orders/**` - 주문 관리
- `/api/tables/**` - 테이블 관리
- `/api/qr/**` - QR 코드 생성
- `/api/analysis/**` - 데이터 분석
- `/api/analytics/**` - 리뷰 및 국가별 방문객 분석

### ML Service APIs
- `POST /api/match/specialDay` - 특일-외국인 방문객 매칭
- `POST /api/content/generate` - 국가별 맞춤 마케팅 컨텐츠 생성
- `POST /api/category/classify` - 특일 카테고리 분류
- `POST /api/suggest/create` - 외국인 방문 예측 제안 생성 (신뢰도 포함)
- `POST /api/ocr` - 메뉴판 OCR 인식 (Google Cloud Vision)
- `POST /api/translate` - 다국어 메뉴 번역 (Google Cloud Translation)
- `POST /api/reviews/analyze` - 리뷰 감정 분석 및 키워드 추출
- `POST /api/chat/menu` - LLM 기반 메뉴 질의응답 (예정)
- `POST /api/review/translate` - 리뷰 자동 번역 (예정)

## 🛠️ 개발 도구
- **IDE**: Android Studio, IntelliJ IDEA, VS Code
- **빌드**: Gradle (Android), Maven (Spring), Vite (React)
- **버전 관리**: Git
- **API 테스트**: Swagger UI, Postman

## 📱 배포 환경
- **Android**: APK/AAB 빌드
- **Web App**: Static 파일로 Spring Boot 통합 배포
- **Backend**: EC2 + systemd (voyz-app.service)
- **ML Service**: EC2 + systemd (voyz-ml.service)
- **CI/CD**: GitHub Actions 자동 배포

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
- API 테스트: Swagger UI, Postman

## 🔐 환경 변수 및 시크릿
- `JWT_SECRET`: JWT 토큰 서명 키
- `OPENAI_API_KEY`: OpenAI API 키
- `GOOGLE_APPLICATION_CREDENTIALS`: Google Cloud 인증 JSON
- `EC2_HOST`, `EC2_USERNAME`, `EC2_PRIVATE_KEY`: 배포 서버 정보

## 📈 개발 진행 상황

### ✅ 완료된 기능 (MVP 단계)
- GitHub Actions 기반 자동 배포 구축
- Google Cloud Vision API를 활용한 OCR 메뉴 인식
- 다국어 메뉴 번역 기능 (Google Cloud Translation)
- 운영관리 매출 그래프 구현 (Android)
- 웹 푸시 알림 및 PWA 지원
- QR 기반 주문 시스템 (토스페이먼츠, PayPal 결제)
- JWT 기반 인증 시스템
- 특일 데이터 수집 및 AI 매칭 시스템
- 국가별 고객 통계 분석 기능
- 리뷰 목록 화면 (ReviewListScreen)
- 리뷰 분석 화면 (ReviewAnalysisScreen) 완료
- 백그라운드 분석 서비스 및 캐싱 시스템
- 날씨 데이터 자동 업데이트 스케줄러
- 리마인더 등록 시 알림 기능

### 🚧 개발 중인 기능
- OCR 메뉴 등록 후 자동 저장 기능
- 외국인 방문 예측 캘린더 UI
- 국가별 선호 메뉴 분석 대시보드
- 프론트엔드 다국어 번역 서비스 통합
- 메뉴 감정 분석 및 국가별 평점 차트 기능

### 📋 예정된 기능 (차별화/프리미엄)
- LLM 기반 메뉴 컨시어지 채팅
- 다국어 리뷰 자동 번역 시스템
- 입국 통계 데이터 연동
- K-POP 콘서트/축제 일정 자동 수집
- 국가별 연휴/이벤트 정보 수집
- 방문객 국가별 통계 및 트렌드 분석

---

## 🎯 프로젝트 목표 및 비전
- **기간**: 2025.07.18 ~ 2025.08.13 (4주)
- **목표**: 외국인 고객 유치를 통한 요식업 매출 향상
- **비전**: AI 기반 예측과 다국어 지원으로 글로벌 고객 서비스 혁신

## 👥 서비스 이용 흐름

### 🏪 사장님 이용 흐름
1. **초기 설정**: 회원가입 → 메뉴판 촬영/OCR 등록 → QR 코드 생성
2. **일상 운영**: 외국인 방문 예측 확인 → 맞춤 프로모션 적용
3. **데이터 분석**: 국가별 방문 통계 → 선호 메뉴 분석 → 리뷰 트렌드 확인

### 🌍 외국인 고객 이용 흐름
1. **메뉴 확인**: QR 스캔 → 자동 언어 감지 → 번역된 메뉴 확인
2. **주문 과정**: 메뉴 선택 → LLM 채팅 문의 → 장바구니 담기
3. **결제/리뷰**: 간편 결제 → 자국어 리뷰 작성 → 인센티브 제공

---

**⚠️ 참고**: 이 문서는 2025년 8월 11일 기준으로 최신 상태이며, 정확한 정보는 소스 코드를 참조하시기 바랍니다.