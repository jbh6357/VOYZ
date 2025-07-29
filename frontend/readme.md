# VOYZ 안드로이드 프론트엔드

Spring Boot REST API 백엔드와 통신하는 VOYZ 플랫폼의 안드로이드 프론트엔드 애플리케이션입니다.

## 프로젝트 구조 (현재 상태 - 2025.07.29 리팩토링 후)

### ✅ 폴더 구조 정리 완료
> **완료된 작업**: fragment/ 폴더를 screen/으로 통합하고 카테고리별로 재정리 완료
> **개선 효과**: 화면 관련 파일들의 일관된 구조와 명확한 카테고리 분류

```text
app/src/main/java/com/voyz/
├── datas/
│   ├── datastore/               # 사용자 환경설정 저장소
│   ├── mapper/                  # 데이터 변환 로직
│   ├── model/                   # 데이터 모델 (MarketingOpportunity, Priority 등)
│   │   └── dto/                 # Data Transfer Objects  
│   ├── network/                 # API 통신 클래스들
│   └── repository/              # 데이터 저장소 패턴
├── presentation/
│   ├── activity/
│   │   └── MainActivity.kt      # 메인 액티비티 (단일)
│   ├── component/               # 재사용 가능한 UI 컴포넌트
│   │   ├── calendar/           # 캘린더 컴포넌트 + ViewModel
│   │   │   └── components/     # ✅ 분리된 캘린더 하위 컴포넌트들
│   │   │       ├── CalendarDayCell.kt    # 날짜 셀 컴포넌트
│   │   │       └── CalendarGrid.kt       # 캘린더 그리드
│   │   ├── fab/                # FloatingActionMenu
│   │   ├── modal/              # 모달 컴포넌트들
│   │   ├── reminder/           # 리마인더 관련 컴포넌트들
│   │   ├── sidebar/            # 사이드바 + 상태 관리
│   │   └── topbar/             # 공통 상단바
│   ├── screen/                 # ✅ 화면들 (카테고리별 정리 완료)
│   │   ├── auth/               # 인증 관련 화면들
│   │   │   ├── LoginScreen.kt
│   │   │   ├── LoginViewModel.kt
│   │   │   ├── IdPwFindScreen.kt
│   │   │   └── signup/         # 회원가입 관련
│   │   ├── main/               # 메인 화면 (리팩토링 완료)
│   │   │   ├── MainScreen.kt       # 메인 컨트롤러 (91줄)
│   │   │   ├── MainScreenState.kt  # 상태 관리 클래스
│   │   │   ├── SearchScreen.kt
│   │   │   └── components/         # MainScreen 전용 컴포넌트
│   │   ├── marketing/          # 마케팅 관련 화면들
│   │   │   ├── MarketingOpportunityDetailScreen.kt
│   │   │   └── MarketingCreateScreen.kt
│   │   ├── management/         # 관리 화면들
│   │   │   ├── CustomerManagementScreen.kt
│   │   │   ├── OperationManagementScreen.kt
│   │   │   ├── SettingsScreen.kt
│   │   │   └── UserProfileScreen.kt
│   │   └── reminder/           # 리마인더 관련 화면들
│   │       ├── ReminderScreen.kt
│   │       ├── ReminderCreateScreen.kt
│   │       ├── ReminderDetailScreen.kt
│   │       └── AlramScreen.kt
│   └── navigation/
│       └── NavGraph.kt         # 네비게이션 설정
├── ui/theme/                   # 테마 및 색상 시스템
│   ├── Color.kt
│   ├── MarketingColors.kt      # 마케팅 전용 색상 시스템
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

### ✅ 완료된 리팩토링 작업들 (2025.07.29)

**1. 폴더 구조 통합 완료**
- ✅ fragment/ → screen/ 통합 완료
- ✅ 화면별 카테고리 분류 (auth, main, marketing, management, reminder)
- ✅ 일관된 네이밍 규칙 적용

**2. 불필요한 폴더 제거 완료**
- ✅ `viewmodel/` 폴더 제거 (State 패턴 사용)
- ✅ `domain/` 폴더 제거 (빈 폴더)
- ✅ `di/` 폴더 제거 (빈 폴더)

**3. 컴포넌트 분리 및 최적화**
- ✅ CalendarComponent (498줄) → CalendarDayCell, CalendarGrid로 분리
- ✅ 재사용 가능한 하위 컴포넌트들을 components/ 폴더로 구조화

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