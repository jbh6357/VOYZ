"""
분석 모델 모듈
"""

import numpy as np
from typing import Dict, Any, List

class AnalysisModels:
    """분석 모델 클래스"""
    
    @staticmethod
    def trend_analysis(data: List[float]) -> Dict[str, Any]:
        """데이터 트렌드 분석"""
        data_array = np.array(data)
        
        # 기본 통계
        mean_val = np.mean(data_array)
        std_val = np.std(data_array)
        min_val = np.min(data_array)
        max_val = np.max(data_array)
        
        # 트렌드 분석
        if data_array[-1] > data_array[0]:
            trend = "상승"
            growth_rate = ((data_array[-1] - data_array[0]) / data_array[0]) * 100
        else:
            trend = "하락"
            growth_rate = ((data_array[0] - data_array[-1]) / data_array[0]) * 100
        
        # 변동성 분석
        volatility = (std_val / mean_val) * 100
        
        return {
            "trend": trend,
            "growth_rate": float(growth_rate),
            "volatility": float(volatility),
            "statistics": {
                "mean": float(mean_val),
                "std": float(std_val),
                "min": float(min_val),
                "max": float(max_val)
            },
            "data": data_array.tolist()
        }
    
    @staticmethod
    def correlation_analysis(data1: List[float], data2: List[float]) -> Dict[str, Any]:
        """두 데이터 간의 상관관계 분석"""
        array1 = np.array(data1)
        array2 = np.array(data2)
        
        correlation = np.corrcoef(array1, array2)[0, 1]
        
        # 상관관계 강도 판단
        if abs(correlation) > 0.7:
            strength = "강함"
        elif abs(correlation) > 0.3:
            strength = "보통"
        else:
            strength = "약함"
        
        return {
            "correlation": float(correlation),
            "strength": strength,
            "interpretation": f"상관계수 {correlation:.3f}로 {strength}한 상관관계를 보입니다"
        }
    
    @staticmethod
    def customer_segmentation(customers: List[Dict[str, Any]]) -> Dict[str, Any]:
        """고객 세분화 분석"""
        total_spending = []
        purchase_frequency = []
        
        for customer in customers:
            total_spending.append(customer.get("total_spending", 0))
            purchase_frequency.append(customer.get("purchase_frequency", 0))
        
        # 세분화 기준
        avg_spending = np.mean(total_spending)
        avg_frequency = np.mean(purchase_frequency)
        
        segments = []
        for i, customer in enumerate(customers):
            spending = total_spending[i]
            frequency = purchase_frequency[i]
            
            if spending > avg_spending and frequency > avg_frequency:
                segment = "VIP"
            elif spending > avg_spending or frequency > avg_frequency:
                segment = "일반"
            else:
                segment = "신규"
            
            segments.append({
                "customer_id": customer.get("id", i),
                "segment": segment,
                "total_spending": spending,
                "purchase_frequency": frequency
            })
        
        return {
            "segments": segments,
            "segment_summary": {
                "VIP": len([s for s in segments if s["segment"] == "VIP"]),
                "일반": len([s for s in segments if s["segment"] == "일반"]),
                "신규": len([s for s in segments if s["segment"] == "신규"])
            },
            "averages": {
                "avg_spending": float(avg_spending),
                "avg_frequency": float(avg_frequency)
            }
        } 