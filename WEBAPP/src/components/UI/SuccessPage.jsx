import { useEffect, useRef } from 'react';

const SuccessPage = ({ onBackToMenu, orderedItems }) => {
    const isMountedRef = useRef(true);

    useEffect(() => {
        isMountedRef.current = true;
        
        // 주문 완료 후 10초 뒤에 자동으로 리뷰 페이지로 이동
        if (orderedItems && orderedItems.length > 0) {
            console.log('📝 주문 완료 페이지: 10초 후 리뷰 페이지로 자동 이동');
            
            const timer = setTimeout(() => {
                // 컴포넌트가 아직 마운트된 상태에서만 실행
                if (isMountedRef.current && window.onWriteReview) {
                    console.log('🔄 리뷰 페이지로 자동 이동');
                    try {
                        window.onWriteReview();
                    } catch (error) {
                        console.error('리뷰 페이지 이동 중 에러:', error);
                    }
                }
            }, 10000); // 10초

            return () => {
                clearTimeout(timer);
                isMountedRef.current = false;
            };
        }

        return () => {
            isMountedRef.current = false;
        };
    }, [orderedItems]);

    return (
        <div className='success-page'>
            <div className='success-content'>
                <div className='success-icon'>[완료]</div>
                <h1>주문이 완료되었습니다!</h1>
                <p>맛있는 식사 되세요!</p>
                <button
                    className='back-to-menu-btn'
                    onClick={onBackToMenu}
                >
                    메뉴로 돌아가기
                </button>
            </div>
        </div>
    );
};
export default SuccessPage;
