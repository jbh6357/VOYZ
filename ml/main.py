"""
FastAPI 메인 애플리케이션
특일 매칭 API 서버
"""

from fastapi import FastAPI, HTTPException
from typing import Dict, Any, List
from datetime import datetime

# 설정 및 모델 import
from config import API_CONFIG, SpecialDayMatchRequest, ContentGenerationRequest, CategoryClassificationRequest
from models.match_models import MatchModels
from services.content_service import ContentService
from services.category_service import CategoryService

# FastAPI 앱 생성
app = FastAPI(
    title=API_CONFIG["title"],
    version=API_CONFIG["version"],
    description=API_CONFIG["description"]
)

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

# 특일 컨텐츠 생성
@app.post("/api/content/generate")
def generate_content(request: ContentGenerationRequest):
    """ 특일 컨텐츠 생성 """
    try:
        content = ContentService.generate_special_day_content(
            name=request.name,
            type_name=request.type,
            category=request.category
        )
        
        return {
            "success": True,
            "content": content
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"컨텐츠 생성 실패: {str(e)}")

# 특일 카테고리 분류
@app.post("/api/category/classify")
def classify_categories(request: CategoryClassificationRequest):
    """ 특일 카테고리 분류 """
    try:
        categories = CategoryService.classify_special_day_categories(
            name=request.name,
            type_name=request.type,
            category=request.category
        )
        
        return {
            "success": True,
            "categories": categories
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"카테고리 분류 실패: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        app, 
        host=API_CONFIG["host"], 
        port=API_CONFIG["port"]
    ) 