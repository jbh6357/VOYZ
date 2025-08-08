import { useState } from 'react'
import { getItemName, formatPrice, getCountryFlag } from '../../utils/helpers.js'
import { COUNTRY_FLAGS } from '../../types/index.js'

const WriteReviewPage = ({ orderedItems, selectedLang, onSubmitReview, onSkip }) => {
  const [selectedItemId, setSelectedItemId] = useState(orderedItems[0]?.id || '')
  const [userName, setUserName] = useState('')
  const [countryCode, setCountryCode] = useState('KR')
  const [reviewText, setReviewText] = useState('')
  const [rating, setRating] = useState(5)

  const handleSubmit = (e) => {
    e.preventDefault()
    
    const review = {
      itemId: selectedItemId,
      user: userName,
      countryCode: countryCode,
      text: {
        [selectedLang]: reviewText
      },
      rating: rating,
      date: new Date().toISOString()
    }
    
    onSubmitReview(review)
  }

  return (
    <div className="write-review-page">
      <div className="page-header">
        <h2>{selectedLang === 'ko' ? '리뷰 작성하기' : 'Write a Review'}</h2>
        <p className="subtitle">
          {selectedLang === 'ko' 
            ? '맛있게 드셨나요? 다른 고객님들을 위해 리뷰를 남겨주세요!' 
            : 'Did you enjoy your meal? Leave a review for other customers!'}
        </p>
      </div>

      <form onSubmit={handleSubmit} className="review-form">
        <div className="form-group">
          <label>{selectedLang === 'ko' ? '어떤 메뉴를 평가하시겠습니까?' : 'Which item would you like to review?'}</label>
          <select 
            value={selectedItemId} 
            onChange={(e) => setSelectedItemId(e.target.value)}
            className="item-select"
            required
          >
            {orderedItems.map(item => (
              <option key={item.id} value={item.id}>
                {getItemName(item, selectedLang)} - {formatPrice(item.price, selectedLang)}
              </option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>{selectedLang === 'ko' ? '이름' : 'Name'}</label>
            <input
              type="text"
              value={userName}
              onChange={(e) => setUserName(e.target.value)}
              placeholder={selectedLang === 'ko' ? '홍길동' : 'John Doe'}
              required
            />
          </div>

          <div className="form-group">
            <label>{selectedLang === 'ko' ? '국가' : 'Country'}</label>
            <select 
              value={countryCode} 
              onChange={(e) => setCountryCode(e.target.value)}
              className="country-select"
              required
            >
              {Object.entries(COUNTRY_FLAGS).map(([code, flag]) => (
                <option key={code} value={code}>
                  [{flag}] {code}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="form-group">
          <label>{selectedLang === 'ko' ? '평점' : 'Rating'}</label>
          <div className="rating-selector">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                type="button"
                className={`star-btn ${star <= rating ? 'active' : ''}`}
                onClick={() => setRating(star)}
              >
                ★
              </button>
            ))}
            <span className="rating-text">{rating}.0</span>
          </div>
        </div>

        <div className="form-group">
          <label>{selectedLang === 'ko' ? '리뷰 내용' : 'Review'}</label>
          <textarea
            value={reviewText}
            onChange={(e) => setReviewText(e.target.value)}
            placeholder={selectedLang === 'ko' 
              ? '음식은 어떠셨나요? 다른 고객님들께 도움이 될 수 있도록 솔직한 리뷰를 남겨주세요.'
              : 'How was your food? Please leave an honest review to help other customers.'}
            rows={5}
            required
          />
        </div>

        <div className="form-actions">
          <button type="submit" className="submit-btn">
            {selectedLang === 'ko' ? '리뷰 작성 완료' : 'Submit Review'}
          </button>
          <button type="button" onClick={onSkip} className="skip-btn">
            {selectedLang === 'ko' ? '건너뛰기' : 'Skip'}
          </button>
        </div>
      </form>
    </div>
  )
}

export default WriteReviewPage