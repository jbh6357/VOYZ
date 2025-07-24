# VOIZ Data Analysis API

Spring Boot에서 호출할 수 있는 데이터 분석 API 서버

## 프로젝트 구조

```
ml/
├── main.py                 # 메인 FastAPI 애플리케이션
├── config.py              # 설정 파일
├── models/                # 분석 모델들
│   ├── __init__.py
│   ├── prediction_models.py
│   └── analysis_models.py
├── services/              # 서비스 레이어
│   ├── __init__.py
│   └── dashboard_service.py
└── README.md
```

아래는 실행 및 예시입니다.

## 설치 및 실행

### 1. 의존성 설치

```bash
pip install -r requirements.txt
```

### 2. 서버 실행

```bash
python main.py
```

서버가 `http://localhost:8000`에서 실행됩니다.

## API 엔드포인트

### 헬스 체크

-   `GET /health` - 서버 상태 확인

### 데이터 제공

-   `GET /api/data` - 샘플 데이터 반환

### 예측 API

-   `POST /api/predict/sales` - 매출 예측
-   `POST /api/predict/{model_name}` - 동적 모델명 예측 (Spring Boot 연동용)

### 분석 API

-   `POST /api/analysis/trend` - 트렌드 분석
-   `POST /api/analysis/correlation` - 상관관계 분석
-   `POST /api/analysis/segmentation` - 고객 세분화

### 대시보드

-   `GET /api/dashboard` - 실시간 대시보드 데이터

## 지원하는 모델

-   **test**: 테스트 모델 - 간단한 평균 기반 예측
-   **linear**: 선형 회귀 모델 - 추세 기반 예측
-   **average**: 평균 기반 모델 - 평균값 예측

## Spring Boot 연동

Spring Boot에서 다음과 같이 호출할 수 있습니다:

```bash
curl -X POST "http://localhost:8000/api/predict/test" \
  -H "Content-Type: application/json" \
  -d '{"data": [100, 150, 200, 180, 220, 250, 280, 300]}'
```

## 개발 가이드

### 새로운 모델 추가

1. `models/prediction_models.py`에 새로운 모델 메서드 추가
2. `config.py`의 `SUPPORTED_MODELS`에 모델 정보 추가
3. `models/prediction_models.py`의 `get_model` 메서드에 분기 추가

### 새로운 분석 기능 추가

1. `models/analysis_models.py`에 새로운 분석 메서드 추가
2. `main.py`에 새로운 엔드포인트 추가
