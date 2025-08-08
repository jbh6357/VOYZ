import { useState } from 'react'
import Modal from '../UI/Modal.jsx'
import { getItemName, getReviewText, getCountryFlag, getFilteredReviews, getUniqueCountries } from '../../utils/helpers.js'

const ReviewModal = ({ item, selectedLang, isOpen, onClose }) => {
  const [reviewViewMode, setReviewViewMode] = useState('translated')
  const [selectedCountryFilter, setSelectedCountryFilter] = useState('all')

  if (!item) return null

  const filteredReviews = getFilteredReviews(item.reviews, selectedCountryFilter)
  const uniqueCountries = getUniqueCountries(item.reviews, getCountryFlag)

  return (
    <Modal 
      isOpen={isOpen}
      onClose={onClose}
      className="reviews-modal"
    >
      <div className="modal-header">
        <h3>{getItemName(item, selectedLang)} Reviews</h3>
        <button className="close-btn" onClick={onClose}>×</button>
      </div>
      
      <div className="review-controls">
        <div className="view-mode-controls">
          <button 
            className={`mode-btn ${reviewViewMode === 'translated' ? 'active' : ''}`}
            onClick={() => setReviewViewMode('translated')}
          >
            번역본
          </button>
          <button 
            className={`mode-btn ${reviewViewMode === 'original' ? 'active' : ''}`}
            onClick={() => setReviewViewMode('original')}
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
            {uniqueCountries.map(country => (
              <option key={country.code} value={country.code}>
                [{country.flag}] {country.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="reviews-list">
        {filteredReviews.map((review, index) => (
          <div key={index} className="review-item">
            <div className="review-header">
              <span className="review-user">
                <span className="review-flag">[{getCountryFlag(review.countryCode)}]</span>
                {review.user}
              </span>
              <span className="review-country">({review.countryCode})</span>
            </div>
            <div className="review-text">"{getReviewText(review, selectedLang, reviewViewMode)}"</div>
            {reviewViewMode === 'translated' && selectedLang !== (review.countryCode === 'KR' ? 'ko' : 'en') && (
              <div className="original-text">원문: "{review.countryCode === 'KR' ? review.text.ko : review.text.en}"</div>
            )}
          </div>
        ))}
      </div>
    </Modal>
  )
}

export default ReviewModal