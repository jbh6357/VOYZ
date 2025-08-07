import { API_CONFIG } from '../config/api.js';

/**
 * 사용자의 메뉴 목록을 가져오는 API
 * @param {string} userId - 사용자 ID (이메일)
 * @returns {Promise<Array>} 메뉴 아이템 배열
 */
export const getMenusByUserId = async (userId) => {
    try {
        
        const response = await fetch(`${API_CONFIG.BASE_URL}/menus/${encodeURIComponent(userId)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        
        return data;
    } catch (error) {
        console.error('메뉴 데이터 로드 실패:', error);
        throw error;
    }
};

/**
 * URL 파라미터에서 userId와 table을 추출하는 함수
 * @returns {Object} { userId, table }
 */
export const getUrlParams = () => {
    const urlParams = new URLSearchParams(window.location.search);
    return {
        userId: urlParams.get('userId'),
        table: urlParams.get('table')
    };
};