import { getItemName, getItemDescription, formatPrice, getCountryFlag, getReviewText } from '../../utils/helpers.js'

const MenuItem = ({ 
  item, 
  selectedLang, 
  reviewViewMode, 
  cart, 
  onAddToCart, 
  onRemoveFromCart, 
  onShowReviews 
}) => {
  return (
    <div className="menu-item">
      <div className="item-info" onClick={() => onAddToCart(item)}>
        <div className="item-name">{getItemName(item, selectedLang)}</div>
        <div className="item-description">{getItemDescription(item, selectedLang)}</div>
        <div className="item-price">{formatPrice(item.price, selectedLang)}</div>
      </div>
      
      <div className="reviews-preview">
        <div className="rating">
          <span className="stars">{'*'.repeat(Math.floor(item.rating))}</span>
          <span className="rating-text">
            {item.rating} ({item.reviewCount} reviews)
          </span>
          <button 
            className="view-reviews-btn"
            onClick={() => onShowReviews(item)}
          >
            View All
          </button>
        </div>
        <div className="review-snippet">
          <span className="review-flag">[{getCountryFlag(item.reviews[0]?.countryCode)}]</span>
          "{getReviewText(item.reviews[0], selectedLang, reviewViewMode)}" - {item.reviews[0]?.user}
        </div>
      </div>

      {cart[item.id] && (
        <div className="cart-controls">
          <button onClick={() => onRemoveFromCart(item.id)}>-</button>
          <span>{cart[item.id]}</span>
          <button onClick={() => onAddToCart(item)}>+</button>
        </div>
      )}
    </div>
  )
}

export default MenuItem