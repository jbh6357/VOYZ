import { API_CONFIG } from "../config/api.js";

/**
 * 메뉴별 리뷰 목록을 가져오는 API
 * @param {string} menuId - 메뉴 ID
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
