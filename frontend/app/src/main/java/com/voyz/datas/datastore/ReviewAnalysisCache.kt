package com.voyz.datas.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val Context.reviewAnalysisCacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "review_analysis_cache")

data class CachedReviewAnalysis(
    val userId: String,
    val insights: List<Map<String, Any>> = emptyList(), // InsightItem을 간단한 Map으로
    val translatedReviews: List<String> = emptyList(),
    val menuSentiments: List<Map<String, Any>> = emptyList(), // MenuSentimentDto를 Map으로
    val countryRatings: List<Map<String, Any>> = emptyList(), // CountryRatingDto를 Map으로
    val lastUpdated: Long, // timestamp
    val reviewsHash: String // 리뷰 데이터 변경 감지용
)

class ReviewAnalysisCache(private val context: Context) {
    
    private val gson = Gson()
    
    companion object {
        private val CACHED_ANALYSIS_KEY = stringPreferencesKey("cached_analysis")
        private val CACHE_EXPIRY_HOURS = 24L // 24시간 후 만료
        
        // 국가별 메뉴 분석 캐시 키 생성
        private fun getMenuAnalysisCacheKey(userId: String, nationality: String?) =
            stringPreferencesKey("menu_analysis_${userId}_${nationality ?: "all"}")
        
        // 국가별 평점 분석 캐시 키 생성  
        private fun getCountryRatingsCacheKey(userId: String, dateRange: String) =
            stringPreferencesKey("country_ratings_${userId}_${dateRange}")
    }
    
    suspend fun saveAnalysis(
        userId: String,
        insights: List<Map<String, Any>>,
        translatedReviews: List<String>,
        menuSentiments: List<Map<String, Any>> = emptyList(),
        countryRatings: List<Map<String, Any>> = emptyList(),
        reviewsHash: String
    ) {
        val cachedData = CachedReviewAnalysis(
            userId = userId,
            insights = insights,
            translatedReviews = translatedReviews,
            menuSentiments = menuSentiments,
            countryRatings = countryRatings,
            lastUpdated = System.currentTimeMillis(),
            reviewsHash = reviewsHash
        )
        
        context.reviewAnalysisCacheDataStore.edit { preferences ->
            preferences[CACHED_ANALYSIS_KEY] = gson.toJson(cachedData)
        }
    }
    
    fun getCachedAnalysis(userId: String): Flow<CachedReviewAnalysis?> {
        return context.reviewAnalysisCacheDataStore.data.map { preferences ->
            preferences[CACHED_ANALYSIS_KEY]?.let { jsonString ->
                try {
                    val cached = gson.fromJson(jsonString, CachedReviewAnalysis::class.java)
                    if (cached.userId == userId && !isCacheExpired(cached.lastUpdated)) {
                        cached
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
    
    private fun isCacheExpired(lastUpdated: Long): Boolean {
        val expiryTime = lastUpdated + (CACHE_EXPIRY_HOURS * 60 * 60 * 1000)
        return System.currentTimeMillis() > expiryTime
    }
    
    suspend fun clearCache() {
        context.reviewAnalysisCacheDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    // 리뷰 데이터의 해시값 생성 (변경 감지용)
    fun generateReviewsHash(reviews: List<String>): String {
        return reviews.joinToString("|").hashCode().toString()
    }
    
    // 국가별 메뉴 분석 캐시 저장
    suspend fun saveMenuAnalysis(
        userId: String,
        nationality: String?,
        menuSentiments: List<Map<String, Any>>,
        lastUpdated: Long = System.currentTimeMillis()
    ) {
        val cacheKey = getMenuAnalysisCacheKey(userId, nationality)
        val cacheData = mapOf(
            "menuSentiments" to menuSentiments,
            "lastUpdated" to lastUpdated
        )
        
        context.reviewAnalysisCacheDataStore.edit { preferences ->
            preferences[cacheKey] = gson.toJson(cacheData)
        }
    }
    
    // 국가별 메뉴 분석 캐시 불러오기
    fun getMenuAnalysisCache(userId: String, nationality: String?): Flow<List<Map<String, Any>>> {
        val cacheKey = getMenuAnalysisCacheKey(userId, nationality)
        return context.reviewAnalysisCacheDataStore.data.map { preferences ->
            preferences[cacheKey]?.let { jsonString ->
                try {
                    val cacheData = gson.fromJson(jsonString, Map::class.java) as Map<String, Any>
                    val lastUpdated = (cacheData["lastUpdated"] as? Number)?.toLong() ?: 0L
                    
                    if (!isCacheExpired(lastUpdated)) {
                        (cacheData["menuSentiments"] as? List<Map<String, Any>>) ?: emptyList()
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList() // 이 부분이 빠져있었음!
        }
    }
    
    // 국가별 평점 분석 캐시 저장
    suspend fun saveCountryRatings(
        userId: String,
        dateRange: String,
        countryRatings: List<Map<String, Any>>,
        lastUpdated: Long = System.currentTimeMillis()
    ) {
        val cacheKey = getCountryRatingsCacheKey(userId, dateRange)
        val cacheData = mapOf(
            "countryRatings" to countryRatings,
            "lastUpdated" to lastUpdated
        )
        
        context.reviewAnalysisCacheDataStore.edit { preferences ->
            preferences[cacheKey] = gson.toJson(cacheData)
        }
    }
    
    // 국가별 평점 분석 캐시 불러오기
    fun getCountryRatingsCache(userId: String, dateRange: String): Flow<List<Map<String, Any>>> {
        val cacheKey = getCountryRatingsCacheKey(userId, dateRange)
        return context.reviewAnalysisCacheDataStore.data.map { preferences ->
            preferences[cacheKey]?.let { jsonString ->
                try {
                    val cacheData = gson.fromJson(jsonString, Map::class.java) as Map<String, Any>
                    val lastUpdated = (cacheData["lastUpdated"] as? Number)?.toLong() ?: 0L
                    
                    if (!isCacheExpired(lastUpdated)) {
                        (cacheData["countryRatings"] as? List<Map<String, Any>>) ?: emptyList()
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }
    }
}