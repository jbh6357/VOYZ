"""
특일 컨텐츠 생성 서비스
"""

import os
import requests
import json
from typing import Optional
from config import CreateSuggestResponse

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
        

    @staticmethod
    def calculate_confidence(special_day_name: str, day_type: str, store_category: str) -> float:
        """특일과 업종의 연관성을 분석하여 신뢰도 계산"""
        # 1단계: 음식과 완전히 무관한 특일 사전 필터링 (매우 낮음: 15-25%)
        non_food_keywords = [
            '보안', '정보', 'IT', '컴퓨터', '인터넷', '사이버', '디지털', '통신', '네트워크',
            '의료', '병원', '의사', '간호', '치료', '약사', '건강검진',
            '교육', '학교', '대학', '교사', '학생', '교수', '학습', '시험',
            '법무', '법률', '변호사', '판사', '검사', '법원', '재판',
            '경찰', '소방', '군인', '국방', '치안', '방범', '순찰',
            '건설', '토목', '건축', '공학', '기술', '산업', '제조',
            '금융', '은행', '증권', '보험', '투자', '경제', '회계',
            '교통', '도로', '철도', '항공', '해운', '운송', '물류',
            '환경', '생태', '자연보호', '오염', '재활용', '에너지',
            '스포츠', '체육', '운동', '올림픽', '경기', '선수',
            '과학', '연구', '실험', '발명', '특허', '기술개발'
        ]
        
        if any(keyword in special_day_name for keyword in non_food_keywords):
            return round(15 + (hash(special_day_name) % 10), 1)
        
        # 2단계: 직접적인 음식 연관성 (매우 높음: 85-95%)
        if any(food in special_day_name for food in ['치킨', '피자', '커피', '삼겹살', '김치', '라면', '떡', '빵']):
            if store_category in ['치킨', '피자', '카페', '한식', '분식']:
                return round(85 + (hash(special_day_name) % 10), 1)
            else:
                return round(70 + (hash(special_day_name) % 10), 1)  # 다른 업종은 조금 낮게
        
        # 3단계: 계절/명절 연관성 (높음: 70-80%)  
        if any(season in day_type for season in ['절기', '명절', '공휴일']):
            if store_category in ['한식', '카페']:
                return round(70 + (hash(special_day_name) % 10), 1)
            else:
                return round(60 + (hash(special_day_name) % 10), 1)
        
        # 4단계: 일반 기념일 (중간: 50-65%)
        if '기념일' in day_type:
            # 음식 관련 키워드가 조금이라도 있으면 중상
            food_related = ['맛', '먹', '식', '요리', '음식', '축제', '잔치', '파티']
            if any(word in special_day_name for word in food_related):
                return round(55 + (hash(special_day_name) % 10), 1)
            else:
                return round(45 + (hash(special_day_name) % 10), 1)  # 완전 일반적인 기념일
        
        # 5단계: 기타 (낮음: 30-45%)
        return round(30 + (hash(special_day_name) % 15), 1)
    
    @staticmethod
    def create_special_day_suggest(name: str, type_name: str, storeCategory: str) -> CreateSuggestResponse:
        """
        LLM을 활용해 특일 마케팅 제안 생성

        Args:
            name (str): 특일명
            type_name (str): 특일 유형
            storeCategory (str): 음식점 업종

        Returns:
            CreateSuggestResponse: 생성된 마케팅 제안 응답
        """

        # API 키 확인
        api_key = os.getenv("OPENAI_API_KEY")
        if not api_key:
            print("OpenAI API 키가 없습니다.")
            return CreateSuggestResponse(
                success=False,
                title=f"{name} 특별 이벤트",
                description=f"{name}에 대한 마케팅 기회들을 확인해보세요.",
                targetCustomer="일반 고객",
                suggestedAction=f"{name} 테마 활용 마케팅",
                expectedEffect="고객 관심도 향상 기대",
                confidence=50.0,
                priority="중"
            )
        
        # 먼저 신뢰도 계산
        calculated_confidence = ContentService.calculate_confidence(name, type_name, storeCategory)
        
        prompt = f"""
당신은 음식점 마케팅 전문가입니다. 아래 특일에 대한 마케팅 제안을 작성해주세요.

특일명: {name}
유형: {type_name}
업종 카테고리: {storeCategory}

분석된 마케팅 신뢰도: {calculated_confidence}%
(이 신뢰도는 특일과 업종의 실제 연관성을 분석한 결과입니다)

요구사항:
1. 마케팅 요약 제목 (10자 이내)
2. 설명 (50자 이내) 
3. 주요 타겟 고객
4. 추천 마케팅 액션
5. 기대 효과
6. 신뢰도: 위에서 계산된 {calculated_confidence} 값을 그대로 사용
7. 우선순위: 신뢰도에 따라 80이상=상, 65이상=중, 그 외=하

형식 (정확히 이 형식을 지켜주세요):
제목: [제목내용]
설명: [설명내용]
타겟: [타겟고객]
액션: [마케팅액션]
효과: [기대효과]
신뢰도: {calculated_confidence}
우선순위: {'상' if calculated_confidence >= 80 else '중' if calculated_confidence >= 65 else '하'}
"""

        try:
            headers = {
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json"
            }

            data = {
                "model": "gpt-4o-mini",
                "messages": [
                    {
                        "role": "system",
                        "content": "당신은 음식점 마케팅 전문가입니다. 구조화된 제안을 작성해주세요."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                "max_tokens": 300,
                "temperature": 0.7
            }

            response = requests.post(
                "https://api.openai.com/v1/chat/completions",
                headers=headers,
                json=data,
                timeout=30
            )

            if response.status_code == 200:
                result = response.json()
                content = result['choices'][0]['message']['content'].strip()
                print("GPT 응답:", content)

                # 간단 파싱 로직 (실제 운영 시엔 정규표현식 또는 JSON 포맷으로 개선 권장)
                def extract_field(label):
                    for line in content.splitlines():
                        if line.startswith(label):
                            value = line.split(":", 1)[-1].strip()
                            return value if value else f"{name} 마케팅 제안"  # 빈 값일 때 기본값 설정
                    return f"{name} 마케팅 제안"  # 필드를 찾지 못했을 때 기본값

                # 디버그: 실제 GPT 응답 확인
                print(f"=== GPT 원본 응답 ===")
                print(repr(content))  # 특수 문자까지 표시
                print(f"=== 줄별 분석 ===")
                for i, line in enumerate(content.splitlines()):
                    print(f"[{i}] '{line}'")
                print(f"=== 파싱 결과 ===")
                print(f"제목: '{extract_field('제목')}'")
                print(f"설명: '{extract_field('설명')}'")
                print(f"타겟: '{extract_field('타겟')}'")
                print(f"액션: '{extract_field('액션')}'")
                print(f"효과: '{extract_field('효과')}'")
                print(f"신뢰도: '{extract_field('신뢰도')}'")
                print(f"우선순위: '{extract_field('우선순위')}'")
                print(f"=========================")
                
                return CreateSuggestResponse(
                    success=True,
                    title=extract_field("제목"),
                    description=extract_field("설명"),
                    targetCustomer=extract_field("타겟"),
                    suggestedAction=extract_field("액션"),
                    expectedEffect=extract_field("효과"),
                    confidence=calculated_confidence,  # 계산된 값 사용
                    priority=extract_field("우선순위"),
                )

            else:
                print(f"OpenAI API 오류: {response.status_code}, {response.text}")
                raise Exception("LLM 응답 실패")

        except Exception as e:
            print(f"OpenAI 호출 중 예외 발생: {e}")
            return CreateSuggestResponse(
                success=False,
                title="",
                description=f"{name}에 대한 마케팅 기회들을 확인해보세요.",
                targetCustomer="",
                suggestedAction="",
                expectedEffect="",
                confidence=calculated_confidence,  # 계산된 값 사용
                priority="하",
            )
    
    