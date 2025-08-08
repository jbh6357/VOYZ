import { useState } from 'react'
import Modal from './Modal.jsx'

const NotificationPermissionModal = ({ isOpen, onAllow, onDeny, selectedLang }) => {
  const [isLoading, setIsLoading] = useState(false)

  const handleAllow = async () => {
    setIsLoading(true)
    try {
      await onAllow()
    } finally {
      setIsLoading(false)
    }
  }

  const content = {
    ko: {
      title: '🔔 알림 허용',
      subtitle: '더 나은 서비스를 위해 알림을 허용해 주세요',
      benefits: [
        '주문 완료 후 리뷰 작성 안내',
        '특별 할인 및 이벤트 알림',
        '새로운 메뉴 출시 소식'
      ],
      allowBtn: '알림 허용',
      denyBtn: '나중에',
      note: '설정에서 언제든지 변경할 수 있습니다.'
    },
    en: {
      title: '🔔 Enable Notifications',
      subtitle: 'Allow notifications for better service',
      benefits: [
        'Review reminders after orders',
        'Special discounts and events',
        'New menu announcements'
      ],
      allowBtn: 'Allow Notifications',
      denyBtn: 'Maybe Later',
      note: 'You can change this in settings anytime.'
    }
  }

  const text = content[selectedLang] || content.ko

  return (
    <Modal isOpen={isOpen} onClose={onDeny} className="notification-permission-modal">
      <div className="permission-content">
        <div className="permission-header">
          <h3>{text.title}</h3>
          <p className="permission-subtitle">{text.subtitle}</p>
        </div>

        <div className="permission-benefits">
          <ul>
            {text.benefits.map((benefit, index) => (
              <li key={index}>
                <span className="benefit-icon">✓</span>
                {benefit}
              </li>
            ))}
          </ul>
        </div>

        <div className="permission-actions">
          <button 
            onClick={handleAllow} 
            className="allow-btn"
            disabled={isLoading}
          >
            {isLoading ? '처리 중...' : text.allowBtn}
          </button>
          <button onClick={onDeny} className="deny-btn">
            {text.denyBtn}
          </button>
        </div>

        <p className="permission-note">{text.note}</p>
      </div>
    </Modal>
  )
}

export default NotificationPermissionModal