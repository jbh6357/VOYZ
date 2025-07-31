"""
매칭 모델 모듈
"""

import numpy as np
from typing import Dict, Any, List

class MatchModels:
    """매칭 모델 클래스"""
    
    @staticmethod
    def specialDay_model(event: str, store_type: str) -> int:
        """매칭 모델"""
        keywords = ["초복", "중복", "말복"]
        if any(bok in event for bok in keywords) and ("치킨집" in store_type or "치킨" in store_type):
            return 1
        return 0
        