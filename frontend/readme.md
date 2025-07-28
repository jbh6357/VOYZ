# VOYZ 안드로이드 프론트엔드

Spring Boot REST API 백엔드와 통신하는 VOYZ 플랫폼의 안드로이드 프론트엔드 애플리케이션입니다.

## 프로젝트 구조 (현재 상태 - 2025.07.28 리팩토링 후)

### ⚠️ 폴더 구조 정리 필요사항
> **현재 문제점**: fragment/ 와 screen/ 폴더가 혼재되어 있음. 동일한 용도(화면)인데 위치가 다름.
> **향후 계획**: fragment/ 폴더를 screen/으로 통합하고 카테고리별로 재정리 예정.

```text
app/src/main/java/com/voyz/
├── data/
│   ├── model/                    # 데이터 모델 (MarketingOpportunity, Priority 등)
│   └── repository/               # 데이터 저장소 (MarketingOpportunityRepository)
├── presentation/
│   ├── activity/
│   │   └── MainActivity.kt       # 메인 액티비티 (단일)
│   ├── component/                # 재사용 가능한 UI 컴포넌트
│   │   ├── calendar/            # 캘린더 컴포넌트 + ViewModel
│   │   ├── fab/                 # FloatingActionMenu
│   │   ├── gesture/             # 제스처 핸들러 (SidebarDragHandler)
│   │   ├── modal/               # 모달 컴포넌트들
│   │   ├── sidebar/             # 사이드바 + 상태 관리
│   │   └── topbar/              # 공통 상단바
│   ├── fragment/                # ⚠️ 기존 화면들 (정리 필요)
│   │   ├── AlramScreen.kt
│   │   ├── CustomerManagementScreen.kt
│   │   ├── IdPwFindScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── MainScreen_backup.kt  # 백업 파일
│   │   ├── MarketingCreateScreen.kt
│   │   ├── OperationManagementScreen.kt
│   │   ├── ReminderCreateScreen.kt
│   │   ├── ReminderScreen.kt
│   │   ├── SettingsScreen.kt
│   │   ├── SignUpScreen.kt
│   │   └── UserProfileScreen.kt
│   ├── screen/                  # 🔥 리팩토링된 화면들
│   │   ├── main/                # MainScreen 리팩토링 결과
│   │   │   ├── MainScreen.kt        # 메인 컨트롤러 (91줄)
│   │   │   ├── MainScreenState.kt   # 상태 관리 클래스
│   │   │   └── components/          # MainScreen 전용 컴포넌트
│   │   │       ├── MainContent.kt       # 메인 UI 컴포넌트
│   │   │       └── OverlayManager.kt    # 오버레이 관리
│   │   └── marketing/
│   │       └── MarketingOpportunityDetailScreen.kt
│   ├── navigation/
│   │   └── NavGraph.kt          # 네비게이션 설정
│   └── viewmodel/               # ⚠️ 현재 비어있음 (State 패턴 사용 중)
├── ui/theme/                    # 테마 및 색상 시스템
│   ├── Color.kt
│   ├── MarketingColors.kt       # 마케팅 전용 색상 시스템
│   └── Type.kt
└── utils/
    └── Constants.kt
```

### 📝 MainScreen 리팩토링 세부사항 (2025.07.28)

**기존 문제점:**
- MainScreen.kt가 226줄로 과도하게 복잡
- 사이드바, 모달, FAB, 캘린더 로직이 모두 한 곳에 집중
- 7개의 상태 변수가 분산 관리
- 드래그 제스처, 애니메이션 로직 혼재

**리팩토링 결과:**
- **MainScreen.kt**: 226줄 → 91줄 (60% 감소)
- **상태 관리 분리**: `MainScreenState.kt`로 모든 상태 캡슐화
- **UI 컴포넌트 분리**: `MainContent.kt`, `OverlayManager.kt`로 역할 분담
- **제스처 로직 분리**: `SidebarDragHandler.kt`로 독립화

**개선 효과:**
- 가독성 및 유지보수성 대폭 향상
- 테스트 용이성 증대 (각 컴포넌트 독립 테스트 가능)
- 재사용성 증대 (상태 클래스, 제스처 핸들러)
- 확장성 향상 (새 기능 추가 시 영향 범위 최소화)

### 🔧 향후 정리 계획

**1. 폴더 구조 통합**
```text
presentation/screen/
├── auth/                 # LoginScreen, SignUpScreen, IdPwFindScreen
├── main/                 # MainScreen (리팩토링 완료)
├── marketing/            # MarketingOpportunityDetailScreen, MarketingCreateScreen
├── management/           # CustomerManagement, OperationManagement, Settings
└── reminder/             # ReminderScreen, ReminderCreateScreen
```

**2. 불필요한 폴더 제거**
- `viewmodel/` 폴더 (현재 비어있음, State 패턴 사용)
- `component/gesture/` 폴더 (파일 1개만 존재)
- 과도한 depth 줄이기

**3. 네이밍 일관성**
- fragment/ → screen/ 통일
- 화면별 카테고리 명확화

### 🎨 UI/UX 개선사항

**마케팅 색상 시스템 구축:**
- 기존 랜덤 색상 → 전문적인 회색-파랑 계열 통일
- 카테고리별 일관된 색상 (특별한 날/공휴일만 강조색)
- 우선순위별 차분한 색상 시스템

**모바일 UX 최적화:**
- 양방향 스와이프 날짜 네비게이션 구현
- 모바일 친화적 모달 디자인 (핸들바, 둥근 모서리)
- 배경 터치로 모달 닫기 기능
- 부드러운 애니메이션 전환

### 🚀 기술 스택 및 패턴

**현재 사용 중인 패턴:**
- **Compose UI**: 선언적 UI 개발
- **State Pattern**: MainScreenState를 통한 상태 캡슐화  
- **Component Composition**: 재사용 가능한 컴포넌트 설계
- **Repository Pattern**: 데이터 접근 추상화

**아키텍처 특징:**
- MVVM 패턴 기반이지만 ViewModel 대신 State 클래스 활용
- 컴포넌트 중심 설계로 높은 재사용성
- 관심사의 분리를 통한 유지보수성 확보



## 아키텍처

이 앱은 MVVM 패턴과 함께 클린 아키텍처 원칙을 따릅니다:

- **데이터 레이어**: Spring Boot 백엔드와의 API 통신 처리
- **도메인 레이어**: 비즈니스 로직과 유스케이스 포함
- **프레젠테이션 레이어**: UI 컴포넌트 (액티비티, 프래그먼트, 뷰모델)

## 백엔드 통신

앱은 다음 기능을 위해 Spring Boot REST API 백엔드와 통신합니다:
- 사용자 인증 및 관리
- 데이터 조회 및 업데이트
- 비즈니스 로직 처리

## 설정

1. 리포지토리 클론
2. 안드로이드 스튜디오에서 열기
3. 프로젝트 빌드 및 실행

## API 설정

API 설정에서 베이스 URL을 Spring Boot 백엔드 서버로 변경하세요.

## 의존성

- Retrofit: REST API 통신
- MVVM 아키텍처 컴포넌트
- 의존성 주입 (Hilt/Dagger)
- 머티리얼 디자인 컴포넌트