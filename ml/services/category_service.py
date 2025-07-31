"""
특일 카테고리 분류 서비스
"""

import os
import requests
import json
from typing import List, Optional

class CategoryService:
    """GPT를 활용한 특일 카테고리 분류 서비스"""
    
    # 가능한 음식 카테고리 목록
    FOOD_CATEGORIES = [
        "한식", "중식", "일식", "양식", "카페", 
        "치킨", "피자", "버거", "분식"
    ]
    
    @staticmethod
    def classify_special_day_categories(name: str, type_name: str, category: Optional[str] = None) -> List[str]:
        """
        특일에 대한 음식 카테고리 분류
        
        Args:
            name: 특일명 (예: 초복, 크리스마스)
            type_name: 특일 유형 (예: 절기, 기념일)
            category: 카테고리 (선택)
            
        Returns:
            List[str]: 분류된 카테고리 목록 (예: ["한식", "치킨"])
        """
        
        # 환경변수에서 API 키 가져오기
        api_key = os.getenv("OPENAI_API_KEY")
        if not api_key:
            print("OpenAI API 키가 설정되지 않았습니다.")
            return []  # API 키 없으면 빈 배열
        
        print(f"특일 카테고리 분류 시작 - name: {name}, type: {type_name}, category: {category}")
        
        # GPT 프롬프트 구성
        categories_str = ", ".join(CategoryService.FOOD_CATEGORIES)
        prompt = f"""다음 특일에 적합한 음식 카테고리들을 선택해주세요.

특일명: {name}
유형: {type_name}
{f'카테고리: {category}' if category else ''}

선택 가능한 카테고리: {categories_str}

분류 기준:
1. 해당 특일에 사람들이 실제로 찾는 음식 종류
2. 특일과 자연스러운 연결고리가 있는 음식 (유연하게 해석)
3. 실제 소비 패턴과 마케팅 관점에서 매출 증가가 예상되는 업종
4. 관련성이 전혀 없으면 빈 배열로 응답
5. 억지로 분류하지 말고 확실한 연관성이 있을 때만 선택

예시:
- 초복/중복/말복 → 한식, 치킨 (삼계탕 등 보양식 + 닭 관련 음식)
- 크리스마스 → 양식, 치킨, 피자, 카페 (파티, 데이트, 따뜻한 음료)
- 추석 → 한식 (전통 명절 음식)
- 어린이날 → 치킨, 피자, 버거 (아이들 선호 음식)
- 발렌타인데이 → 카페, 양식 (데이트, 디저트)
- 무역의 날 → (관련성 없음)
- 국제 우표의 날 → (관련성 없음)

응답 형식: 관련 카테고리명만 쉼표로 구분 (예: 한식, 치킨) 또는 관련성이 없으면 아무것도 쓰지 마세요.
"""

        try:
            # requests를 사용한 직접 API 호출
            headers = {
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json"
            }
            
            data = {
                "model": "gpt-4o-mini", 
                "messages": [
                    {
                        "role": "system", 
                        "content": "당신은 음식점 마케팅 전문가입니다. 특일과 음식 카테고리의 연관성을 정확하게 분석합니다."
                    },
                    {
                        "role": "user", 
                        "content": prompt
                    }
                ],
                "max_tokens": 100,
                "temperature": 0.3  # 일관성을 위해 낮은 temperature
            }
            
            print("특일 카테고리 분류 API 호출 시작")
            
            response = requests.post(
                "https://api.openai.com/v1/chat/completions",
                headers=headers,
                json=data,
                timeout=30
            )
            
            if response.status_code == 200:
                result = response.json()
                content = result['choices'][0]['message']['content'].strip()
                print(f"GPT 응답: {content}")
                
                # 응답에서 카테고리 추출
                categories = CategoryService._parse_categories(content)
                print(f"분류된 카테고리: {categories}")
                
                return categories
            else:
                print(f"API 호출 실패: {response.status_code}, {response.text}")
                return []  # 실패 시 빈 배열
                
        except Exception as e:
            print(f"카테고리 분류 오류: {str(e)}")
            print(f"오류 타입: {type(e).__name__}")
            return []  # 실패 시 빈 배열
    
    @staticmethod
    def _parse_categories(content: str) -> List[str]:
        """
        GPT 응답에서 카테고리 목록 추출
        
        Args:
            content: GPT 응답 텍스트
            
        Returns:
            List[str]: 유효한 카테고리 목록
        """
        categories = []
        
        # 쉼표로 분리
        parts = content.replace('、', ',').split(',')
        
        for part in parts:
            category = part.strip().replace('"', '').replace("'", '')
            
            # 유효한 카테고리인지 확인
            if category in CategoryService.FOOD_CATEGORIES:
                if category not in categories:  # 중복 제거
                    categories.append(category)
        
        # 결과가 없으면 빈 배열 반환 (관련성이 없는 경우)
        return categories  # 0~9개 카테고리 반환 (관련성에 따라)