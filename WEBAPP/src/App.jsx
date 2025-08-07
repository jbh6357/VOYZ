import { useState, useEffect, useCallback } from 'react';

// Components
import LanguageSelector from './components/UI/LanguageSelector.jsx';
import MenuSection from './components/Menu/MenuSection.jsx';
import OrderPage from './components/Menu/OrderPage.jsx';
import ReviewModal from './components/Review/ReviewModal.jsx';
import WriteReviewPage from './components/Review/WriteReviewPage.jsx';
import NotificationPermissionModal from './components/UI/NotificationPermissionModal.jsx';
import PaymentModal from './components/Payment/PaymentModal.jsx';
import TossPaymentWidget from './components/Payment/TossPaymentWidget.jsx';
import PayPalPaymentWidget from './components/Payment/PayPalPaymentWidget.jsx';
import SuccessPage from './components/UI/SuccessPage.jsx';

// Data & Utils
import { sampleMenuData } from './datas/sampleData.js';
import { useMenu } from './hooks/useMenu.js';
import { formatPrice } from './utils/helpers.js';
import {
    isPushNotificationSupported,
    requestNotificationPermission,
    initializePushNotifications,
    scheduleReviewReminder,
} from './utils/pushNotifications.js';

function App() {
    // 앱 로드 시 알림 권한 확인 및 요청 + URL 파라미터 체크
    useEffect(() => {
        const checkNotificationPermission = async () => {
            // 이미 권한을 요청했는지 확인
            const hasAsked = localStorage.getItem('hasAskedForNotification');
            if (hasAsked) {
                setHasAskedForNotification(true);
                // 이미 권한이 있다면 푸시 알림 초기화
                if (Notification.permission === 'granted') {
                    await initializePushNotifications();
                }
                return;
            }

            // 푸시 알림을 지원하는 브라우저에서만 모달 표시
            if (isPushNotificationSupported()) {
                // 1초 후 모달 표시 (사용자가 페이지에 적응할 시간)
                setTimeout(() => {
                    setShowNotificationModal(true);
                }, 1000);
            }
        };

        // URL에서 결제 성공/실패 및 페이지 파라미터 확인
        const urlParams = new URLSearchParams(window.location.search);
        const paymentParam = urlParams.get('payment');
        const pageParam = urlParams.get('page');

        if (paymentParam === 'success') {
            console.log('토스 결제 성공 페이지로 이동됨');

            // 저장된 장바구니 복원
            const savedItems = localStorage.getItem('pendingOrderItems');
            const savedCart = localStorage.getItem('pendingOrderCart');

            if (savedItems) {
                const items = JSON.parse(savedItems);
                setOrderedItems(items);
                localStorage.setItem('lastOrderedItems', JSON.stringify(items));

                // 임시 저장 데이터 삭제
                localStorage.removeItem('pendingOrderItems');
                localStorage.removeItem('pendingOrderCart');
            }

            // 결제 성공 처리
            const orderId = urlParams.get('orderId');
            const paymentKey = urlParams.get('paymentKey');
            const amount = urlParams.get('amount');

            setShowTossWidget(false);
            setShowPayPalWidget(false);
            setCurrentPage('success');

            // URL 정리
            window.history.replaceState({}, document.title, '/');
        } else if (paymentParam === 'fail') {
            console.log('토스 결제 실패 페이지로 이동됨');
            const message = urlParams.get('message') || '결제가 실패했습니다.';
            handlePaymentError(message);

            // URL 정리
            window.history.replaceState({}, document.title, '/');
        } else if (pageParam === 'review') {
            console.log('🔔 푸시 알림에서 리뷰 페이지로 이동됨');
            // 저장된 주문 정보가 있으면 리뷰 페이지로, 없으면 메뉴로
            const savedOrder = localStorage.getItem('lastOrderedItems');
            if (savedOrder) {
                try {
                    const items = JSON.parse(savedOrder);
                    setOrderedItems(items);
                    setCurrentPage('writeReview');
                } catch (e) {
                    console.error('주문 정보 파싱 실패:', e);
                    setCurrentPage('menu');
                }
            } else {
                setCurrentPage('menu');
            }

            // URL 정리
            window.history.replaceState({}, document.title, '/');
        }

        checkNotificationPermission();
    }, []);
    const [selectedLang, setSelectedLang] = useState('ko');
    const [showReviews, setShowReviews] = useState(null);
    const [currentPage, setCurrentPage] = useState('menu');
    const [showPaymentModal, setShowPaymentModal] = useState(false);
    const [showTossWidget, setShowTossWidget] = useState(false);
    const [showPayPalWidget, setShowPayPalWidget] = useState(false);
    const [paymentError, setPaymentError] = useState(null);
    const [orderedItems, setOrderedItems] = useState([]);
    const [showNotificationModal, setShowNotificationModal] = useState(false);
    const [hasAskedForNotification, setHasAskedForNotification] = useState(false);

    const { cart, addToCart, removeFromCart, getTotalItems, getTotalPrice, clearCart } = useMenu();

    // 토스페이먼츠는 TossPaymentWidget 컴포넌트에서 직접 로드

    const getAllItems = () => {
        return Object.values(sampleMenuData.menu).flat();
    };

    const handleOrderClick = () => {
        setCurrentPage('order');
    };

    const handlePaymentClick = () => {
        setShowPaymentModal(true);
    };

    const handleTossPayment = async () => {
        // 토스 결제 전에 장바구니 저장 (페이지 이동 대비)
        const cartItems = Object.keys(cart).map((itemId) => {
            const item = getAllItems().find((i) => i.id === parseInt(itemId));
            return { ...item, quantity: cart[itemId] };
        });
        localStorage.setItem('pendingOrderItems', JSON.stringify(cartItems));
        localStorage.setItem('pendingOrderCart', JSON.stringify(cart));

        setShowPaymentModal(false);
        setShowTossWidget(true);
    };

    const handlePayPalPayment = () => {
        setShowPaymentModal(false);
        setShowPayPalWidget(true);
    };

    const handlePaymentError = (errorMessage) => {
        setPaymentError(errorMessage);
        setShowTossWidget(false);
        setShowPayPalWidget(false);
        setTimeout(() => setPaymentError(null), 5000); // 5초 후 에러 메시지 제거
    };

    const handlePaymentComplete = (paymentDetails) => {
        console.log('🎉 결제 완료 함수 호출됨:', paymentDetails);
        setShowTossWidget(false);
        setShowPayPalWidget(false);

        // 주문한 아이템 저장
        const items = Object.keys(cart).map((itemId) => {
            const item = getAllItems().find((i) => i.id === parseInt(itemId));
            return { ...item, quantity: cart[itemId] };
        });
        console.log('📦 주문 아이템들:', items);
        setOrderedItems(items);

        // 리뷰 알림을 위해 주문 정보를 localStorage에 저장
        localStorage.setItem('lastOrderedItems', JSON.stringify(items));

        console.log('📄 페이지를 success로 변경');
        setCurrentPage('success');
        clearCart();
    };

    const handleBackToMenu = () => {
        setCurrentPage('menu');
        setOrderedItems([]);
    };

    const handleGoToReview = () => {
        console.log('📱 리뷰 페이지로 이동');
        setCurrentPage('writeReview');
    };

    const handleSubmitReview = (review) => {
        console.log('리뷰 작성:', review);
        // 여기서 실제로는 서버에 리뷰를 저장해야 합니다
        setCurrentPage('success');
    };

    const handleSkipReview = () => {
        setCurrentPage('success');
    };

    // 알림 권한 허용 처리
    const handleAllowNotifications = async () => {
        try {
            const granted = await requestNotificationPermission();
            if (granted) {
                await initializePushNotifications();
                console.log('Push notifications initialized successfully');
            }
            localStorage.setItem('hasAskedForNotification', 'true');
            setHasAskedForNotification(true);
            setShowNotificationModal(false);
        } catch (error) {
            console.error('Failed to enable notifications:', error);
            setShowNotificationModal(false);
        }
    };

    // 알림 권한 거부 처리
    const handleDenyNotifications = () => {
        localStorage.setItem('hasAskedForNotification', 'true');
        setHasAskedForNotification(true);
        setShowNotificationModal(false);
    };

    if (currentPage === 'writeReview') {
        return (
            <WriteReviewPage
                orderedItems={orderedItems}
                selectedLang={selectedLang}
                onSubmitReview={handleSubmitReview}
                onSkip={handleSkipReview}
            />
        );
    }

    // window 전역 함수 제거 - props로 직접 전달

    if (currentPage === 'success') {
        return (
            <SuccessPage
                onBackToMenu={handleBackToMenu}
                onGoToReview={handleGoToReview}
                orderedItems={orderedItems}
                selectedLang={selectedLang}
            />
        );
    }

    if (currentPage === 'order') {
        return (
            <>
                <OrderPage
                    cart={cart}
                    getAllItems={getAllItems}
                    selectedLang={selectedLang}
                    onAddToCart={addToCart}
                    onRemoveFromCart={removeFromCart}
                    onBackToMenu={() => setCurrentPage('menu')}
                    onPaymentClick={handlePaymentClick}
                    getTotalPrice={() => getTotalPrice(getAllItems)}
                />
                <PaymentModal
                    isOpen={showPaymentModal}
                    onClose={() => setShowPaymentModal(false)}
                    onTossPayment={handleTossPayment}
                    onPayPalPayment={handlePayPalPayment}
                />
                <TossPaymentWidget
                    isOpen={showTossWidget}
                    totalPrice={getTotalPrice(getAllItems)}
                    selectedLang={selectedLang}
                    onPaymentComplete={handlePaymentComplete}
                    onPaymentError={handlePaymentError}
                />
                <PayPalPaymentWidget
                    isOpen={showPayPalWidget}
                    onClose={() => setShowPayPalWidget(false)}
                    totalPrice={getTotalPrice(getAllItems)}
                    onPaymentComplete={handlePaymentComplete}
                    onPaymentError={handlePaymentError}
                />
            </>
        );
    }

    return (
        <div className='mobile-container'>
            <header className='header'>
                <h1 className='restaurant-name'>{sampleMenuData.restaurant.name}</h1>
                <p className='restaurant-subtitle'>{sampleMenuData.restaurant.subtitle}</p>

                <LanguageSelector
                    selectedLang={selectedLang}
                    onLanguageChange={setSelectedLang}
                />
            </header>

            <main>
                {Object.entries(sampleMenuData.menu).map(([category, items]) => (
                    <MenuSection
                        key={category}
                        category={category}
                        items={items}
                        selectedLang={selectedLang}
                        cart={cart}
                        onAddToCart={addToCart}
                        onRemoveFromCart={removeFromCart}
                        onShowReviews={setShowReviews}
                    />
                ))}
            </main>

            {getTotalItems() > 0 && (
                <div className='order-summary'>
                    <button
                        className='order-btn'
                        onClick={handleOrderClick}
                    >
                        주문하기 ({getTotalItems()}개) - {formatPrice(getTotalPrice(getAllItems), selectedLang)}
                    </button>
                </div>
            )}

            <ReviewModal
                item={showReviews}
                selectedLang={selectedLang}
                isOpen={!!showReviews}
                onClose={() => setShowReviews(null)}
            />

            {/* 알림 권한 요청 모달 */}
            <NotificationPermissionModal
                isOpen={showNotificationModal}
                onAllow={handleAllowNotifications}
                onDeny={handleDenyNotifications}
                selectedLang={selectedLang}
            />

            {/* 결제 에러 알림 */}
            {paymentError && (
                <div className='payment-error-toast'>
                    <div className='error-message'>[오류] {paymentError}</div>
                    <button onClick={() => setPaymentError(null)}>×</button>
                </div>
            )}
        </div>
    );
}

export default App;
