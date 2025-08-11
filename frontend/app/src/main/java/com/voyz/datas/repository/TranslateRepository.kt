package com.voyz.datas.repository

import com.voyz.datas.network.ApiClient
import com.voyz.datas.network.ReviewTranslateRequest
import com.voyz.datas.network.TranslateApiService

class TranslateRepository {
    private val api = ApiClient.translateApiService
    
    suspend fun translateReviews(reviews: List<String>, targetLanguage: String = "ko"): List<String> {
        return try {
            val request = ReviewTranslateRequest(reviews, targetLanguage)
            val response = api.translateReviews(request)
            response.translated_reviews
        } catch (e: Exception) {
            e.printStackTrace()
            // 번역 실패 시 원본 반환
            reviews
        }
    }
}