# VOYZ 안드로이드 프론트엔드

Spring Boot REST API 백엔드와 통신하는 VOYZ 플랫폼의 안드로이드 프론트엔드 애플리케이션입니다.

## 프로젝트 구조

```text
app/src/main/java/com/voyz/
├── data/
│   ├── api/
│   ├── repository/
│   └── model/
├── domain/
│   ├── usecase/
│   └── repository/
├── presentation/
│   ├── viewmodel/
│   ├── activity/
│   ├── fragment/
│   └── navigation/
├── utils/
└── di/
```

### 디렉토리 설명

#### data/
외부 데이터 소스를 다루는 계층으로, 네트워크 통신 및 데이터 가공 책임을 집니다.

- **api/**: REST API와 통신하는 인터페이스 및 구현체 (예: Retrofit 사용).
- **repository/**: 도메인 레이어의 repository 인터페이스를 실제로 구현한 클래스들이 위치합니다.
- **model/**: 서버 응답, 요청, DB 엔티티 등 순수 데이터 구조(DTO, Entity)를 정의합니다.

#### domain/
비즈니스 로직을 담당하는 계층입니다. 외부 라이브러리나 안드로이드 프레임워크에 의존하지 않으며, 핵심 로직만을 정의합니다.

- **usecase/**: 애플리케이션의 구체적인 기능 단위 (예: 로그인, 데이터 조회 등).
- **repository/**: 비즈니스 로직에서 필요로 하는 데이터 소스를 추상화한 인터페이스를 정의합니다.

#### presentation/
사용자 인터페이스와 관련된 요소들이 위치하는 계층입니다. MVVM 패턴을 따릅니다.

- **viewmodel/**: UI 상태를 관리하고, 도메인 레이어의 유스케이스를 호출하는 로직을 포함합니다.
- **activity/**: 단일 화면을 구성하는 Activity 클래스들이 위치합니다.
- **fragment/**: 화면 내 여러 UI 구성 단위를 나누는 Fragment 클래스들이 위치합니다.
- **navigation/**: 앱 내 화면 간 전환(Navigation Graph 등)을 정의하며, 네비게이션 흐름을 관리합니다.

#### utils/
전역적으로 사용할 수 있는 유틸리티 클래스, 확장 함수, 공통 상수 등을 모아둡니다.

#### di/
의존성 주입 설정 파일들이 위치합니다. Hilt, Dagger, Koin 등의 DI 프레임워크를 사용할 경우 이곳에 모듈을 정의합니다.

### 아키텍처 흐름 요약

```
[ UI ] ← ViewModel ← UseCase ← Repository Interface ← Repository 구현 ← API/DB
       └──── presentation ────┘   └───── domain ─────┘    └──── data ─────┘
```

- presentation은 domain에 의존하지만, 그 반대는 아닙니다.
- 의존성은 항상 바깥에서 안쪽 방향으로만 흐릅니다.
- 클린 아키텍처 및 MVVM 패턴을 기반으로 구조화되어 있습니다.



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