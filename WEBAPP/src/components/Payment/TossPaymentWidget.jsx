import { useState, useEffect } from 'react';
import { loadTossPayments } from '@tosspayments/payment-sdk';
import Modal from '../UI/Modal.jsx';
import { formatPrice } from '../../utils/helpers.js';

const TossPaymentWidget = ({ isOpen, totalPrice, selectedLang, onPaymentComplete, onPaymentError }) => {
    const [paymentStep, setPaymentStep] = useState('confirm'); // confirm, api-call, processing, complete
    const [countdown, setCountdown] = useState(3);
    const [tossPayments, setTossPayments] = useState(null);

    // 처리중 카운트다운
    useEffect(() => {
        if (paymentStep === 'processing' && countdown > 0) {
            console.log(`⏰ 토스 결제 카운트다운: ${countdown}초 남음`);
            const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
            return () => clearTimeout(timer);
        } else if (paymentStep === 'processing' && countdown === 0) {
            console.log('✅ 토스 결제 완료, onPaymentComplete 호출');
            const paymentDetails = {
                status: 'success',
                message: '토스페이 결제 완료',
                orderId: 'toss_order_' + Date.now(),
                totalPrice: totalPrice,
            };
            console.log('📤 전달할 결제 정보:', paymentDetails);
            onPaymentComplete(paymentDetails);
        }
    }, [paymentStep, countdown, onPaymentComplete, totalPrice]);

    // 토스페이먼츠 SDK 로드
    useEffect(() => {
        if (isOpen && !tossPayments) {
            loadTossPayments('test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq').then(setTossPayments).catch(console.error);
        }
    }, [isOpen]);

    // 초기화 (모달 열릴 때마다 리셋)
    useEffect(() => {
        if (isOpen) {
            setPaymentStep('confirm');
            setCountdown(3);
        }
    }, [isOpen]);

         const handleConfirmPayment = async () => {
         if (!tossPayments) {
             // SDK 없으면 바로 시뮬레이션
             console.log('토스 SDK 없음 - 시뮬레이션 실행');
             setPaymentStep('processing');
             setCountdown(3);
             return;
         }

        setPaymentStep('api-call');

        try {
            // 토스페이먼츠 API 호출
            await tossPayments.requestPayment('카드', {
                amount: totalPrice,
                orderId: 'order_' + Date.now(),
                orderName: '한옥마을 전통 맛집',
                customerName: '고객',
                customerEmail: 'customer@example.com',
                successUrl: window.location.origin + '/?payment=success',
                failUrl: window.location.origin + '/?payment=fail',
            });

            // 성공하면 여기 안 옴 (페이지 이동됨)
        } catch (error) {
            console.error('결제 요청 실패:', error);

            // 테스트 환경에서는 에러나도 성공으로 처리
            if (error.code === 'USER_CANCEL') {
                onPaymentError && onPaymentError('결제가 취소되었습니다.');
            } else {
                // 테스트에서는 실패해도 성공으로 처리
                console.log('테스트 환경: 에러 발생했지만 성공으로 처리');
                setTimeout(() => {
                    onPaymentComplete({
                        status: 'success',
                        message: '테스트 결제 완료 (에러 발생했지만 성공처리)',
                        orderId: 'test_order_' + Date.now(),
                        totalPrice: totalPrice,
                    });
                }, 1000);
            }
        }
    };

    if (!isOpen) return null;

    return (
        <Modal
            isOpen={isOpen}
            onClose={() => {}}
            className='toss-widget'
        >
            <div className='toss-payment-container'>
                <div className='toss-header'>
                    <div className='toss-logo'>토스페이</div>
                </div>

                {paymentStep === 'confirm' && (
                    <div className='toss-confirm-step'>
                        <div className='payment-info'>
                            <div className='merchant-name'>한옥마을 전통 맛집</div>
                            <div className='payment-amount-large'>{formatPrice(totalPrice, selectedLang)}</div>
                        </div>

                        <div className='payment-method-selected'>
                            <div className='method-icon'>TOSS</div>
                            <div className='method-info'>
                                <div className='method-name'>토스페이 간편결제</div>
                                <div className='method-detail'>모바일 최적화</div>
                            </div>
                        </div>

                        <button
                            className='toss-confirm-btn'
                            onClick={handleConfirmPayment}
                        >
                            결제하기
                        </button>
                    </div>
                )}

                {paymentStep === 'api-call' && (
                    <div className='toss-processing-step'>
                        <div className='processing-text'>결제창 열기 중...</div>
                        <div className='processing-detail'>팝업 창에서 결제를 진행하세요</div>
                    </div>
                )}

                {paymentStep === 'processing' && (
                    <div className='toss-processing-step'>
                        <div className='processing-text'>결제 진행 중...</div>
                        <div className='processing-detail'>
                            {countdown > 0 ? `${countdown}초 후 완료` : '완료 중...'}
                        </div>
                    </div>
                )}
            </div>
        </Modal>
    );
};

export default TossPaymentWidget;
