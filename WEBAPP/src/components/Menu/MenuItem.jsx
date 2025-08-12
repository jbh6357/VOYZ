import {
  getItemName,
  getItemDescription,
  formatPrice,
  getCountryFlag,
  getReviewText,
} from "../../utils/helpers.js";

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
            <span className="stars">{"★".repeat(Math.floor(item.rating))}</span>
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
            <span className="stars">☆☆☆☆☆</span>
            <span className="rating-text placeholder">평가 대기중</span>
          </div>
        )}

        {item?.reviews && item.reviews.length > 0 && item.reviews[0] ? (
          <div className="review-snippet">
            <span className="review-flag">
              [{getCountryFlag(item.reviews[0]?.countryCode)}]
            </span>
            <span>
              {item.reviews[0].text} -{" "}
              <span className="review-country no-translate-flag">
                {item.reviews[0]?.user || "익명"}
              </span>
            </span>
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
