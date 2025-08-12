package com.voyz.presentation.service

import android.content.Context
import androidx.work.*
import com.voyz.presentation.worker.MenuAnalysisWorker
import com.voyz.presentation.worker.SmartRecommendationWorker
import com.voyz.presentation.worker.ReviewTranslationWorker
import com.voyz.datas.datastore.UserPreferencesManager
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.DayOfWeek
import java.util.concurrent.TimeUnit

class ScheduledAnalysisManager(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val MENU_ANALYSIS_WORK_NAME = "menu_analysis_daily"
        private const val SMART_RECOMMENDATION_WORK_NAME = "smart_recommendation_weekly"
        private const val REVIEW_TRANSLATION_WORK_NAME = "review_translation_login"
    }
    
    /**
     * 모든 정기 작업 스케줄링 설정
     */
    fun setupScheduledTasks() {
        setupDailyMenuAnalysis()
        setupWeeklySmartRecommendation()
        println("✅ 정기 작업 스케줄링 완료")
    }
    
    /**
     * 매일 아침 9시에 메뉴별 분석 업데이트
     */
    private fun setupDailyMenuAnalysis() {
        // 다음 아침 9시까지의 시간 계산
        val now = LocalDateTime.now()
        val nextRun = if (now.toLocalTime().isBefore(LocalTime.of(9, 0))) {
            now.toLocalDate().atTime(9, 0)
        } else {
            now.toLocalDate().plusDays(1).atTime(9, 0)
        }
        
        val initialDelay = Duration.between(now, nextRun).toMinutes()
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<MenuAnalysisWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            MENU_ANALYSIS_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )
        
        println("📅 매일 아침 9시 메뉴별 분석 스케줄링 설정 완료")
    }
    
    /**
     * 매주 월요일 오전 9시에 스마트 추천 업데이트
     */
    private fun setupWeeklySmartRecommendation() {
        // 다음 월요일 9시까지의 시간 계산
        val now = LocalDateTime.now()
        val nextMonday = now.toLocalDate()
            .with(DayOfWeek.MONDAY)
            .let { monday ->
                if (now.dayOfWeek == DayOfWeek.MONDAY && now.toLocalTime().isBefore(LocalTime.of(9, 0))) {
                    monday.atTime(9, 0)
                } else if (now.dayOfWeek == DayOfWeek.MONDAY) {
                    monday.plusWeeks(1).atTime(9, 0)
                } else if (now.dayOfWeek.value > DayOfWeek.MONDAY.value) {
                    monday.plusWeeks(1).atTime(9, 0)
                } else {
                    monday.atTime(9, 0)
                }
            }
        
        val initialDelay = Duration.between(now, nextMonday).toMinutes()
        
        val weeklyWorkRequest = PeriodicWorkRequestBuilder<SmartRecommendationWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SMART_RECOMMENDATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            weeklyWorkRequest
        )
        
        println("📅 매주 월요일 오전 9시 스마트 추천 스케줄링 설정 완료")
    }
    
    /**
     * 로그인 시 리뷰/번역 즉시 업데이트
     */
    fun updateReviewsOnLogin() {
        val loginWorkRequest = OneTimeWorkRequestBuilder<ReviewTranslationWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        workManager.enqueue(loginWorkRequest)
        println("📝 로그인 시 리뷰 번역 업데이트 실행")
        
        // 로그인 시 백그라운드 분석 서비스 실행 (인사이트 분석 포함)
        val backgroundService = BackgroundAnalysisService(context)
        val userPreferencesManager = UserPreferencesManager(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userPreferencesManager.userId.collect { userId ->
                    if (!userId.isNullOrEmpty()) {
                        backgroundService.preloadAnalysisData(userId)
                        return@collect
                    }
                }
            } catch (e: Exception) {
                println("❌ 로그인 시 백그라운드 분석 실패: ${e.message}")
            }
        }
        println("🔄 로그인 시 백그라운드 분석(인사이트 포함) 시작")
    }
    
    /**
     * 모든 작업 취소 (로그아웃 시 사용)
     */
    fun cancelAllTasks() {
        workManager.cancelUniqueWork(MENU_ANALYSIS_WORK_NAME)
        workManager.cancelUniqueWork(SMART_RECOMMENDATION_WORK_NAME)
        workManager.cancelUniqueWork(REVIEW_TRANSLATION_WORK_NAME)
        println("❌ 모든 정기 작업 취소 완료")
    }
    
    /**
     * 작업 상태 확인 (디버깅용)
     */
    fun checkWorkStatus() {
        workManager.getWorkInfosForUniqueWork(MENU_ANALYSIS_WORK_NAME)
        workManager.getWorkInfosForUniqueWork(SMART_RECOMMENDATION_WORK_NAME)
        println("🔍 작업 상태 확인 완료")
    }
}