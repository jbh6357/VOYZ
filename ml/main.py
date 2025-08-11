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
from config import MenuItem, TranslateRequest, TranslateRequest2
from typing import List
import math
from collections import Counter
import re as _re
import json as _json
import requests
# FastAPI 앱 생성
app = FastAPI(
    title=API_CONFIG["title"],
    version=API_CONFIG["version"],
    description=API_CONFIG["description"]
)
@app.post("/api/reviews/keywords")
def extract_review_keywords(payload: dict):
    """
    요청: {
      "comments": [{"text": str, "rating": int, "menuIdx": int, "language": str|null}],
      "positiveThreshold": 4,
      "negativeThreshold": 2,
      "topK": 5
    }
    응답: {
      "overall": {"positiveKeywords": [], "negativeKeywords": []},
      "byMenu": [{"menuIdx": int, "positiveKeywords": [], "negativeKeywords": []}]
    }
    """
    comments = payload.get("comments", [])
    pos_th = int(payload.get("positiveThreshold", 4))
    neg_th = int(payload.get("negativeThreshold", 2))
    top_k = int(payload.get("topK", 5))

    def tokenize(text: str):
        if not text:
            return []
        # 아주 단순 토크나이징: 영/한/숫자만 남기고 공백 분리
        cleaned = _re.sub(r"[^0-9A-Za-z가-힣\s]", " ", text)
        tokens = [t.strip().lower() for t in cleaned.split() if len(t.strip()) >= 2]
        return tokens

    def top_keywords(texts):
        counter = Counter()
        for t in texts:
            counter.update(tokenize(t))
        return [w for w, _ in counter.most_common(top_k)]

    pos_texts = [c.get("text", "") for c in comments if (c.get("rating") or 0) >= pos_th]
    neg_texts = [c.get("text", "") for c in comments if (c.get("rating") or 0) <= neg_th]

    mode = (payload.get("mode") or "simple").lower()

    if mode == "openai":
        api_key = os.getenv("OPENAI_API_KEY")
        if api_key:
            try:
                # 입력 축약 (토큰 폭주 방지)
                def sample_texts(texts, limit=50, each_len=120):
                    return [t[:each_len] for t in texts[:limit]]

                prompt = {
                    "instruction": (
                        "Given review texts grouped by positive and negative (and by menuIdx), "
                        "extract top unique keywords only (no sentences), prioritize aspect terms and nouns, "
                        "deduplicate similar words, and return exactly the following JSON schema without extra text: "
                        "{\"overall\": {\"positiveKeywords\": [], \"negativeKeywords\": []}, \"byMenu\": [{\"menuIdx\": 0, \"positiveKeywords\": [], \"negativeKeywords\": []}]}"
                    ),
                    "topK": top_k,
                    "overall": {
                        "positiveTexts": sample_texts(pos_texts),
                        "negativeTexts": sample_texts(neg_texts),
                    },
                    "byMenu": {}
                }
                # byMenu 텍스트 구성
                by_menu_texts = {}
                for c in comments:
                    mi = c.get("menuIdx")
                    if mi is None:
                        continue
                    r = int(c.get("rating") or 0)
                    entry = by_menu_texts.setdefault(mi, {"pos": [], "neg": []})
                    (entry["pos"] if r >= pos_th else entry["neg"] if r <= neg_th else entry.setdefault("neu", [])).append((c.get("text") or "")[:120])
                prompt["byMenu"] = {str(k): {"positiveTexts": v.get("pos", []), "negativeTexts": v.get("neg", [])} for k, v in by_menu_texts.items()}

                headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}
                data = {
                    "model": "gpt-4o-mini",
                    "messages": [
                        {"role": "system", "content": "You are a precise NLP assistant for keyword extraction. Output strictly valid JSON only."},
                        {"role": "user", "content": _json.dumps(prompt, ensure_ascii=False)}
                    ],
                    "temperature": 0.2,
                    "max_tokens": 400
                }
                resp = requests.post("https://api.openai.com/v1/chat/completions", headers=headers, json=data, timeout=30)
                if resp.status_code == 200:
                    content = resp.json()["choices"][0]["message"]["content"].strip()
                    try:
                        parsed = _json.loads(content)
                        # 최소 유효성 검사 및 topK 제한
                        def clamp(arr):
                            if isinstance(arr, list):
                                return [str(x)[:40] for x in arr][:top_k]
                            return []
                        overall = {
                            "positiveKeywords": clamp(parsed.get("overall", {}).get("positiveKeywords", [])),
                            "negativeKeywords": clamp(parsed.get("overall", {}).get("negativeKeywords", [])),
                        }
                        by_menu = []
                        for item in parsed.get("byMenu", []):
                            by_menu.append({
                                "menuIdx": int(item.get("menuIdx", 0)),
                                "positiveKeywords": clamp(item.get("positiveKeywords", [])),
                                "negativeKeywords": clamp(item.get("negativeKeywords", [])),
                            })
                        return {"overall": overall, "byMenu": by_menu}
                    except Exception:
                        pass
            except Exception:
                pass

    overall = {
        "positiveKeywords": top_keywords(pos_texts),
        "negativeKeywords": top_keywords(neg_texts),
    }

    # 메뉴별
    by_menu_map = {}
    for c in comments:
        menu_idx = c.get("menuIdx")
        if menu_idx is None:
            continue
        m = by_menu_map.setdefault(menu_idx, {"pos": [], "neg": []})
        rating = c.get("rating") or 0
        if rating >= pos_th:
            m["pos"].append(c.get("text", ""))
        elif rating <= neg_th:
            m["neg"].append(c.get("text", ""))

    by_menu = []
    for k, v in by_menu_map.items():
        by_menu.append({
            "menuIdx": k,
            "positiveKeywords": top_keywords(v.get("pos", [])),
            "negativeKeywords": top_keywords(v.get("neg", [])),
        })

    return {"overall": overall, "byMenu": by_menu}

# 환경변수로 인증 설정 (선택사항)
google_credentials_path = os.getenv('GOOGLE_APPLICATION_CREDENTIALS')
if google_credentials_path and os.path.exists(google_credentials_path):
    print(f"Google Cloud credentials loaded from: {google_credentials_path}")
elif google_credentials_path:
    print(f"Google Cloud credentials path set but file not found: {google_credentials_path}")
else:
    print("Warning: Google Cloud credentials not found - OCR/Translation features may not work")

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

    # Google Cloud 인증이 없으면 더미 데이터 반환
    google_credentials_path = os.getenv('GOOGLE_APPLICATION_CREDENTIALS')
    if not google_credentials_path or not os.path.exists(google_credentials_path):
        # 더미 OCR 결과 반환
        return [
            MenuItem(menuName="된장찌개", menuPrice=7000),
            MenuItem(menuName="김치찌개", menuPrice=7500), 
            MenuItem(menuName="제육볶음", menuPrice=8000),
            MenuItem(menuName="불고기", menuPrice=9000)
        ]

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
                                items.append(MenuItem(menuName=menu_name, menuPrice=price))
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
                                            items.append(MenuItem(menuName=menu_name, menuPrice=price))
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

@app.post("/api/translateWeb")
def translate_text(req: TranslateRequest2):
    try:
        translate_client = translate.Client()
        all_translated_texts = []
        
        # API 허용 최대 텍스트 수
        MAX_TEXT_SEGMENTS = 128
        # 텍스트 배열을 최대 허용 수만큼 묶음으로 나눕니다.
        num_chunks = math.ceil(len(req.texts) / MAX_TEXT_SEGMENTS)
        
        for i in range(num_chunks):
            start_index = i * MAX_TEXT_SEGMENTS
            end_index = start_index + MAX_TEXT_SEGMENTS
            chunk_of_texts = req.texts[start_index:end_index]
            
            # 각 묶음을 별도의 API 요청으로 보냅니다.
            results = translate_client.translate(chunk_of_texts, target_language=req.targetLanguage)
            translated_chunk = [result["translatedText"] for result in results]
            all_translated_texts.extend(translated_chunk)
            
        return {"translated_texts": all_translated_texts}
    
    except Exception as e:
        print(f"An error occurred: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        app, 
        host=API_CONFIG["host"], 
        port=API_CONFIG["port"]

    ) 
