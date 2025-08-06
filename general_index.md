# VOYZ 프로젝트 - 전체 파일 인덱스

> ⚠️ **주의**: 이 인덱스는 최신이 아닐 수 있습니다. 정확한 정보는 실제 소스 코드를 참조하시기 바랍니다.

## 📁 **1. WEBAPP (React 웹앱)**
*위치: `/mnt/c/Users/1/Desktop/VOYZ/WEBAPP`*

### 🔧 설정 파일
| 파일명 | 설명 |
|--------|------|
| `package.json` | 프로젝트 의존성 및 빌드 스크립트 정의 |
| `vite.config.js` | Vite 번들러 설정 파일 |
| `eslint.config.js` | ESLint 코드 품질 검사 설정 |
| `index.html` | 메인 HTML 템플릿 |

### 🎯 메인 컴포넌트
| 파일명 | 설명 |
|--------|------|
| `src/App.jsx` | 메인 애플리케이션 컴포넌트, 전체 상태 관리 |
| `src/main.jsx` | React DOM 렌더링 진입점 |

### 🍽️ 메뉴 관리
| 파일명 | 설명 |
|--------|------|
| `src/components/Menu/MenuSection.jsx` | 카테고리별 메뉴 섹션 표시 컴포넌트 |
| `src/components/Menu/MenuItem.jsx` | 개별 메뉴 아이템 표시 및 리뷰 기능 |
| `src/components/Menu/OrderPage.jsx` | 주문 페이지 및 장바구니 관리 |

### 💳 결제 시스템
| 파일명 | 설명 |
|--------|------|
| `src/components/Payment/PaymentModal.jsx` | 결제 방법 선택 모달 (토스페이, PayPal) |
| `src/components/Payment/TossPaymentWidget.jsx` | 토스페이먼츠 결제 위젯 및 API 연동 |
| `src/components/Payment/PayPalPaymentWidget.jsx` | PayPal 결제 위젯 및 국제결제 처리 |

### ⭐ 리뷰 시스템
| 파일명 | 설명 |
|--------|------|
| `src/components/Review/ReviewModal.jsx` | 리뷰 보기 모달, 다국어 지원 |
| `src/components/Review/WriteReviewPage.jsx` | 리뷰 작성 페이지, 별점 및 텍스트 입력 |

### 📱 UI 컴포넌트
| 파일명 | 설명 |
|--------|------|
| `src/components/UI/LanguageSelector.jsx` | 다국어 선택기 컴포넌트 |
| `src/components/UI/Modal.jsx` | 공통 모달 컴포넌트 |
| `src/components/UI/NotificationPermissionModal.jsx` | 푸시 알림 권한 요청 모달 |
| `src/components/UI/SuccessPage.jsx` | 주문 완료 페이지 |

### 🔧 유틸리티 및 훅
| 파일명 | 설명 |
|--------|------|
| `src/hooks/useMenu.js` | 장바구니 상태 관리 커스텀 훅 |
| `src/utils/pushNotifications.js` | 웹 푸시 알림 기능 (Service Worker, VAPID) |
| `src/utils/helpers.js` | 공통 헬퍼 함수들 (가격 포맷, 리뷰 텍스트 등) |

### 📊 데이터 및 타입
| 파일명 | 설명 |
|--------|------|
| `src/datas/sampleData.js` | 샘플 메뉴 데이터 (다국어 리뷰 포함) |
| `src/types/index.js` | 타입 정의 및 상수 (언어, 국가 플래그) |

### 🎨 스타일
| 파일명 | 설명 |
|--------|------|
| `src/index.css` | 전역 스타일 및 CSS 변수 정의 |
| `src/App.css` | 애플리케이션 메인 스타일 |

### 🛠️ Service Worker
| 파일명 | 설명 |
|--------|------|
| `public/sw.js` | 푸시 알림 처리 Service Worker |

---

## 📁 **2. Backend (Spring Boot)**
*위치: `/mnt/c/Users/1/Desktop/VOYZ/backend`*

### 🔧 설정 파일
| 파일명 | 설명 |
|--------|------|
| `pom.xml` | Maven 의존성 관리 (Spring Boot, Oracle JDBC, JWT) |
| `src/main/resources/application.properties` | 데이터베이스, JWT, 서버 포트 설정 |

### 🎯 메인 클래스
| 파일명 | 클래스명 | 설명 |
|--------|----------|------|
| `src/main/java/com/voiz/VoizApplication.java` | `VoizApplication` | Spring Boot 메인 클래스, 비동기 처리 활성화 |

### 🌐 Controller Layer
| 파일명 | 클래스명 | 주요 메서드 | 설명 |
|--------|----------|-------------|------|
| `src/main/java/com/voiz/controller/CalendarController.java` | `CalendarController` | `getDaySuggestions()`, `getWeatherInfo()` | 캘린더 및 리마인더 API |
| `src/main/java/com/voiz/controller/WeatherDataController.java` | `WeatherDataController` | `getCurrentWeather()`, `getForecast()` | 날씨 데이터 API |
| `src/main/java/com/voiz/controller/AuthController.java` | `AuthController` | `login()`, `register()` | 사용자 인증 API |
| `src/main/java/com/voiz/controller/UserController.java` | `UserController` | `getUserInfo()`, `updateUser()` | 사용자 관리 API |
| `src/main/java/com/voiz/controller/DataMatchController.java` | `DataMatchController` | `matchSpecialDays()` | 특일 데이터 매칭 API |
| `src/main/java/com/voiz/controller/AnalysisController.java` | `AnalysisController` | `analyzeData()` | 데이터 분석 API |

### 🏢 Service Layer
| 파일명 | 클래스명 | 주요 메서드 | 설명 |
|--------|----------|-------------|------|
| `src/main/java/com/voiz/service/WeatherService.java` | `WeatherService` | `getCurrentWeather()`, `getForecast()` | 기상청 API 연동 서비스 |
| `src/main/java/com/voiz/service/CalendarService.java` | `CalendarService` | `getCalendarData()`, `saveReminder()` | 캘린더 비즈니스 로직 |
| `src/main/java/com/voiz/service/UserService.java` | `UserService` | `authenticateUser()`, `registerUser()` | 사용자 관리 서비스 |
| `src/main/java/com/voiz/service/MatchService.java` | `MatchService` | `matchSpecialDays()` | 특일 매칭 알고리즘 |
| `src/main/java/com/voiz/service/AsyncMatchingService.java` | `AsyncMatchingService` | `processMatchingAsync()` | 비동기 매칭 처리 |
| `src/main/java/com/voiz/service/JwtTokenService.java` | `JwtTokenService` | `generateToken()`, `validateToken()` | JWT 토큰 관리 |
| `src/main/java/com/voiz/service/FastApiClient.java` | `FastApiClient` | `callMlService()` | ML 서비스 HTTP 클라이언트 |

### 📊 Repository Layer (MyBatis)
| 파일명 | 인터페이스명 | 주요 메서드 | 설명 |
|--------|-------------|-------------|------|
| `src/main/java/com/voiz/mapper/WeatherRepository.java` | `WeatherRepository` | `insertWeather()`, `selectWeather()` | 날씨 데이터 저장소 |
| `src/main/java/com/voiz/mapper/UsersRepository.java` | `UsersRepository` | `findByUsername()`, `save()` | 사용자 데이터 저장소 |
| `src/main/java/com/voiz/mapper/CalendarRepository.java` | `CalendarRepository` | `findCalendarData()` | 캘린더 데이터 저장소 |
| `src/main/java/com/voiz/mapper/SpecialDayRepository.java` | `SpecialDayRepository` | `findSpecialDays()` | 특일 데이터 저장소 |

### 📝 Entity/VO Classes
| 파일명 | 클래스명 | 주요 필드 | 설명 |
|--------|----------|-----------|------|
| `src/main/java/com/voiz/vo/Users.java` | `Users` | `userId`, `storeName`, `storeCategory` | 사용자 및 매장 정보 엔터티 (JPA) |
| `src/main/java/com/voiz/vo/Weather.java` | `Weather` | `temperature`, `condition` | 날씨 정보 엔터티 |
| `src/main/java/com/voiz/vo/SpecialDay.java` | `SpecialDay` | `date`, `name`, `category` | 특일 정보 엔터티 |

### 🔧 Utility Classes
| 파일명 | 클래스명 | 주요 메서드 | 설명 |
|--------|----------|-------------|------|
| `src/main/java/com/voiz/util/WeatherApi.java` | `WeatherApi` | `fetchWeatherData()` | 기상청 API 연동 유틸리티 |
| `src/main/java/com/voiz/util/GeoConverter.java` | `GeoConverter` | `convertToGrid()` | GPS 좌표를 격자 좌표로 변환 |
| `src/main/java/com/voiz/util/PasswordEncoder.java` | `PasswordEncoder` | `encode()`, `matches()` | 비밀번호 암호화 유틸리티 |

---

## 📁 **3. Frontend (Android 앱)**
*위치: `/mnt/c/Users/1/Desktop/VOYZ/frontend`*

### 🔧 설정 파일
| 파일명 | 설명 |
|--------|------|
| `app/build.gradle.kts` | Android 프로젝트 빌드 설정 (Compose, Retrofit) |
| `gradle/libs.versions.toml` | 의존성 버전 중앙 관리 |
| `settings.gradle.kts` | Gradle 프로젝트 설정 |

### 🎯 메인 액티비티
| 파일명 | 클래스명 | 설명 |
|--------|----------|------|
| `app/src/main/java/com/voyz/presentation/activity/MainActivity.kt` | `MainActivity` | 메인 액티비티, Compose 설정 |
| `app/src/main/java/com/voyz/presentation/navigation/NavGraph.kt` | `NavGraph` | 네비게이션 그래프, 15개 화면 라우팅 (로그인, 회원가입, 메인, 알람, 검색, 관리, 마케팅, 리마인더 등) |

### 🔐 인증 화면
| 파일명 | 함수명 | 설명 |
|--------|--------|------|
| `app/src/main/java/com/voyz/presentation/screen/auth/LoginScreen.kt` | `LoginScreen()` | 로그인 화면, 자동 로그인 기능 |
| `app/src/main/java/com/voyz/presentation/screen/auth/LoginViewModel.kt` | `LoginViewModel` | 로그인 상태 관리 ViewModel |
| `app/src/main/java/com/voyz/presentation/screen/auth/signup/SignUpScreen.kt` | `SignUpScreen()` | 회원가입 화면 |
| `app/src/main/java/com/voyz/presentation/screen/auth/IdPwFindScreen.kt` | `IdPwFindScreen()` | 아이디/비밀번호 찾기 화면 |

### 📅 캘린더 컴포넌트
| 파일명 | 함수/클래스명 | 설명 |
|--------|---------------|------|
| `app/src/main/java/com/voyz/presentation/component/calendar/CalendarComponent.kt` | `CalendarComponent()` | 메인 캘린더 UI, 드래그 제스처 지원 |
| `app/src/main/java/com/voyz/presentation/component/calendar/CalendarViewModel.kt` | `CalendarViewModel` | 캘린더 데이터 및 상태 관리 |
| `app/src/main/java/com/voyz/presentation/component/calendar/components/CalendarGrid.kt` | `CalendarGrid()` | 캘린더 그리드 레이아웃 |

### 🏠 메인 화면
| 파일명 | 함수명 | 설명 |
|--------|--------|------|
| `app/src/main/java/com/voyz/presentation/screen/main/MainScreen.kt` | `MainScreen()` | 메인 대시보드 화면 |
| `app/src/main/java/com/voyz/presentation/screen/main/SearchScreen.kt` | `SearchScreen()` | 검색 기능 화면 |

### 📱 관리 화면
| 파일명 | 함수명 | 설명 |
|--------|--------|------|
| `app/src/main/java/com/voyz/presentation/screen/management/OperationManagementScreen.kt` | `OperationManagementScreen()` | 운영 관리 화면 |
| `app/src/main/java/com/voyz/presentation/screen/management/CustomerManagementScreen.kt` | `CustomerManagementScreen()` | 고객 관리 화면 |
| `app/src/main/java/com/voyz/presentation/screen/management/SettingsScreen.kt` | `SettingsScreen()` | 설정 화면 |
| `app/src/main/java/com/voyz/presentation/screen/management/UserProfileScreen.kt` | `UserProfileScreen()` | 사용자 프로필 화면 |

### 📈 마케팅 화면
| 파일명 | 함수명 | 설명 |
|--------|--------|------|
| `app/src/main/java/com/voyz/presentation/screen/marketing/MarketingCreateScreen.kt` | `MarketingCreateScreen()` | 마케팅 생성 화면 |
| `app/src/main/java/com/voyz/presentation/screen/marketing/MarketingOpportunityDetailScreen.kt` | `MarketingOpportunityDetailScreen()` | 마케팅 기회 상세 화면 |

### 🔔 리마인더 기능
| 파일명 | 함수명 | 설명 |
|--------|--------|------|
| `app/src/main/java/com/voyz/presentation/screen/reminder/ReminderScreen.kt` | `ReminderScreen()` | 리마인더 목록 표시 |
| `app/src/main/java/com/voyz/presentation/screen/reminder/ReminderCreateScreen.kt` | `ReminderCreateScreen()` | 새 리마인더 생성 (파라미터 지원) |
| `app/src/main/java/com/voyz/presentation/screen/reminder/ReminderDetailScreen.kt` | `ReminderDetailScreen()` | 리마인더 상세 보기 |
| `app/src/main/java/com/voyz/presentation/screen/reminder/AlarmScreen.kt` | `AlarmScreen()` | 알람 화면 |

### 🌐 네트워크 레이어
| 파일명 | 클래스/인터페이스명 | 주요 메서드 | 설명 |
|--------|-------------------|-------------|------|
| `app/src/main/java/com/voyz/datas/network/ApiClient.kt` | `ApiClient` | `createRetrofit()` | HTTP 클라이언트 생성 |
| `app/src/main/java/com/voyz/datas/network/CalendarApiService.kt` | `CalendarApiService` | `getCalendarData()` | 캘린더 API 인터페이스 |
| `app/src/main/java/com/voyz/datas/network/UserApiService.kt` | `UserApiService` | `login()`, `register()` | 사용자 API 인터페이스 |

### 💾 데이터 관리
| 파일명 | 클래스명 | 주요 메서드 | 설명 |
|--------|----------|-------------|------|
| `app/src/main/java/com/voyz/datas/datastore/UserPreferencesManager.kt` | `UserPreferencesManager` | `saveUserToken()` | DataStore 기반 설정 관리 |
| `app/src/main/java/com/voyz/datas/repository/CalendarRepository.kt` | `CalendarRepository` | `getCalendarData()` | 캘린더 데이터 저장소 |

### 🎨 UI 테마
| 파일명 | 함수명 | 설명 |
|--------|--------|------|
| `app/src/main/java/com/voyz/ui/theme/Theme.kt` | `VoyzTheme()` | Material 3 테마 정의 |
| `app/src/main/java/com/voyz/ui/theme/Color.kt` | N/A | 앱 컬러 팔레트 정의 |

---

## 📁 **4. ML 서비스 (FastAPI)**
*위치: `/mnt/c/Users/1/Desktop/VOYZ/ml`*

### 🔧 설정 파일
| 파일명 | 설명 |
|--------|------|
| `requirements.txt` | Python 의존성 관리 (FastAPI, OpenAI, numpy) |
| `config.py` | API 설정, OpenAI 키, 데이터 모델 정의 |

### 🎯 메인 API
| 파일명 | 함수명 | 엔드포인트 | 설명 |
|--------|--------|-----------|------|
| `main.py` | `match_special_day()` | `POST /api/match/specialDay` | 특일-고객 매칭 API |
| `main.py` | `generate_content()` | `POST /api/content/generate` | GPT 기반 컨텐츠 생성 |
| `main.py` | `classify_category()` | `POST /api/category/classify` | 특일 카테고리 분류 |
| `main.py` | `create_suggestion()` | `POST /api/suggest/create` | 특일 제안 생성 |

### 🤖 AI 모델 및 서비스
| 파일명 | 클래스/함수명 | 주요 메서드 | 설명 |
|--------|---------------|-------------|------|
| `models/match_models.py` | `BasicMatchModel` | `match()` | 기본 키워드 매칭 모델 |
| `services/content_service.py` | `ContentService` | `generate_content()` | GPT 기반 컨텐츠 생성 서비스 |
| `services/category_service.py` | `CategoryService` | `classify_category()` | GPT 기반 카테고리 분류 |

---

> ⚠️ **주의**: 이 인덱스는 최신이 아닐 수 있습니다. 정확한 정보는 실제 소스 코드를 참조하시기 바랍니다.