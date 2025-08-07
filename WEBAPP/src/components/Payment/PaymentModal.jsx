import Modal from '../UI/Modal.jsx'

const PaymentModal = ({ isOpen, onClose, onTossPayment, onPayPalPayment }) => {
  return (
    <Modal 
      isOpen={isOpen}
      onClose={onClose}
      className="payment-modal"
    >
      <div className="modal-header">
        <h3>결제 방법 선택</h3>
        <button className="close-btn" onClick={onClose}>×</button>
      </div>
      <div className="payment-methods">
        <button className="payment-method toss-official" onClick={onTossPayment}>
          <div className="toss-official-logo">toss</div>
          <div className="payment-info">
            <div className="payment-name">토스페이</div>
            <div className="payment-desc">간편하고 안전한 결제</div>
          </div>
        </button>
        
        <button className="payment-method paypal-official" onClick={onPayPalPayment}>
          <div className="paypal-official-logo">
            <span className="paypal-blue">Pay</span>
            <span className="paypal-blue-light">Pal</span>
          </div>
          <div className="payment-info">
            <div className="payment-name">PayPal</div>
            <div className="payment-desc">Safe & Secure Payment</div>
          </div>
        </button>
      </div>
    </Modal>
  )
}

export default PaymentModal