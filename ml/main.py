"""
FastAPI 메인 애플리케이션
Spring Boot에서 호출할 수 있는 데이터 분석 API 서버
"""

from fastapi import FastAPI, HTTPException
from typing import Dict, Any, List
from datetime import datetime

# 설정 및 모델 import
from config import API_CONFIG, SAMPLE_DATA, PredictionRequest, AnalysisRequest, SpecialDayMatchRequest
from models.prediction_models import PredictionModels
from models.analysis_models import AnalysisModels
from services.dashboard_service import DashboardService
from models.match_models import MatchModels

# FastAPI 앱 생성
app = FastAPI(
    title=API_CONFIG["title"],
    version=API_CONFIG["version"],
    description=API_CONFIG["description"]
)

# 헬스 체크
@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "service": API_CONFIG["title"]
    }

# 기본 데이터 제공
@app.get("/api/data")
def get_sample_data():
    """샘플 데이터 반환"""
    return SAMPLE_DATA

# 매출 예측
@app.post("/api/predict/sales")
def predict_sales(request: PredictionRequest):
    """매출 예측 모델"""
    try:
        if len(request.data) < 2:
            raise HTTPException(status_code=400, detail="최소 2개 이상의 데이터가 필요합니다")
        
        result = PredictionModels.get_model(request.model_type, request.data)
        
        if "error" in result:
            raise HTTPException(status_code=400, detail=result["error"])
        
        return result
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"예측 중 오류 발생: {str(e)}")

# 동적 모델명을 받는 예측 엔드포인트 (Spring Boot 연동용)
@app.post("/api/predict/{model_name}")
def predict_with_model(model_name: str, request: Dict[str, Any]):
    """동적 모델명을 받는 예측 엔드포인트"""
    try:
        # 요청 데이터에서 필요한 정보 추출
        data = request.get("data", [])
        if not data:
            # 데이터가 없으면 샘플 데이터 사용
            data = SAMPLE_DATA["sales_data"]
        
        if len(data) < 2:
            raise HTTPException(status_code=400, detail="최소 2개 이상의 데이터가 필요합니다")
        
        result = PredictionModels.get_model(model_name, data)
        
        if "error" in result:
            return {
                "model_name": model_name,
                "error": result["error"],
                "supported_models": result["supported_models"],
                "status": "error"
            }
        
        # 성공 응답에 추가 정보 포함
        result.update({
            "model_name": model_name,
            "input_data": data,
            "status": "success"
        })
        
        return result
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"예측 중 오류 발생: {str(e)}")

# 트렌드 분석
@app.post("/api/analysis/trend")
def analyze_trend(request: AnalysisRequest):
    """데이터 트렌드 분석"""
    try:
        if len(request.data) < 2:
            raise HTTPException(status_code=400, detail="최소 2개 이상의 데이터가 필요합니다")
        
        return AnalysisModels.trend_analysis(request.data)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"분석 중 오류 발생: {str(e)}")

# 상관관계 분석
@app.post("/api/analysis/correlation")
def analyze_correlation(request: Dict[str, List[float]]):
    """두 데이터 간의 상관관계 분석"""
    try:
        if "data1" not in request or "data2" not in request:
            raise HTTPException(status_code=400, detail="data1과 data2가 필요합니다")
        
        data1 = request["data1"]
        data2 = request["data2"]
        
        if len(data1) != len(data2):
            raise HTTPException(status_code=400, detail="두 데이터의 길이가 같아야 합니다")
        
        return AnalysisModels.correlation_analysis(data1, data2)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"상관관계 분석 중 오류 발생: {str(e)}")

# 고객 세분화 분석
@app.post("/api/analysis/segmentation")
def customer_segmentation(request: Dict[str, Any]):
    """고객 세분화 분석"""
    try:
        customers = request.get("customers", [])
        
        if not customers:
            raise HTTPException(status_code=400, detail="고객 데이터가 필요합니다")
        
        return AnalysisModels.customer_segmentation(customers)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"세분화 분석 중 오류 발생: {str(e)}")

# 실시간 대시보드 데이터
@app.get("/api/dashboard")
def get_dashboard_data():
    """대시보드용 실시간 데이터"""
    return DashboardService.get_dashboard_data()

# 특일 - 고객 매칭
@app.post("/api/match/specialDay")
def specialDay_match(request: SpecialDayMatchRequest):
    """ 특일 - 고객 데이터 매칭"""
    result = []
    for i, day in enumerate(request.specialDays):
        # llama 모델로 판단
        judgement = MatchModels.specialDay_model(day.name, request.matchRequest.storeCategory)

        if judgement == 1:
            result.append({
                 "sd_idx": day.sd_idx,
                 "userId": request.matchRequest.userId
            })

    return result

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        app, 
        host=API_CONFIG["host"], 
        port=API_CONFIG["port"]
    ) 