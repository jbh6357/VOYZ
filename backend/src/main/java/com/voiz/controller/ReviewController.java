package com.voiz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voiz.dto.ReviewRequestDto;
import com.voiz.dto.ReviewResponseDto;
import com.voiz.service.ReviewService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/review")
@Tag(name = "review", description = "리뷰 관리 API")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;

    @PostMapping("/")
    public String createReview(@RequestBody ReviewRequestDto reviewRequestDto) {
        reviewService.saveReview(reviewRequestDto);

        return "리뷰 생성 완료";
    }

    // @GetMapping("/restaurant/{restaurantId}")
    // public List<ReviewResponseDto> getReviewsByRestaurant(@PathVariable String restaurantId) {
    //     return reviewService.getReviewsByRestaurantId(restaurantId);
    // }


    

//     @GetMapping("/menu/{menuId}")
//     public List<ReviewResponseDto> getReviewsByMenuId(
//         @PathVariable String menuId,
//         @RequestParam(required = false) String userId,
//         @RequestParam(required = false) String nationality) {
//     return reviewService.getReviewsByMenuId(menuId, userId, nationality);
// }

}
