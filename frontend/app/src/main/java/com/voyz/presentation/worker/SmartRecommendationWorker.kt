package com.voyz.presentation.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voyz.datas.datastore.ReviewAnalysisCache
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.AnalyticsRepository
import kotlinx.coroutines.flow.first

/**
 * 월요일 오전 9시에 실행되는 스마트 추천 업데이트 Worker
 */
class SmartRecommendationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            println("🤖 스마트 추천 백그라운드 업데이트 시작")
            
            val userPreferencesManager = UserPreferencesManager(applicationContext)
            val userId = userPreferencesManager.userId.first()
            
            if (userId.isNullOrEmpty()) {
                println("❌ 사용자 ID가 없어서 스마트 추천 업데이트 중단")
                return Result.success()
            }
            
            val analyticsRepo = AnalyticsRepository()
            val analysisCache = ReviewAnalysisCache(applicationContext)
            
            // AI 기반 종합 인사이트 생성 (GPT 사용)
            val insightsResponse = analyticsRepo.getComprehensiveInsights(userId)
            val insightsList = insightsResponse["insights"] as? List<Map<String, Any>> ?: emptyList()
            val simplifiedInsights = insightsList.map { insightMap ->
                mapOf(
                    "type" to (insightMap["type"] as? String ?: "trend"),
                    "title" to (insightMap["title"] as? String ?: ""),
                    "description" to (insightMap["description"] as? String ?: ""),
                    "priority" to (insightMap["priority"] as? String ?: "medium")
                )
            }
            
            // 기존 캐시 가져오기
            val existingCache = analysisCache.getCachedAnalysis(userId).first()
            
            // 스마트 추천(인사이트)만 업데이트해서 캐시 저장
            analysisCache.saveAnalysis(
                userId = userId,
                insights = simplifiedInsights,
                translatedReviews = existingCache?.translatedReviews ?: emptyList(),
                menuSentiments = existingCache?.menuSentiments ?: emptyList(),
                countryRatings = existingCache?.countryRatings ?: emptyList(),
                reviewsHash = existingCache?.reviewsHash ?: ""
            )
            
            println("✅ 스마트 추천 백그라운드 업데이트 완료")
            Result.success()
            
        } catch (e: Exception) {
            println("❌ 스마트 추천 백그라운드 업데이트 실패: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }
}