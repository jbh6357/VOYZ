# VOIZ Special Day Matching API

Spring Boot에서 호출할 수 있는 특일 매칭 API 서버

## 프로젝트 구조

```
ml/
├── main.py                 # 메인 FastAPI 애플리케이션
├── config.py              # 설정 파일
├── models/                # 매칭 모델
│   ├── __init__.py
│   └── match_models.py
├── services/              # 서비스 레이어
│   └── __init__.py
└── README.md
```

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

### 특일 매칭 API

-   `POST /api/match/specialDay` - 특일과 고객 데이터 매칭

## Spring Boot 연동

Spring Boot에서 다음과 같이 호출할 수 있습니다:

```bash
curl -X POST "http://localhost:8000/api/match/specialDay" \
  -H "Content-Type: application/json" \
  -d '{
    "matchRequest": {
      "userId": "user123",
      "storeCategory": "식당",
      "storeAddress": "서울시 강남구"
    },
    "specialDays": [
      {
        "sd_idx": 1,
        "name": "크리스마스",
        "category": "기념일"
      }
    ]
  }'
```
