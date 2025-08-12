package com.voyz.datas.model.dto

data class CountryRatingItem(
    val nationality: String,
    val flag: String,
    val count: Long,
    val averageRating: Double,
    val percentage: Float
)