package com.voiz.service;

import com.voiz.dto.ReviewRequestDto;
import com.voiz.dto.ReviewResponseDto;
import com.voiz.mapper.ReviewRepository;
import com.voiz.vo.MenusReviews;
import com.voiz.vo.Reviews;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    
   
    public void saveReview(ReviewRequestDto reviewRequestDto) {
        Reviews review = new Reviews();
        review.setOrderId(reviewRequestDto.getOrderId());
        review.setUserId(reviewRequestDto.getUserId());
        review.setComment(reviewRequestDto.getComment());
        review.setRating(reviewRequestDto.getRating());
        review.setNationality(reviewRequestDto.getNationality());
        review.setLanguage(reviewRequestDto.getLanguage());

        reviewRepository.save(review);
    }





    public List<ReviewResponseDto> getReviewsByMenuId(String menuId, String userId, String nationality) {
        
        List<MenusReviews> reviewsWithDetails = reviewRepository.findByMenuId(menuId);

        return reviewsWithDetails.stream()
                // 이제 mr.getReviewId()는 Reviews 객체이므로 .getUserId() 호출이 가능합니다.
                .filter(mr -> userId == null || mr.getReviewId().getUserId().equals(userId))
                .filter(mr -> nationality == null || mr.getReviewId().getNationality().equals(nationality))
                .map(mr -> {
                    // mr.getReviewId()는 Reviews 객체이므로 변환이 가능합니다.
                    Reviews reviewDetails = mr.getReviewId(); 
                    ReviewResponseDto dto = new ReviewResponseDto();

                    dto.setReviewId(reviewDetails.getReviewId().intValue());
                    dto.setMenuId(mr.getMenuId());
                    dto.setOrderId(reviewDetails.getOrderId());
                    dto.setUserId(reviewDetails.getUserId());
                    dto.setComment(reviewDetails.getComment());
                    dto.setRating(reviewDetails.getRating());
                    dto.setNationality(reviewDetails.getNationality());
                    dto.setLanguage(reviewDetails.getLanguage());
                    dto.setCreatedAt(reviewDetails.getCreatedAt());

                    return dto;
                })
                .collect(Collectors.toList());
    }
}