package com.voiz.service;

import com.voiz.dto.ReviewDto;
import com.voiz.dto.ReviewRequestDto;
import com.voiz.dto.ReviewResponseDto;
import com.voiz.mapper.ReviewRepository;
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
        review.setMenuIdx(reviewRequestDto.getMenuIdx()); // menuIdx 저장 로직 추가
        review.setOrderIdx(reviewRequestDto.getOrderIdx());
        review.setUserId(reviewRequestDto.getUserId());
        review.setComment(reviewRequestDto.getComment());
        review.setRating(reviewRequestDto.getRating());
        review.setNationality(reviewRequestDto.getNationality());
        review.setLanguage(reviewRequestDto.getLanguage());

        reviewRepository.save(review);
    }

   
    
     // 메뉴별 리뷰 목록 조회 메서드
    public ReviewDto getReviewsByMenuId(int menuIdx, String userId, String nationality) {
        
        // Repository에서 menuId에 해당하는 리뷰 목록을 가져오기
        List<Reviews> reviews = reviewRepository.findByMenuIdx(menuIdx);

        // userId, nationality 조건으로 필터링하고 DTO로 변환
        List<ReviewResponseDto> reviewDtos = reviews.stream()
                .filter(review -> userId == null || review.getUserId().equals(userId))
                .filter(review -> nationality == null || review.getNationality().equals(nationality))
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 리뷰 개수
        int reviewCount = reviewDtos.size();

        // 평균 평점 (리뷰가 없으면 0.0)
        double rating = reviewCount > 0
                ? reviewDtos.stream().mapToInt(ReviewResponseDto::getRating).average().orElse(0.0)
                : 0.0;

        // ReviewDto로 감싸서 반환
        return new ReviewDto(rating, reviewCount, reviewDtos);
    }

    
    // Reviews 엔티티를 ReviewResponseDto로 변환
    private ReviewResponseDto convertToDto(Reviews review) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setReviewIdx(review.getReviewIdx().intValue());
        dto.setMenuIdx(review.getMenuIdx());
        dto.setOrderIdx(review.getOrderIdx());
        dto.setUserId(review.getUserId());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setNationality(review.getNationality());
        dto.setLanguage(review.getLanguage());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
    
}
