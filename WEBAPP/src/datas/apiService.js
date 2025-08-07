// API Base URL 설정
const API_BASE_URL = process.env.NODE_ENV === 'production' 
    ? 'http://13.125.251.36:8081/api' 
    : 'http://localhost:8081/api';

/**
 * 사용자의 메뉴 목록을 가져오는 API
 * @param {string} userId - 사용자 ID (이메일)
 * @returns {Promise<Array>} 메뉴 아이템 배열
 */
export const getMenusByUserId = async (userId) => {
    try {
        console.log('메뉴 데이터 요청:', userId);
        
        const response = await fetch(`${API_BASE_URL}/menus/${encodeURIComponent(userId)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log('📋 메뉴 데이터 수신:', data);
        
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