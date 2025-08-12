package com.voyz.presentation.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voyz.datas.datastore.ReviewAnalysisCache
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.datas.repository.TranslateRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 로그인 시 실행되는 리뷰 번역 업데이트 Worker
 */
class ReviewTranslationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            println("📝 리뷰 번역 백그라운드 업데이트 시작")
            
            val userPreferencesManager = UserPreferencesManager(applicationContext)
            val userId = userPreferencesManager.userId.first()
            
            if (userId.isNullOrEmpty()) {
                println("❌ 사용자 ID가 없어서 리뷰 번역 업데이트 중단")
                return Result.success()
            }
            
            val analyticsRepo = AnalyticsRepository()
            val translateRepo = TranslateRepository()
            val analysisCache = ReviewAnalysisCache(applicationContext)
            
            // 최근 1개월 리뷰 데이터 가져오기
            val today = LocalDate.now()
            val oneMonthAgo = today.minusMonths(1).withDayOfMonth(1)
            val startDate = oneMonthAgo.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            val reviews = analyticsRepo.getReviews(userId, startDate, endDate)
            val reviewTexts = reviews.map { it.comment }
            val reviewsHash = analysisCache.generateReviewsHash(reviewTexts)
            
            // 기존 캐시와 해시값 비교
            val existingCache = analysisCache.getCachedAnalysis(userId).first()
            if (existingCache?.reviewsHash == reviewsHash) {
                println("✅ 리뷰 데이터 변경 없음, 번역 업데이트 생략")
                return Result.success()
            }
            
            // 새로운 리뷰가 있으면 번역 실행
            val translatedReviews = translateRepo.translateReviews(reviewTexts, "ko")
            
            // 리뷰/번역만 업데이트해서 캐시 저장
            analysisCache.saveAnalysis(
                userId = userId,
                insights = existingCache?.insights ?: emptyList(),
                translatedReviews = translatedReviews,
                menuSentiments = existingCache?.menuSentiments ?: emptyList(),
                countryRatings = existingCache?.countryRatings ?: emptyList(),
                reviewsHash = reviewsHash
            )
            
            println("✅ 리뷰 번역 백그라운드 업데이트 완료")
            Result.success()
            
        } catch (e: Exception) {
            println("❌ 리뷰 번역 백그라운드 업데이트 실패: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }
}