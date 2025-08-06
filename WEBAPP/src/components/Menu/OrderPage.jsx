import { getItemName, formatPrice } from '../../utils/helpers.js'

const OrderPage = ({ 
  cart, 
  getAllItems, 
  selectedLang, 
  onAddToCart, 
  onRemoveFromCart, 
  onBackToMenu, 
  onPaymentClick, 
  getTotalPrice 
}) => {
  return (
    <div className="order-page">
      <header className="header">
        <button className="back-btn" onClick={onBackToMenu}>← Back</button>
        <h1>Order Summary</h1>
      </header>
      <div className="order-items">
        {Object.entries(cart).map(([itemId, count]) => {
          const item = getAllItems().find(item => item.id === parseInt(itemId))
          return (
            <div key={itemId} className="order-item">
              <div className="item-info">
                <div className="item-name">{getItemName(item, selectedLang)}</div>
                <div className="item-price">{formatPrice(item.price, selectedLang)}</div>
              </div>
              <div className="quantity-controls">
                <button onClick={() => onRemoveFromCart(parseInt(itemId))}>-</button>
                <span>{count}</span>
                <button onClick={() => onAddToCart(item)}>+</button>
              </div>
            </div>
          )
        })}
      </div>
      <div className="payment-section">
        <div className="total">Total: {formatPrice(getTotalPrice(), selectedLang)}</div>
        <button className="payment-btn" onClick={onPaymentClick}>
          결제하기
        </button>
      </div>
    </div>
  )
}

export default OrderPage