import {
  getItemName,
  getItemDescription,
  formatPrice,
  getCountryName,
  getReviewText,
} from "../../utils/helpers.js";
import StarRating from "../UI/StarRating.jsx";

const MenuItem = ({
  item,
  selectedLang,
  reviewViewMode,
  cart,
  onAddToCart,
  onRemoveFromCart,
  onShowReviews,
}) => {
  return (
    <div className="menu-item">
      <div className="item-info" onClick={() => onAddToCart(item)}>
        <div className="item-name">{item.name}</div>
        {item.description ? (
          <div className="item-description">{item.description}</div>
        ) : (
          <div className="item-description placeholder">
            메뉴 설명 준비중입니다.
          </div>
        )}
        <div className="item-price">
          {formatPrice(item.price, selectedLang)}
        </div>
      </div>

      <div className="reviews-preview">
        {item?.rating && item.rating > 0 ? (
          <div className="rating">
            <StarRating rating={item.rating} />
            <span className="rating-text">
              {item.rating.toFixed(1)} ({item?.reviewCount || 0} reviews)
            </span>
            <button
              className="view-reviews-btn"
              onClick={() => onShowReviews(item)}
            >
              View All
            </button>
          </div>
        ) : (
          <div className="rating no-rating">
            <StarRating rating={0} />
            <span className="rating-text placeholder">평가 대기중</span>
          </div>
        )}

        {item?.reviews && item.reviews.length > 0 && item.reviews[0] ? (
          <div className="review-snippet">
            <div className="review-meta">
              <span className="review-country">
                [{getCountryName(item.reviews[0]?.countryCode)}]
              </span>
              <span className="review-author no-translate-flag">
                {item.reviews[0]?.user || "익명"}
              </span>
            </div>
            <div className="review-text">
              {item.reviews[0].text}
            </div>
          </div>
        ) : (
          <div className="review-snippet placeholder">
            아직 리뷰가 없습니다 (주문 후 리뷰 작성 가능)
          </div>
        )}
      </div>

      {cart[item.id] && (
        <div className="cart-controls">
          <button onClick={() => onRemoveFromCart(item.id)}>-</button>
          <span>{cart[item.id]}</span>
          <button onClick={() => onAddToCart(item)}>+</button>
        </div>
      )}
    </div>
  );
};

export default MenuItem;
