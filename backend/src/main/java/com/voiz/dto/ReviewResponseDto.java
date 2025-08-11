package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDto {

    private int reviewIdx;
    // private String restaurantId; // 레스토랑 ID
    private int menuIdx; 
    private int orderIdx;
    private String userId;
    private String comment;
    private int rating;
    private String nationality;
    private String language;
    private String createdAt; // 리뷰 작성 시간
    
}
