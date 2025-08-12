package com.voyz.datas.model.dto

data class ReviewSummaryDto(
    val totalReviews: Long,
    val averageRating: Double,
    val positiveCount: Long,
    val negativeCount: Long,
)


