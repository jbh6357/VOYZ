package com.voyz.datas.network

import retrofit2.http.Body
import retrofit2.http.POST

data class ReviewTranslateRequest(
    val reviews: List<String>,
    val targetLanguage: String = "ko"
)

data class ReviewTranslateResponse(
    val translated_reviews: List<String>
)

interface TranslateApiService {
    @POST("translate/reviews")
    suspend fun translateReviews(@Body request: ReviewTranslateRequest): ReviewTranslateResponse
}