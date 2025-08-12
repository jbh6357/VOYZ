package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDto {

    private int orderIdx;
    private String userId;
    private String guestName; // 리뷰 작성자 이름
    private int menuIdx;
    private int rating;
    private String comment;
    private String nationality;
    private String language;
    
    
}
