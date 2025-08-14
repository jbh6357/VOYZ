import { API_CONFIG } from "../config/api.js";

/**
 * HTML 엔티티를 디코딩하는 함수
 */
const decodeHtmlEntities = (text) => {
  if (typeof text !== 'string') return text;
  
  const entityMap = {
    '&#39;': "'",
    '&quot;': '"',
    '&amp;': '&',
    '&lt;': '<',
    '&gt;': '>',
    '&nbsp;': ' ',
    '&#x27;': "'",
    '&#x2F;': '/',
    '&#x60;': '`',
    '&#x3D;': '='
  };
  
  return text.replace(/&#?\w+;/g, (entity) => {
    return entityMap[entity] || entity;
  });
};

/**
 * 번역 API
 */
export const translateTexts = async (texts, targetLanguage) => {
  try {
    const res = await fetch(`${API_CONFIG.BASE_URL}/translate/`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        texts: texts,
        targetLanguage: targetLanguage,
      }),
    });

    if (!res.ok) throw new Error("API translation failed.");

    const { translated_texts } = await res.json();
    
    // HTML 엔티티 디코딩 처리
    const decodedTexts = translated_texts.map(text => decodeHtmlEntities(text));
    
    return decodedTexts;
  } catch (error) {
    console.error("Translation error:", error);
    throw error;
  }
};
