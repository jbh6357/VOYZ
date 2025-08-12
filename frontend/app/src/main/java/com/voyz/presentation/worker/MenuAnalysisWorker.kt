package com.voyz.presentation.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voyz.datas.datastore.ReviewAnalysisCache
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.AnalyticsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 매일 아침 9시에 실행되는 메뉴별 분석 업데이트 Worker
 */
class MenuAnalysisWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            println("🍽️ 메뉴별 분석 백그라운드 업데이트 시작")
            
            val userPreferencesManager = UserPreferencesManager(applicationContext)
            val userId = userPreferencesManager.userId.first()
            
            if (userId.isNullOrEmpty()) {
                println("❌ 사용자 ID가 없어서 메뉴별 분석 업데이트 중단")
                return Result.success()
            }
            
            val analyticsRepo = AnalyticsRepository()
            val analysisCache = ReviewAnalysisCache(applicationContext)
            
            // 최근 1개월 데이터로 분석
            val today = LocalDate.now()
            val oneMonthAgo = today.minusMonths(1).withDayOfMonth(1)
            val startDate = oneMonthAgo.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            // 1. 전체 메뉴 분석 저장 (nationality 포함)
            val allMenuSentiments = analyticsRepo.getMenuSentiment(userId, startDate, endDate, 4, 2, null, true)
            val menuMaps = allMenuSentiments.map { dto ->
                mapOf<String, Any>(
                    "menuId" to dto.menuId,
                    "menuName" to (dto.menuName ?: ""),
                    "positiveCount" to dto.positiveCount,
                    "negativeCount" to dto.negativeCount,
                    "neutralCount" to dto.neutralCount,
                    "averageRating" to dto.averageRating,
                    "reviewSummary" to (dto.reviewSummary ?: ""),
                    "nationality" to (dto.nationality ?: "")
                )
            }
            analysisCache.saveMenuAnalysis(userId, null, menuMaps) // 전체 데이터
            
            // 2. 이제 전체 데이터에 nationality가 포함되므로 개별 저장 불필요
            
            // 2. 국가별 평점 분석 저장
            val countryRatings = analyticsRepo.getCountryRatings(userId, startDate, endDate)
            val dateRangeKey = "${startDate}_${endDate}"
            val countryMaps = countryRatings.map { dto ->
                mapOf<String, Any>(
                    "nationality" to dto.nationality,
                    "count" to dto.count,
                    "averageRating" to dto.averageRating
                )
            }
            analysisCache.saveCountryRatings(userId, dateRangeKey, countryMaps)
            
            println("✅ 메뉴별 분석 백그라운드 업데이트 완료")
            Result.success()
            
        } catch (e: Exception) {
            println("❌ 메뉴별 분석 백그라운드 업데이트 실패: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }
}