"""
FastAPI 설정 파일
"""

import os
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
from dotenv import load_dotenv

# .env 파일에서 환경 변수 로드
load_dotenv()

# 환경 변수 로드
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")

# API 설정
API_CONFIG = {
    "title": "VOIZ Special Day Matching API",
    "version": "1.0.0",
    "description": "특일 매칭 API 서버",
    "host": os.getenv("FASTAPI_HOST", "0.0.0.0"),
    "port": int(os.getenv("FASTAPI_PORT", "8000")),
    "debug": os.getenv("FASTAPI_DEBUG", "True").lower() == "true"
}

# OpenAI 설정
OPENAI_CONFIG = {
    "api_key": OPENAI_API_KEY,
    "model": "gpt-4o-mini",  # 최신 GPT-4o mini 모델 (빠르고 저렴)
    "max_tokens": 1000,
    "temperature": 0.7
} 

# 고객 - 데이터 매칭 데이터 모델 정의
class MatchRequestDto(BaseModel):
    userId: str
    storeCategory: str
    storeAddress: str

class MatchSpecialDayDto(BaseModel):
    sd_idx: int
    name: str
    category: Optional[str] = None

class SpecialDayMatchRequest(BaseModel):
    matchRequest: MatchRequestDto
    specialDays: List[MatchSpecialDayDto]

class SpecialDayMatch(BaseModel):
    sm_idx: int
    sd_idx: int
    userId: str

# Content 생성 요청 모델
class ContentGenerationRequest(BaseModel):
    name: str
    type: str
    category: Optional[str] = None
    startDate: str
    endDate: str

# 카테고리 분류 요청 모델
class CategoryClassificationRequest(BaseModel):
    name: str
    type: str
    category: Optional[str] = None

# 카테고리 분류 응답 모델
class CategoryClassificationResponse(BaseModel):
    success: bool
    categories: List[str]

# 제안 생성 요청 모델
class CreateSuggestRequest(BaseModel):
    name: str
    type: str
    storeCategory: str

# 제안 생성 응답 모델
class CreateSuggestResponse(BaseModel):
    success: bool
    title: str
    description: str
    targetCustomer: str
    suggestedAction: str
    expectedEffect: str
    confidence: float  # 문자열에서 숫자로 변경
    priority: str

# 메뉴 OCR 모델 설정
class MenuItem(BaseModel):
    name: str
    price: int