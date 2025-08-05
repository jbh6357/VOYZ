import { useEffect } from 'react'
import { scheduleReviewReminder } from '../../utils/pushNotifications.js'

const SuccessPage = ({ onBackToMenu, orderedItems }) => {
  useEffect(() => {
    // 주문 완료 후 10초 뒤에 푸시 알림 발송
    if (orderedItems && orderedItems.length > 0) {
      console.log('📝 주문 완료 페이지: 10초 후 푸시 알림 예약')
      scheduleReviewReminder({
        items: orderedItems,
        timestamp: new Date().toISOString()
      }, 0.17) // 0.17분 = 10초 (테스트용)
    }
  }, [orderedItems])

  return (
    <div className="success-page">
      <div className="success-content">
        <div className="success-icon">[완료]</div>
        <h1>주문이 완료되었습니다!</h1>
        <p>맛있는 식사 되세요!</p>
        <button className="back-to-menu-btn" onClick={onBackToMenu}>
          메뉴로 돌아가기
        </button>
      </div>
    </div>
  )
}

export default SuccessPage