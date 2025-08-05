"""
FastAPI 메인 애플리케이션
특일 매칭 API 서버
"""

from fastapi import FastAPI, HTTPException, File, UploadFile
from typing import Dict, Any, List
from datetime import datetime

# 설정 및 모델 import
from config import API_CONFIG, SpecialDayMatchRequest, ContentGenerationRequest, CategoryClassificationRequest, CreateSuggestRequest, CreateSuggestResponse
from models.match_models import MatchModels
from services.content_service import ContentService
from services.category_service import CategoryService
import os
from google.cloud import vision
import re
from google.cloud import translate_v2 as translate
from config import MenuItem, TranslateRequest
# FastAPI 앱 생성
app = FastAPI(
    title=API_CONFIG["title"],
    version=API_CONFIG["version"],
    description=API_CONFIG["description"]
)

# 환경변수로 인증 설정
os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = os.getenv('GOOGLE_CREDENTIALS_PATH')

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
    
# 특일 제안 생성
@app.post("/api/suggest/create")
def create_suggest(request: CreateSuggestRequest):
    """ 특일 제안 생성 """
    try:
        result = ContentService.create_special_day_suggest(
            name=request.name,
            type_name=request.type,
            storeCategory=request.storeCategory
        )
        
        # 계산된 confidence를 직접 사용 (문자열이 아닌 숫자)
        calculated_confidence = ContentService.calculate_confidence(
            request.name, request.type, request.storeCategory
        )
        
        return {
            "success": True,
            "title": result.title,
            "description": result.description,
            "targetCustomer": result.targetCustomer,
            "suggestedAction": result.suggestedAction,
            "expectedEffect": result.expectedEffect,
            "confidence": calculated_confidence,  # 숫자값 직접 사용
            "priority": result.priority
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"제안 생성 실패: {str(e)}")

@app.post("/api/ocr")
async def extract_text(file: UploadFile = File(...)):
    try:
        # 파일 타입 검증
        if not file.content_type.startswith('image/'):
            raise HTTPException(status_code=400, detail="이미지 파일만 업로드 가능합니다")
        
        # 파일 크기 제한 (예: 10MB)
        contents = await file.read()
        if len(contents) > 10 * 1024 * 1024:
            raise HTTPException(status_code=413, detail="파일 크기가 너무 큽니다")
        
        # OCR 처리
        client = vision.ImageAnnotatorClient()
        image = vision.Image(content=contents)
        response = client.document_text_detection(image=image)
        document = response.full_text_annotation
        # 문단 단위로 출력 + 좌표 정보 출력
        for page in document.pages:
            for block in page.blocks:
                block_text = ''
                for paragraph in block.paragraphs:
                    for word in paragraph.words:
                        word_text = ''.join([symbol.text for symbol in word.symbols])
                        block_text += word_text + ' '
                    block_text += '\n'

                # 블록 좌표 가져오기 (bounding box의 꼭짓점 4개)
                vertices = block.bounding_box.vertices
                coords = [(v.x, v.y) for v in vertices]

                # 출력
                print("문단 텍스트:")
                print(block_text.strip())
                print("문단 좌표 (bounding box):", coords)
                print("-" * 50)

        texts = response.text_annotations
        text = texts[0].description
        
        items = []
        lines = text.strip().split('\n')
        
        # 확장된 가격 패턴들
        price_patterns = [
            r'(\d{1,3}[,.]?\d{3})\s*원',  # 16,000원
            r'(\d{1,3}[,.]?\d{3})',       # 16,000
            r'(\d{4,6})',                 # 16000
            r'(\d+\.\d)',                 # 5.0
        ]
     
        i = 0
        while i < len(lines):
            line = lines[i].strip()
            # 빈 줄 스킵
            if not line:
                i += 1
                continue
            
            # 노이즈 필터링 (이메일, 전화번호, 주소, 시간 등)
            if (re.search(r'@.*\.com', line) or 
                re.search(r'\+?\d{3}-\d{3}-\d{4}', line) or
                re.search(r'\d+\s+anywhere', line.lower()) or
                re.search(r'\d{1,2}[:.]\d{2}\s*(am|pm)', line.lower()) or
                len([c for c in line if c.isalpha()]) < 2):  # 너무 적은 문자
                i += 1
                continue
            
            # 현재 줄에서 가격 찾기
            price_found = False
            for pattern in price_patterns:
                price_match = re.search(pattern, line)
                if price_match:
                    try:
                        price_str = price_match.group(1).replace(',', '').replace('.', '')
                        
                        # 소수점 형태 처리 (5.0 -> 5000)
                        if '.' in price_match.group(1) and len(price_str) <= 2:
                            price = int(float(price_match.group(1)) * 1000)
                        else:
                            price = int(price_str)
                        
                        # 합리적인 가격 범위 체크
                        if 1000 <= price <= 50000:
                            menu_name = line[:price_match.start()].strip()
                            if menu_name and len(menu_name) > 1:
                                items.append(MenuItem(name=menu_name, price=price))
                                price_found = True
                                break
                    except ValueError:
                        continue
            
            if not price_found:
                # 현재 줄에 가격이 없으면 다음 줄 확인
                if i + 1 < len(lines):
                    next_line = lines[i + 1].strip()
                    
                    for pattern in price_patterns:
                        next_price_match = re.search(pattern, next_line)
                        if next_price_match:
                            try:
                                price_str = next_price_match.group(1).replace(',', '').replace('.', '')
                                
                                # 소수점 형태 처리
                                if '.' in next_price_match.group(1) and len(price_str) <= 2:
                                    price = int(float(next_price_match.group(1)) * 1000)
                                else:
                                    price = int(price_str)
                                
                                # 합리적인 가격 범위 체크
                                if 1000 <= price <= 50000:
                                    # 다음 줄이 순수 가격인지 확인 (원 문자 포함해서 처리)
                                    remaining_text = next_line.replace(next_price_match.group(0), '').replace('원', '').strip()
                                    
                                    # 남은 텍스트가 거의 없으면 순수 가격으로 판단
                                    if len(remaining_text) <= 2:
                                        # 역방향으로 진짜 메뉴명 찾기
                                        menu_name = line
                                        # 현재 줄이 설명문인지 확인 (쉼표, 마침표 포함 또는 15글자 초과)
                                        if (',' in line or '.' in line or len(line) > 15):
                                            
                                            # 최대 3줄 전까지 확인
                                            for back_offset in range(1, min(4, i + 1)):
                                                prev_line = lines[i - back_offset].strip()
                                                
                                                # 이전 줄이 설명문이 아니고 가격이 없으면 메뉴명 가능성 높음
                                                if (prev_line and 
                                                    not (',' in prev_line or '.' in prev_line or len(prev_line) > 15) and
                                                    not re.search(r'\d+[,.]?\d*원?', prev_line)):  # 가격이 없고
                                                    menu_name = prev_line
                                                    break
                                    
                                        if len(menu_name) > 1:
                                            items.append(MenuItem(name=menu_name, price=price))
                                            i += 1  # 다음 줄 스킵
                                            break
                            except ValueError:
                                continue
            
            i += 1

        return items
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/translate")
def translateMenu(req: TranslateRequest):
    # 요청 받은 메뉴명
    source_text = req.text
    targetLanguage = req.targetLanguage
    # 번역 클라이언트 생성
    translate_client = translate.Client()
    # 번역 실행
    result = translate_client.translate(
        source_text,
        target_language= targetLanguage  
    )

    return {
        "targetLanguage": targetLanguage,
        "translated": result["translatedText"]
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        app, 
        host=API_CONFIG["host"], 
        port=API_CONFIG["port"]
    ) 