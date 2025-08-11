import { API_CONFIG } from "../config/api.js";

/**
 * 메뉴별 리뷰 목록을 가져오는 API
 * @param {int} menuId - 메뉴 ID
 * @returns {Promise<Array>} 리뷰 아이템 배열
 */
export const getReviewsByMenuId = async (menuId) => {
  try {
    const response = await fetch(
      `${API_CONFIG.BASE_URL}/review/menu/${encodeURIComponent(menuId)}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("리뷰데이터는! ", data);

    return data;
  } catch (error) {
    console.error("리뷰 데이터 로드 실패:", error);
    throw error;
  }
};

export const postReview = async (orderIdx, selectedLang, review) => {
  try {
    const reviewData = {
      orderIdx: orderIdx,
      userId: review.user,
      menuIdx: review.itemIdx,
      rating: review.rating,
      comment: review.text,
      nationality: review.countryCode,
      language: selectedLang,
    };

    const response = await fetch(`${API_CONFIG.BASE_URL}/review/`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(reviewData),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
  } catch (error) {
    console.error("리뷰 업로드 실패:", error);
    throw error;
  }
};
