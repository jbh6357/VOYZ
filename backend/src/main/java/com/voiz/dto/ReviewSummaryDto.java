package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDto {
    private long totalReviews;
    private double averageRating;
    private long positiveCount;
    private long negativeCount;
}


