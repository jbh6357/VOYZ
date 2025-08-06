import { useEffect } from 'react';

const SuccessPage = ({ onBackToMenu, onGoToReview, orderedItems }) => {
    useEffect(() => {
        // 주문 완료 후 10초 뒤에 자동으로 리뷰 페이지로 이동
        if (orderedItems && orderedItems.length > 0 && onGoToReview) {
            const timer = setTimeout(() => {
                console.log('🔄 리뷰 페이지로 자동 이동');
                try {
                    onGoToReview();
                } catch (error) {
                    console.error('리뷰 페이지 이동 중 에러:', error);
                }
            }, 5000); // 5초

            return () => {
                clearTimeout(timer);
            };
        }
    }, [orderedItems, onGoToReview]);

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
