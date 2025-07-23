"""
대시보드 서비스 모듈
"""

import numpy as np
from datetime import datetime, timedelta
from typing import Dict, Any, List

class DashboardService:
    """대시보드 서비스 클래스"""
    
    @staticmethod
    def get_dashboard_data() -> Dict[str, Any]:
        """대시보드용 실시간 데이터"""
        # 현재 시간 기준으로 가상의 실시간 데이터 생성
        now = datetime.now()
        
        # 시간별 매출 데이터 (최근 24시간)
        hourly_sales = []
        for i in range(24):
            hour = now - timedelta(hours=23-i)
            # 가상의 매출 데이터 (실제로는 DB에서 가져와야 함)
            sales = np.random.normal(1000, 200)
            hourly_sales.append({
                "hour": hour.strftime("%H:00"),
                "sales": max(0, int(sales))
            })
        
        # 카테고리별 매출
        categories = ["전자제품", "의류", "식품", "가구", "화장품"]
        category_sales = []
        for category in categories:
            category_sales.append({
                "category": category,
                "sales": int(np.random.uniform(500, 2000))
            })
        
        return {
            "timestamp": now.isoformat(),
            "total_sales_today": sum([h["sales"] for h in hourly_sales]),
            "hourly_sales": hourly_sales,
            "category_sales": category_sales,
            "top_products": [
                {"name": "스마트폰", "sales": 150},
                {"name": "노트북", "sales": 120},
                {"name": "태블릿", "sales": 90}
            ]
        } 