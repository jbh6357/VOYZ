package com.voiz.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDto {

    private String orderId;
    private String userId;
    private Long menuIdx;
    private int rating;
    private String comment;
    private String nationality;
    private String language;
    
    
}
