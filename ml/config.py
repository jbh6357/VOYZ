"""
FastAPI 설정 파일
"""

from pydantic import BaseModel
from typing import List, Dict, Any

# API 설정
API_CONFIG = {
    "title": "VOIZ Data Analysis API",
    "version": "1.0.0",
    "description": "Spring Boot에서 호출할 수 있는 데이터 분석 API 서버",
    "host": "0.0.0.0",
    "port": 8000,
    "debug": True
}

# 지원하는 모델 타입
SUPPORTED_MODELS = {
    "test": "테스트 모델 - 간단한 평균 기반 예측",
    "linear": "선형 회귀 모델 - 추세 기반 예측",
    "average": "평균 기반 모델 - 평균값 예측"
}

# 데이터 모델 정의
class PredictionRequest(BaseModel):
    data: List[float]
    model_type: str = "linear"

class AnalysisRequest(BaseModel):
    data: List[float]
    analysis_type: str

class SalesData(BaseModel):
    sales: List[float]
    dates: List[str]

# 샘플 데이터
SAMPLE_DATA = {
    "sales_data": [100, 150, 200, 180, 220, 250, 280, 300],
    "dates": [
        "2024-01", "2024-02", "2024-03", "2024-04",
        "2024-05", "2024-06", "2024-07", "2024-08"
    ],
    "categories": ["전자제품", "의류", "식품", "가구"],
    "category_sales": [45, 30, 15, 10]
} 