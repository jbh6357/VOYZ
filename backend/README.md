# VOIZ Backend

소규모 비즈니스를 위한 에이전트 백엔드 애플리케이션

## 프로젝트 구조

```
src/main/java/com/voiz/
├── config/          # 설정 클래스들
├── controller/      # REST API 컨트롤러
├── dto/            # Data Transfer Objects
├── mapper/         # Repository 인터페이스
├── service/        # 비즈니스 로직 서비스
├── util/           # 유틸리티 클래스
└── vo/             # Value Objects (엔티티)
```

## 기술 스택

-   **Spring Boot 3.5.3**
-   **Spring Data JPA**
-   **Spring Security**
-   **Oracle Database**
-   **Swagger/OpenAPI 3**
-   **Lombok**

## 데이터베이스 설정

-   **URL**: project-db-campus.smhrd.com:1523
-   **Username**: jung
-   **Password**: smhrd3
-   **Database**: xe

## API 엔드포인트

### 사용자 관리

-   `POST /api/users/register` - 회원가입
-   `POST /api/users/login` - 로그인
-   `GET /api/users/test` - API 테스트

### 데이터 분석 (FastAPI 연동)

-   `GET /api/analysis/data` - FastAPI에서 데이터 가져오기
-   `POST /api/analysis/predict` - 예측 모델 실행
-   `POST /api/analysis/analyze` - 데이터 분석 실행
-   `GET /api/analysis/test` - FastAPI 연결 테스트

## Swagger UI

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

-   http://localhost:8080/swagger-ui.html

## FastAPI 연동 예시

### 1. FastAPI 서버 설정 예시

```python
# main.py
from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
import numpy as np

app = FastAPI()

class PredictionRequest(BaseModel):
    data: list

@app.get("/health")
def health_check():
    return {"status": "healthy"}

@app.get("/api/data")
def get_data():
    # 예시 데이터 반환
    return {
        "sales_data": [100, 150, 200, 180, 220],
        "dates": ["2024-01", "2024-02", "2024-03", "2024-04", "2024-05"]
    }

@app.post("/api/predict/sales")
def predict_sales(request: PredictionRequest):
    # 간단한 예측 모델 (예시)
    data = np.array(request.data)
    prediction = np.mean(data) * 1.1  # 10% 증가 예측
    return {"prediction": float(prediction)}

@app.post("/api/analysis/trend")
def analyze_trend(request: PredictionRequest):
    data = np.array(request.data)
    trend = "상승" if data[-1] > data[0] else "하락"
    return {"trend": trend, "data": data.tolist()}
```

### 2. Spring Boot에서 FastAPI 호출 예시

```java
// AnalysisController에서 사용 예시
@PostMapping("/predict-sales")
public ResponseEntity<String> predictSales(@RequestBody List<Double> salesData) {
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("data", salesData);

    ResponseEntity<String> response = fastApiClient.getPrediction("sales", requestData);
    return ResponseEntity.ok(response.getBody());
}

@PostMapping("/analyze-trend")
public ResponseEntity<String> analyzeTrend(@RequestBody List<Double> data) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("data", data);

    ResponseEntity<String> response = fastApiClient.getAnalysisResult("trend", parameters);
    return ResponseEntity.ok(response.getBody());
}
```

### 3. FastAPI 실행 방법

```bash
# FastAPI 서버 실행
pip install fastapi uvicorn pandas numpy
uvicorn main:app --host 0.0.0.0 --port 8000
```

### 4. 데이터 분석 파이프라인 예시

```python
# analysis_service.py
import pandas as pd
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import StandardScaler

class AnalysisService:
    def __init__(self):
        self.model = LinearRegression()
        self.scaler = StandardScaler()

    def predict_sales(self, historical_data):
        # 시계열 데이터를 특성으로 변환
        df = pd.DataFrame(historical_data, columns=['sales'])
        df['month'] = range(len(df))

        X = df[['month']].values
        y = df['sales'].values

        # 모델 학습
        self.model.fit(X, y)

        # 다음 달 예측
        next_month = len(df)
        prediction = self.model.predict([[next_month]])[0]

        return {
            "prediction": float(prediction),
            "confidence": 0.85,
            "trend": "상승" if prediction > y[-1] else "하락"
        }

    def analyze_correlation(self, data1, data2):
        correlation = np.corrcoef(data1, data2)[0, 1]
        return {
            "correlation": float(correlation),
            "strength": "강함" if abs(correlation) > 0.7 else "보통" if abs(correlation) > 0.3 else "약함"
        }
```

## 실행 방법

1. **Oracle Database 연결 확인**
2. **FastAPI 서버 실행** (포트 8000)
3. **Spring Boot 애플리케이션 실행**

```bash
# Spring Boot 실행
./mvnw spring-boot:run
```

## 테스트

### 1. 회원가입 테스트

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "name": "테스트 사용자",
    "role": "USER"
  }'
```

### 2. 로그인 테스트

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 3. FastAPI 연동 테스트

```bash
curl -X GET "http://localhost:8080/api/analysis/test"
```

## 주의사항

1. **Oracle Database 연결**: 네트워크 연결이 가능한지 확인
2. **FastAPI 서버**: 데이터 분석 서버가 실행 중인지 확인
3. **포트 충돌**: 8080번 포트가 사용 가능한지 확인
4. **의존성**: Maven 의존성이 모두 다운로드되었는지 확인
