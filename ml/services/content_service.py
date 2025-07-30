"""
특일 컨텐츠 생성 서비스
"""

import os
import requests
import json
from typing import Optional

class ContentService:
    """GPT를 활용한 특일 컨텐츠 생성 서비스"""
    
    @staticmethod
    def generate_special_day_content(name: str, type_name: str, category: Optional[str] = None) -> str:
        """
        특일에 대한 음식점 마케팅용 설명 컨텐츠 생성
        
        Args:
            name: 특일명 (예: 크리스마스, 어린이날)
            type_name: 특일 유형
            category: 카테고리 (선택)
            
        Returns:
            str: 생성된 컨텐츠 (한 줄, 최대 30자)
        """
        
        # 환경변수에서 API 키 가져오기
        api_key = os.getenv("OPENAI_API_KEY")
        if not api_key:
            print("OpenAI API 키가 설정되지 않았습니다.")
            return f"{name}에 대한 마케팅 기회들을 확인해보세요."
        
        print(f"OpenAI API 호출 시작 - name: {name}, type: {type_name}, category: {category}")
        
        # GPT 프롬프트 구성
        prompt = f"""다음 특일에 대한 간단한 설명을 작성해주세요.

특일명: {name}
유형: {type_name}
{f'카테고리: {category}' if category else ''}

조건:
1. 음식점 마케팅 전문가 관점으로 작성
2. 한 줄로 간결하게 (최대 30자)
3. 모달창에 표시될 설명문
4. 자연스럽고 실용적인 톤
5. 모든 날을 중요하게 포장하지 말고 객관적으로 서술
6. GPT스러운 어투 피하기
7. 음식점 사장이 이해하기 쉽게

예시 스타일:
- "가족단위 고객이 많이 찾는 날로, 세트메뉴나 단체 할인이 효과적입니다"
- "직장인들의 회식 수요가 증가하는 시기입니다"
- "일반적인 영업일과 큰 차이가 없지만, 테마 메뉴로 차별화 가능합니다"

설명:"""

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
                        "content": "당신은 음식점 마케팅 전문가입니다. 특일에 대한 실용적이고 간결한 설명을 제공합니다."
                    },
                    {
                        "role": "user", 
                        "content": prompt
                    }
                ],
                "max_tokens": 100,
                "temperature": 0.7
            }
            
            print("OpenAI API 직접 호출 시작")
            
            response = requests.post(
                "https://api.openai.com/v1/chat/completions",
                headers=headers,
                json=data,
                timeout=30
            )
            
            if response.status_code == 200:
                result = response.json()
                content = result['choices'][0]['message']['content'].strip()
                print(f"OpenAI 응답: {content}")
                    
                print(f"최종 컨텐츠: {content}")
                return content
            else:
                print(f"API 호출 실패: {response.status_code}, {response.text}")
                return f"{name}에 대한 마케팅 기회들을 확인해보세요."
            
        except Exception as e:
            print(f"OpenAI API 오류: {str(e)}")
            print(f"오류 타입: {type(e).__name__}")
            # GPT API 실패 시 기본 메시지 반환
            return f"{name}에 대한 마케팅 기회들을 확인해보세요."