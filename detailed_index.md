# VOYZ 프로젝트 - 상세 파일 인덱스

> ⚠️ **주의**: 이 인덱스는 최신이 아닐 수 있습니다. 정확한 정보는 실제 소스 코드를 참조하시기 바랍니다.

---

## 📁 **1. WEBAPP (React 웹앱) - 상세 분석**

### 🎯 **src/App.jsx - 메인 애플리케이션**
**주요 State:**
- `selectedLang`: 현재 선택된 언어 ('ko', 'en')
- `currentPage`: 현재 페이지 ('menu', 'order', 'success', 'writeReview')
- `cart`: 장바구니 상태 객체
- `showPaymentModal`: 결제 모달 표시 여부
- `orderedItems`: 주문 완료된 아이템 목록

**핵심 함수:**
- `handlePaymentComplete()`: 결제 완료 처리, localStorage에 주문 정보 저장
- `handleTossPayment()`: 토스 결제 처리, 장바구니 사전 저장
- `handleAllowNotifications()`: 푸시 알림 권한 허용 처리
- `useEffect()`: URL 파라미터 감지로 결제 성공/리뷰 페이지 라우팅

### 🍽️ **src/components/Menu/MenuItem.jsx - 메뉴 아이템**
**Props:**
- `item`: 메뉴 아이템 객체 (id, name, price, reviews, rating)
- `selectedLang`: 현재 언어
- `cart`: 장바구니 상태
- `onAddToCart`, `onRemoveFromCart`: 장바구니 조작 함수

**렌더링 요소:**
- 메뉴 이름, 설명, 가격 (다국어 지원)
- 별점 표시 (`item.rating`, `item.reviewCount`) - '*' 문자로 표시
- 리뷰 미리보기 (`item.reviews[0]`) - 국가 플래그와 함께
- 장바구니 추가/제거 버튼 (+/- 버튼)
- "View All" 버튼으로 전체 리뷰 보기

### 💳 **src/components/Payment/TossPaymentWidget.jsx - 토스페이 결제**
**State:**
- `paymentStep`: 결제 단계 ('confirm', 'api-call', 'processing')
- `countdown`: 처리 중 카운트다운 (3초)
- `tossPayments`: 토스페이먼츠 SDK 객체

**핵심 로직:**
- `handleConfirmPayment()`: SDK 호출 또는 시뮬레이션 처리
- `useEffect()`: 카운트다운 및 완료 처리
- `loadTossPayments()`: 토스 SDK 로드

### 📱 **src/utils/pushNotifications.js - 푸시 알림 시스템**
**주요 함수:**
- `registerServiceWorker()`: Service Worker 등록
- `requestNotificationPermission()`: 알림 권한 요청
- `subscribeToPush()`: 푸시 구독 생성 (VAPID 키 사용)
- `scheduleReviewReminder()`: 리뷰 리마인더 예약 (10초 후 알림)
- `initializePushNotifications()`: 푸시 알림 초기화

**VAPID 설정:**
```javascript
const VAPID_PUBLIC_KEY = 'BMUlRpAMJy1VfLrmZMvmC8I0JpbVCJoFVBKrGTGWG3dj...'
```

### 🛠️ **public/sw.js - Service Worker**
**이벤트 리스너:**
- `install`: Service Worker 설치
- `activate`: Service Worker 활성화
- `push`: 푸시 메시지 수신 처리
- `notificationclick`: 알림 클릭 처리 (`/?page=review`로 이동)

**알림 설정:**
```javascript
{
  title: 'VOYZ Restaurant',
  body: '리뷰를 작성해 주세요! 🍽️',
  actions: [
    { action: 'review', title: '리뷰 작성하기' },
    { action: 'dismiss', title: '나중에' }
  ]
}
```

### ⭐ **src/components/Review/WriteReviewPage.jsx - 리뷰 작성**
**State:**
- `selectedItemId`: 선택된 메뉴 아이템 ID
- `userName`: 리뷰어 이름
- `countryCode`: 국가 코드
- `reviewText`: 리뷰 내용
- `rating`: 별점 (1-5)

**Props:**
- `orderedItems`: 주문한 아이템 목록
- `onSubmitReview()`: 리뷰 제출 콜백
- `onSkip()`: 건너뛰기 콜백

---

## 📁 **2. Backend (Spring Boot) - 상세 분석**

### 🌐 **CalendarController.java - 캘린더 API**
**클래스:** `@RestController`, `@RequestMapping("/api/calendar")`

**주요 메서드:**
```java
@GetMapping("/day-suggestions")
public ResponseEntity<List<DaySuggestionDto>> getDaySuggestions(
    @RequestParam String date,
    @RequestParam String businessType
)
```
- 특정 날짜의 마케팅 제안 조회
- 업종별 맞춤 제안 제공

```java
@GetMapping("/weather-info")
public ResponseEntity<WeatherResponseDto> getWeatherInfo(
    @RequestParam double latitude,
    @RequestParam double longitude
)
```
- GPS 좌표 기반 날씨 정보 조회
- 기상청 API 연동

### 🏢 **WeatherService.java - 날씨 서비스**
**클래스:** `@Service`

**주요 메서드:**
```java
public CurrentWeatherDto getCurrentWeather(double lat, double lon)
```
- GPS 좌표를 격자 좌표로 변환
- 기상청 초단기실황 API 호출
- 온도, 습도, 강수량 정보 반환

```java
public ForecastResponseDto getForecast(double lat, double lon)
```
- 단기예보 API 호출
- 3일간 예보 데이터 제공

**의존성:**
- `WeatherRepository`: 날씨 데이터 저장
- `GeoConverter`: 좌표 변환
- `WeatherApi`: 외부 API 호출

### 🔧 **GeoConverter.java - 좌표 변환 유틸**
**클래스:** 정적 유틸리티 클래스

**주요 메서드:**
```java
public static int[] convertToGrid(double lat, double lon)
```
- WGS84 GPS 좌표를 기상청 격자 좌표로 변환
- Lambert Conformal Conic 투영법 사용
- 반환: `[gridX, gridY]`

**변환 상수:**
```java
private static final double RE = 6371.00877; // 지구 반지름
private static final double GRID = 5.0; // 격자 간격 (km)
```

### 🌤️ **WeatherApi.java - 기상청 API 연동**
**주요 메서드:**
```java
public String fetchCurrentWeather(int nx, int ny)
```
- 기상청 초단기실황 API 호출
- 파라미터: 격자 X, Y 좌표
- 반환: JSON 응답 문자열

**API 엔드포인트:**
- 초단기실황: `getUltraSrtNcst`
- 단기예보: `getVilageFcst`

### 📝 **Users.java - 사용자 엔터티**
**클래스:** `@Entity @Data` (JPA + Lombok)

**필드:**
```java
private String userId;        // 사용자 ID (Primary Key)
private String userPw;        // 사용자 비밀번호
private String userName;      // 사용자 이름
private String userPhone;     // 사용자 전화번호
private String storeName;     // 매장명
private String storeCategory; // 매장 카테고리
private String storeAddress;  // 매장 주소
private LocalDateTime createdAt; // 생성일시
```

### 🔒 **JwtTokenService.java - JWT 토큰 관리**
**주요 메서드:**
```java
public String generateToken(String username)
```
- JWT 토큰 생성
- 만료 시간 설정 (24시간)

```java
public boolean validateToken(String token)
```
- 토큰 유효성 검증
- 만료 시간 확인

---

## 📁 **3. Frontend (Android) - 상세 분석**

### 🎯 **MainActivity.kt - 메인 액티비티**
**클래스:** `ComponentActivity`

**onCreate() 구성:**
```kotlin
setContent {
    VoyzTheme {
        NavGraph()
    }
}
```

### 📅 **CalendarComponent.kt - 캘린더 컴포넌트**
**함수:** `@Composable fun CalendarComponent()`

**주요 기능:**
- 월별 캘린더 그리드 표시
- 드래그 제스처 감지 (`detectDragGestures`)
- 특일 마크 표시
- 애니메이션 효과 (`animateFloatAsState`)

**State:**
```kotlin
var currentDate by remember { mutableStateOf(LocalDate.now()) }
var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
var dragOffset by remember { mutableStateOf(Offset.Zero) }
```

### 🔐 **LoginScreen.kt - 로그인 화면**
**함수:** `@Composable fun LoginScreen()`

**UI 구성:**
- `OutlinedTextField`: 사용자명, 비밀번호 입력
- `Button`: 로그인 버튼
- `Checkbox`: 자동 로그인 체크박스
- `SnackbarHost`: 오류 메시지 표시

**상태 관리:**
```kotlin
val loginState by viewModel.loginState.collectAsState()
when (loginState) {
    is LoginState.Success -> { /* 성공 처리 */ }
    is LoginState.Error -> { /* 오류 처리 */ }
    LoginState.Loading -> { /* 로딩 표시 */ }
}
```

### 🌐 **ApiClient.kt - API 클라이언트**
**객체:** `object ApiClient`

**Retrofit 설정:**
```kotlin
private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .client(okHttpClient)
    .build()
```

**OkHttp 인터셉터:**
- 로깅 인터셉터
- 인증 토큰 자동 추가
- 타임아웃 설정 (30초)

### 💾 **UserPreferencesManager.kt - 설정 관리**
**클래스:** DataStore 기반 설정 관리

**주요 함수:**
```kotlin
suspend fun saveUserToken(token: String)
suspend fun getUserToken(): String?
suspend fun clearUserData()
```

**DataStore 키:**
```kotlin
private val USER_TOKEN = stringPreferencesKey("user_token")
private val AUTO_LOGIN = booleanPreferencesKey("auto_login")
```

---

## 📁 **4. ML 서비스 (FastAPI) - 상세 분석**

### 🎯 **main.py - FastAPI 메인 서버**
**앱 설정:**
```python
app = FastAPI(
    title="VOYZ ML Service",
    description="AI 기반 마케팅 제안 서비스",
    version="1.0.0"
)
```

**CORS 설정:**
```python
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)
```

### 🤖 **content_service.py - 컨텐츠 생성 서비스**
**클래스:** `ContentService`

**주요 메서드:**
```python
async def generate_content(self, special_day: str, business_type: str) -> ContentResponse
```
- OpenAI GPT-4o-mini 활용
- 특일별 맞춤 마케팅 컨텐츠 생성
- 신뢰도 점수 계산

**프롬프트 템플릿:**
```python
prompt = f"""
{special_day}에 맞는 {business_type} 업종의 마케팅 제안을 작성해주세요.
- 고객에게 어필할 수 있는 메시지
- 구체적인 이벤트 아이디어
- 예상 효과
"""
```

### 📊 **models/match_models.py - 매칭 모델**
**클래스:** `BasicMatchModel`

**매칭 알고리즘:**
```python
def match(self, special_day: dict, customer_data: dict) -> float
```
- 키워드 기반 유사도 계산
- TF-IDF 벡터화
- 코사인 유사도 측정

**반환값:** 0.0 ~ 1.0 사이의 매칭 점수

### ⚙️ **config.py - 설정 파일**
**환경 변수:**
```python
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
MODEL_NAME = "gpt-4o-mini"
MAX_TOKENS = 1000
TEMPERATURE = 0.7
```

**Pydantic 모델:**
```python
class SpecialDayRequest(BaseModel):
    date: str
    business_type: str
    customer_profile: Optional[dict] = None

class ContentResponse(BaseModel):
    content: str
    confidence: float
    suggestions: List[str]
```

---

## 🔗 **시스템 연동 구조**

### 📱 **Android ↔ Spring Boot**
```
CalendarApiService → CalendarController
UserApiService → AuthController
Retrofit HTTP Client → REST API
JWT Token → Spring Security
```

### 🌐 **Web App ↔ Spring Boot**
```
fetch() API → REST Endpoints
localStorage → JWT Token Storage
Payment APIs → External Payment Services
```

### 🤖 **Spring Boot ↔ ML Service**
```
FastApiClient → FastAPI Endpoints
HTTP JSON → Pydantic Models
Async Processing → OpenAI API
```

### 🛡️ **보안 계층**
```
JWT Authentication → All API Endpoints
CORS Policy → Cross-Origin Requests
Input Validation → Pydantic/Bean Validation
SQL Injection Prevention → MyBatis/JPA
```

---

> ⚠️ **주의**: 이 상세 인덱스는 최신이 아닐 수 있습니다. 정확한 함수 시그니처, 클래스 구조, API 스펙은 실제 소스 코드를 참조하시기 바랍니다.