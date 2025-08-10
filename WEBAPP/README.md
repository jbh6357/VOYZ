# VOYZ Web Application

외국인 고객 유치를 위한 요식업 마케팅 플랫폼의 웹 애플리케이션입니다. QR 코드 스캔을 통해 접속하는 모바일 웹 주문 시스템입니다.

## 주요 기능

- **QR 기반 메뉴 시스템**: 테이블별 QR 코드를 통한 즉시 접속
- **다국어 지원**: 한국어, 영어, 중국어, 일본어 자동 감지 및 번역
- **실시간 주문**: 메뉴 선택부터 결제까지 원스톱 처리
- **다중 결제 옵션**: 토스페이먼츠, PayPal 지원
- **리뷰 시스템**: 주문 후 다국어 리뷰 작성
- **푸시 알림**: 웹 푸시를 통한 리뷰 리마인더

## 기술 스택

- **프레임워크**: React 18 + Vite
- **스타일링**: CSS Variables, 반응형 디자인
- **결제**: TossPayments SDK, PayPal SDK
- **알림**: Web Push API, Service Worker
- **HTTP 클라이언트**: Fetch API (브라우저 내장)

## 프로젝트 구조

```
WEBAPP/src/
├── api/                # API 호출 관련
│   └── menu.js        # 메뉴 API 서비스
├── components/         # 재사용 컴포넌트
│   ├── Menu/          # 메뉴 관련 컴포넌트
│   ├── Payment/       # 결제 관련 컴포넌트
│   ├── Review/        # 리뷰 관련 컴포넌트
│   └── UI/            # 공통 UI 컴포넌트
├── config/            # 환경 설정
│   └── api.js         # API 설정
├── constants/         # 상수 및 샘플 데이터
│   └── sampleData.js  # 샘플 메뉴 데이터
├── hooks/             # 커스텀 훅
│   └── useMenu.js     # 메뉴 상태 관리
├── pages/             # 페이지 컴포넌트
│   ├── OrderPage/     # 주문 페이지
│   ├── ReviewPage/    # 리뷰 작성 페이지
│   └── SuccessPage/   # 주문 완료 페이지
├── types/             # 타입 정의
├── utils/             # 유틸리티 함수
│   ├── helpers.js     # 일반 헬퍼 함수
│   └── pushNotifications.js  # 푸시 알림 관리
├── App.jsx            # 메인 앱 컴포넌트
└── main.jsx           # 애플리케이션 진입점
```

## 개발 환경 설정

### 필수 요구사항
- Node.js 16.0 이상
- npm 또는 yarn

### 설치 및 실행

```bash
# 의존성 설치
npm install

# 개발 서버 실행
npm run dev

# 프로덕션 빌드
npm run build

# 빌드 결과 미리보기
npm run preview
```

### 환경 변수

개발/프로덕션 환경에 따른 API URL 자동 설정:
- 개발환경: `http://localhost:8081/api`
- 프로덕션: `http://13.125.251.36:8081/api`

## API 연동

### 메뉴 데이터 조회
```javascript
import { getMenusByUserId } from './api/menu.js';

const menuData = await getMenusByUserId('user@example.com');
```

### URL 파라미터 처리
QR 코드 스캔 시 다음 형태의 URL로 접속:
```
http://localhost:5173/?userId=restaurant@gmail.com&table=1
```

## 주요 컴포넌트

### MenuSection
메뉴 카테고리별 아이템 표시 컴포넌트

### MenuItem
개별 메뉴 아이템 표시 및 장바구니 추가 기능

### OrderPage
장바구니 확인 및 주문 진행 페이지

### PaymentModal
결제 방법 선택 모달 (토스페이먼츠/PayPal)

### ReviewModal
메뉴별 리뷰 확인 모달

## 데이터 처리

### Null 값 처리
- 메뉴 설명 없음: "메뉴 설명 준비중입니다"
- 평점 없음: "평가 대기중" 상태 표시
- 리뷰 없음: "아직 리뷰가 없습니다 (주문 후 리뷰 작성 가능)"

### 다국어 지원
자동 언어 감지 및 메뉴/리뷰 번역 기능 제공

## PWA 기능

- 웹 푸시 알림 지원
- 오프라인 캐싱 (Service Worker)
- 모바일 앱과 유사한 사용자 경험

## 브라우저 지원

- Chrome 60+
- Safari 12+
- Firefox 60+
- Edge 79+

모바일 브라우저 최적화로 iOS Safari 10.3+, Android Chrome 42+ 지원

## 성능 최적화

- Vite를 통한 빠른 번들링
- 코드 스플리팅으로 초기 로딩 시간 단축
- 이미지 최적화 및 지연 로딩
- CSS Variables를 통한 테마 관리

## 배포

Spring Boot 백엔드 서버의 static 리소스로 통합 배포되며, GitHub Actions을 통한 자동 배포 파이프라인을 지원합니다.
