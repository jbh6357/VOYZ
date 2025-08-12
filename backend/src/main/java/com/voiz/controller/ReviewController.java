package com.voiz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody; 
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voiz.dto.ReviewDto;
import com.voiz.dto.ReviewRequestDto;
import com.voiz.dto.ReviewResponseDto;
import com.voiz.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/review")
@Tag(name = "review", description = "리뷰 관리 API")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;

    @PostMapping("/")
    @Operation(summary = "리뷰 생성")
    public String createReview(@RequestBody ReviewRequestDto reviewRequestDto) {
        reviewService.saveReview(reviewRequestDto);

        return "리뷰 생성 완료";
    }



    @GetMapping("/menu/{menuId}")
    @Operation(summary = "메뉴별 리뷰 목록 조회")
    public ResponseEntity<ReviewDto> getReviewsByMenuId(
            @PathVariable int menuId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String nationality) {
        
    	ReviewDto reviewData = reviewService.getReviewsByMenuId(menuId, userId, nationality);
        return ResponseEntity.ok(reviewData);
}

}
