"""
예측 모델 모듈
"""

import numpy as np
from typing import Dict, Any, List

class PredictionModels:
    """예측 모델 클래스"""
    
    @staticmethod
    def test_model(data: List[float]) -> Dict[str, Any]:
        """테스트 모델 - 간단한 평균 기반 예측"""
        data_array = np.array(data)
        prediction = float(np.mean(data_array))
        trend = "상승" if data_array[-1] > prediction else "하락"
        
        return {
            "prediction": prediction,
            "trend": trend,
            "confidence": 0.75,
            "model_type": "test"
        }
    
    @staticmethod
    def linear_model(data: List[float]) -> Dict[str, Any]:
        """선형 회귀 모델"""
        data_array = np.array(data)
        x = np.arange(len(data_array))
        coeffs = np.polyfit(x, data_array, 1)
        next_value = coeffs[0] * len(data_array) + coeffs[1]
        trend = "상승" if coeffs[0] > 0 else "하락"
        
        return {
            "prediction": float(next_value),
            "trend": trend,
            "confidence": 0.85,
            "model_type": "linear",
            "coefficients": coeffs.tolist()
        }
    
    @staticmethod
    def average_model(data: List[float]) -> Dict[str, Any]:
        """평균 기반 모델"""
        data_array = np.array(data)
        prediction = float(np.mean(data_array))
        trend = "상승" if data_array[-1] > prediction else "하락"
        
        return {
            "prediction": prediction,
            "trend": trend,
            "confidence": 0.70,
            "model_type": "average"
        }
    
    @staticmethod
    def get_model(model_name: str, data: List[float]) -> Dict[str, Any]:
        """모델명에 따른 예측 실행"""
        if model_name == "test":
            return PredictionModels.test_model(data)
        elif model_name == "linear":
            return PredictionModels.linear_model(data)
        elif model_name == "average":
            return PredictionModels.average_model(data)
        else:
            return {
                "error": f"지원하지 않는 모델명: {model_name}",
                "supported_models": ["test", "linear", "average"]
            } 