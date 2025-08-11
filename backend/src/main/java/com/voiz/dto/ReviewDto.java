package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {
    private double rating; // 평균 평점
    private int reviewCount; // 리뷰 개수
    private List<ReviewResponseDto> reviews; // 리뷰 목록
}