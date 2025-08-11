package com.voyz.presentation.service

import android.content.Context
import com.voyz.datas.datastore.ReviewAnalysisCache
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.datas.repository.TranslateRepository
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BackgroundAnalysisService(private val context: Context) {
    
    private val analyticsRepo = AnalyticsRepository()
    private val translateRepo = TranslateRepository()
    private val analysisCache = ReviewAnalysisCache(context)
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 로그인 시 백그라운드에서 분석 데이터 미리 준비
     */
    fun preloadAnalysisData(userId: String) {
        serviceScope.launch {
            try {
                println("🔄 백그라운드 분석 시작: $userId")
                
                // 1. 최근 1개월 리뷰 데이터 가져오기
                val today = LocalDate.now()
                val oneMonthAgo = today.minusMonths(1).withDayOfMonth(1)
                val startDate = oneMonthAgo.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                val reviews = analyticsRepo.getReviews(userId, startDate, endDate)
                val reviewTexts = reviews.map { it.comment }
                val reviewsHash = analysisCache.generateReviewsHash(reviewTexts)
                
                // 2. 캐시된 데이터 확인
                analysisCache.getCachedAnalysis(userId).collect { cached ->
                    if (cached?.reviewsHash == reviewsHash) {
                        println("✅ 캐시된 분석 데이터 사용 가능")
                        return@collect
                    }
                    
                    // 3. 캐시가 없거나 만료된 경우 새로 분석
                    println("🔄 새로운 분석 시작...")
                    
                    // 병렬로 처리해서 속도 향상
                    val insightsDeferred = async { 
                        try {
                            analyticsRepo.getComprehensiveInsights(userId)
                        } catch (e: Exception) {
                            println("❌ 인사이트 분석 실패: ${e.message}")
                            emptyMap<String, Any>()
                        }
                    }
                    
                    val translationDeferred = async {
                        try {
                            translateRepo.translateReviews(reviewTexts, "ko")
                        } catch (e: Exception) {
                            println("❌ 번역 실패: ${e.message}")
                            reviewTexts // 원본 반환
                        }
                    }
                    
                    val menuSentimentDeferred = async {
                        try {
                            val menuSentiments = analyticsRepo.getMenuSentiment(userId, startDate, endDate, 4, 2, null, true)
                            menuSentiments.map { dto ->
                                mapOf<String, Any>(
                                    "menuId" to dto.menuId,
                                    "menuName" to (dto.menuName ?: ""),
                                    "positiveCount" to dto.positiveCount,
                                    "negativeCount" to dto.negativeCount,
                                    "neutralCount" to dto.neutralCount,
                                    "averageRating" to dto.averageRating,
                                    "reviewSummary" to (dto.reviewSummary ?: "")
                                )
                            }
                        } catch (e: Exception) {
                            println("❌ 메뉴별 분석 실패: ${e.message}")
                            emptyList<Map<String, Any>>()
                        }
                    }
                    
                    val countryRatingDeferred = async {
                        try {
                            val countryRatings = analyticsRepo.getCountryRatings(userId, startDate, endDate)
                            countryRatings.map { dto ->
                                mapOf<String, Any>(
                                    "nationality" to dto.nationality,
                                    "count" to dto.count,
                                    "averageRating" to dto.averageRating
                                )
                            }
                        } catch (e: Exception) {
                            println("❌ 국가별 평점 분석 실패: ${e.message}")
                            emptyList<Map<String, Any>>()
                        }
                    }
                    
                    // 결과 대기
                    val insightsResponse = insightsDeferred.await()
                    val translatedReviews = translationDeferred.await()
                    val menuSentiments = menuSentimentDeferred.await()
                    val countryRatings = countryRatingDeferred.await()
                    
                    // 4. 인사이트 데이터 변환
                    val insightsList = insightsResponse["insights"] as? List<Map<String, Any>> ?: emptyList()
                    val simplifiedInsights = insightsList.map { insightMap ->
                        mapOf(
                            "type" to (insightMap["type"] as? String ?: "trend"),
                            "title" to (insightMap["title"] as? String ?: ""),
                            "description" to (insightMap["description"] as? String ?: ""),
                            "priority" to (insightMap["priority"] as? String ?: "medium"),
                            "suggestedFilters" to (insightMap["suggestedFilters"] as? Map<String, String> ?: emptyMap())
                        )
                    }
                    
                    // 5. 캐시에 저장
                    analysisCache.saveAnalysis(
                        userId = userId,
                        insights = simplifiedInsights,
                        translatedReviews = translatedReviews,
                        menuSentiments = menuSentiments,
                        countryRatings = countryRatings,
                        reviewsHash = reviewsHash
                    )
                    
                    println("✅ 백그라운드 분석 완료 및 캐시 저장")
                }
                
            } catch (e: Exception) {
                println("❌ 백그라운드 분석 실패: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 서비스 종료
     */
    fun shutdown() {
        serviceScope.cancel()
    }
}