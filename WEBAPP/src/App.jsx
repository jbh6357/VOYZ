import { useState } from 'react'

// Components
import LanguageSelector from './components/UI/LanguageSelector.jsx'
import MenuSection from './components/Menu/MenuSection.jsx'
import OrderPage from './components/Menu/OrderPage.jsx'
import ReviewModal from './components/Review/ReviewModal.jsx'
import PaymentModal from './components/Payment/PaymentModal.jsx'
import TossPaymentWidget from './components/Payment/TossPaymentWidget.jsx'
import PayPalPaymentWidget from './components/Payment/PayPalPaymentWidget.jsx'
import SuccessPage from './components/UI/SuccessPage.jsx'

// Data & Utils
import { sampleMenuData } from './data/sampleData.js'
import { useMenu } from './hooks/useMenu.js'
import { formatPrice } from './utils/helpers.js'

function App() {
  const [selectedLang, setSelectedLang] = useState('ko')
  const [showReviews, setShowReviews] = useState(null)
  const [currentPage, setCurrentPage] = useState('menu')
  const [showPaymentModal, setShowPaymentModal] = useState(false)
  const [showTossWidget, setShowTossWidget] = useState(false)
  const [showPayPalWidget, setShowPayPalWidget] = useState(false)
  const [paymentError, setPaymentError] = useState(null)

  const {
    cart,
    addToCart,
    removeFromCart,
    getTotalItems,
    getTotalPrice,
    clearCart
  } = useMenu()

  // 토스페이먼츠는 TossPaymentWidget 컴포넌트에서 직접 로드

  const getAllItems = () => {
    return Object.values(sampleMenuData.menu).flat()
  }

  const handleOrderClick = () => {
    setCurrentPage('order')
  }

  const handlePaymentClick = () => {
    setShowPaymentModal(true)
  }

  const handleTossPayment = async () => {
    setShowPaymentModal(false)
    setShowTossWidget(true)
  }

  const handlePayPalPayment = () => {
    setShowPaymentModal(false)
    setShowPayPalWidget(true)
  }

  const handlePaymentError = (errorMessage) => {
    setPaymentError(errorMessage)
    setShowTossWidget(false)
    setShowPayPalWidget(false)
    setTimeout(() => setPaymentError(null), 5000) // 5초 후 에러 메시지 제거
  }

  const handlePaymentComplete = (paymentDetails) => {
    console.log('결제 완료:', paymentDetails)
    setShowTossWidget(false)
    setShowPayPalWidget(false)
    setCurrentPage('success')
    clearCart()
  }

  const handleBackToMenu = () => {
    setCurrentPage('menu')
    clearCart()
  }

  if (currentPage === 'success') {
    return <SuccessPage onBackToMenu={handleBackToMenu} />
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
    )
  }

  return (
    <div className="mobile-container">
      <header className="header">
        <h1 className="restaurant-name">{sampleMenuData.restaurant.name}</h1>
        <p className="restaurant-subtitle">{sampleMenuData.restaurant.subtitle}</p>
        
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
        <div className="order-summary">
          <button className="order-btn" onClick={handleOrderClick}>
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

      {/* 결제 에러 알림 */}
      {paymentError && (
        <div className="payment-error-toast">
          <div className="error-message">
            [오류] {paymentError}
          </div>
          <button onClick={() => setPaymentError(null)}>×</button>
        </div>
      )}
    </div>
  )
}

export default App