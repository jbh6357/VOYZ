"""
FastAPI 설정 파일
"""

from pydantic import BaseModel
from typing import List, Dict, Any, Optional

# API 설정
API_CONFIG = {
    "title": "VOIZ Special Day Matching API",
    "version": "1.0.0",
    "description": "특일 매칭 API 서버",
    "host": "0.0.0.0",
    "port": 8000,
    "debug": True
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