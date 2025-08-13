import { useState } from "react";
import Modal from "../UI/Modal.jsx";
import StarRating from "../UI/StarRating.jsx";
import {
  getItemName,
  getReviewText,
  getCountryName,
  getFilteredReviews,
  getUniqueCountries,
} from "../../utils/helpers.js";

const ReviewModal = ({
  item,
  selectedLang,
  isOpen,
  onClose,
  reviewViewMode,
  setReviewViewMode,
}) => {
  const [selectedCountryFilter, setSelectedCountryFilter] = useState("all");

  if (!item) return null;

  const filteredReviews = getFilteredReviews(
    item.reviews,
    selectedCountryFilter
  );
  const uniqueCountries = getUniqueCountries(item.reviews, getCountryName);

  return (
    <Modal
      key={reviewViewMode}
      isOpen={isOpen}
      onClose={onClose}
      className="reviews-modal"
    >
      <div className="modal-header">
        <h3>{getItemName(item, selectedLang)} Reviews</h3>
        <button className="close-btn" onClick={onClose}>
          ×
        </button>
      </div>

      <div className="review-controls">
        <div className="view-mode-controls">
          <button
            className={`mode-btn ${
              reviewViewMode === "translated" ? "active" : ""
            }`}
            onClick={() => setReviewViewMode("translated")}
          >
            번역본
          </button>
          <button
            className={`mode-btn ${
              reviewViewMode === "original" ? "active" : ""
            }`}
            onClick={() => setReviewViewMode("original")}
          >
            원문
          </button>
        </div>

        <div className="country-filter">
          <select
            value={selectedCountryFilter}
            onChange={(e) => setSelectedCountryFilter(e.target.value)}
            className="country-select"
          >
            <option value="all">All Countries</option>
            {uniqueCountries.map((country) => (
              <option 
                key={country.code} 
                value={country.code}
              >
                {country.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div
        className={`reviews-list ${
          reviewViewMode === "original" ? "original-mode" : ""
        }`}
      >
        {filteredReviews.map((review, index) => {
          const countryCode = review.nationality || review.countryCode;
          const countryName = getCountryName(countryCode);
          
          return (
            <div key={index} className="review-item">
              <div className="review-header">
                <span className="review-user">
                  <span className="review-country">
                    [{countryName}]
                  </span>
                  <span className="review-author no-translate-flag">{review.guestName || review.user}</span>
                </span>
                <span className="review-rating no-translate-flag">
                  <StarRating rating={review.rating} />
                </span>
              </div>
              <div className="review-text">{review.comment || review.text}</div>
            </div>
          );
        })}
      </div>
    </Modal>
  );
};

export default ReviewModal;
