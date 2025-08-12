import { API_CONFIG } from "../config/api.js";

/**
 * 사용자의 주문 정보를 DB에 저장하는 API
 * @param {string} userId - 사용자 ID (이메일)
 * @param {string} tableNumber - 테이블 번호
 * @param {Promiss<Array} items - 주문 메뉴 배열
 */
export const postOrder = async (userId, tableNumber, reducedItems) => {
  try {
    const orderData = {
      userId: userId,
      tableNumber: tableNumber,
      orderDetails: reducedItems.map((item) => ({
        menuIdx: item.id,
        quantity: item.quantity,
      })),
    };
    const response = await fetch(`${API_CONFIG.BASE_URL}/orders/`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(orderData),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("주문 번호:", data);
    return data; // 주문 번호 반환
  } catch (error) {
    console.error("주문 실패:", error);
    throw error;
  }
};
