package com.voyz.datas.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.voyz.datas.model.MarketingOpportunity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.calendarDataStore by preferencesDataStore(name = "calendar_cache")

class CalendarDataStore(private val context: Context) {
    
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer<LocalDate> { json, _, _ ->
            LocalDate.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE)
        })
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ ->
            com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE))
        })
        .create()
    
    companion object {
        private fun getCacheKey(userId: String, month: String) = 
            stringPreferencesKey("calendar_${userId}_$month")
        
        private fun getOpportunityKey(opportunityId: String) = 
            stringPreferencesKey("opportunity_$opportunityId")
    }
    
    /**
     * 마케팅 기회 캐시 저장
     */
    suspend fun cacheOpportunities(
        userId: String,
        month: String, // "2025-07"
        opportunities: List<MarketingOpportunity>
    ) {
        context.calendarDataStore.edit { preferences ->
            val cacheKey = getCacheKey(userId, month)
            val json = gson.toJson(opportunities)
            preferences[cacheKey] = json
            
            // 개별 기회도 저장 (빠른 조회용)
            opportunities.forEach { opportunity ->
                val oppKey = getOpportunityKey(opportunity.id)
                preferences[oppKey] = gson.toJson(opportunity)
            }
        }
    }
    
    /**
     * 특정 기회 조회
     */
    suspend fun getOpportunityById(opportunityId: String): MarketingOpportunity? {
        return try {
            context.calendarDataStore.data.map { prefs ->
                val oppKey = getOpportunityKey(opportunityId)
                prefs[oppKey]?.let { json ->
                    try {
                        gson.fromJson(json, MarketingOpportunity::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }.first()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 월별 기회 조회
     */
    fun getOpportunitiesByMonth(userId: String, month: String): Flow<List<MarketingOpportunity>?> {
        return context.calendarDataStore.data.map { preferences ->
            val cacheKey = getCacheKey(userId, month)
            preferences[cacheKey]?.let { json ->
                try {
                    val type = object : TypeToken<List<MarketingOpportunity>>() {}.type
                    gson.fromJson<List<MarketingOpportunity>>(json, type)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
    
    /**
     * 캐시 삭제
     */
    suspend fun clearCache(userId: String, month: String) {
        context.calendarDataStore.edit { preferences ->
            val cacheKey = getCacheKey(userId, month)
            preferences.remove(cacheKey)
        }
    }
} 