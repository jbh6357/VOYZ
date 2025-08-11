package com.voyz.datas.model.dto

data class MenuSentimentDto(
    val menuId: Int,
    val menuName: String?,
    val positiveCount: Long,
    val negativeCount: Long,
    val neutralCount: Long,
    val averageRating: Double
)