import { API_CONFIG } from "../config/api.js";

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
    return translated_texts;
  } catch (error) {
    console.error("Translation error:", error);
    throw error;
  }
};
