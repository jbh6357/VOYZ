import './StarRating.css';

const StarRating = ({ rating = 0, maxStars = 5 }) => {
  const stars = [];
  
  for (let i = 1; i <= maxStars; i++) {
    const starFill = Math.min(Math.max(rating - i + 1, 0), 1);
    
    if (starFill === 0) {
      // 빈 별
      stars.push(
        <span key={i} className="star empty">☆</span>
      );
    } else if (starFill === 1) {
      // 꽉 찬 별
      stars.push(
        <span key={i} className="star filled">★</span>
      );
    } else {
      // 부분 채운 별
      stars.push(
        <span key={i} className="star partial" style={{'--fill': `${starFill * 100}%`}}>
          <span className="star-bg">☆</span>
          <span className="star-fill">★</span>
        </span>
      );
    }
  }
  
  return <span className="star-rating">{stars}</span>;
};

export default StarRating;