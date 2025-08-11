package com.voyz.presentation.screen.management.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.presentation.screen.management.review.component.ReviewStatBox
import com.voyz.presentation.screen.management.review.component.NationalityStatBox
import com.voyz.presentation.screen.management.review.component.NationalityPieChart
import com.voyz.presentation.screen.management.operation.formatForDisplay
import com.voyz.presentation.screen.management.common.DateRangePickerDialog
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.AnalyticsRepository
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@Composable
fun ReviewAnalysisScreen() {
    val today = LocalDate.now()
    val defaultStart = today.minusMonths(1).withDayOfMonth(1)
    val defaultEnd = today
    val defaultPeriod = "${defaultStart.year} ${defaultStart.monthValue}월 ~ ${defaultEnd.year} ${defaultEnd.monthValue}월"

    var topDialogOpen by remember { mutableStateOf(false) }
    var bottomDialogOpen by remember { mutableStateOf(false) }
    var dateRangeDialogTop by remember { mutableStateOf(false) }
    var dateRangeDialogBottom by remember { mutableStateOf(false) }

    var topPeriodText by remember { mutableStateOf(defaultPeriod) }
    var bottomPeriodText by remember { mutableStateOf(defaultPeriod) }
    var topSelectedMode by remember { mutableStateOf("월") } // 연도/월/주 표시 선택 추적


    // userId 로드
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val scope = rememberCoroutineScope()
    var userId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userPreferencesManager.userId.collect { fetched ->
            userId = fetched
        }
    }

    // 데이터 상태
    val analyticsRepository = remember { AnalyticsRepository() }
    var nationalityStatsYear by remember { mutableStateOf(emptyList<com.voyz.datas.model.dto.NationalityAnalyticsDto>()) }
    var nationalityStatsMonth by remember { mutableStateOf(emptyList<com.voyz.datas.model.dto.NationalityAnalyticsDto>()) }
    var nationalityStatsWeek by remember { mutableStateOf(emptyList<com.voyz.datas.model.dto.NationalityAnalyticsDto>()) }
    var nationalitySummaryMonth by remember { mutableStateOf<com.voyz.datas.model.dto.NationalitySummaryDto?>(null) }
    var reviewSummary by remember { mutableStateOf<com.voyz.datas.model.dto.ReviewSummaryDto?>(null) }

    // 초기 로드: 월 범위를 today 기준으로 계산하여 요약 호출
    LaunchedEffect(userId) {
        val id = userId ?: return@LaunchedEffect
        val start = defaultStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val end = defaultEnd.format(DateTimeFormatter.ISO_LOCAL_DATE)
        reviewSummary = analyticsRepository.getReviewSummary(id, start, end, 4, 2)
        // 월/연/주 통계는 기본값 사용: 월=현재월, 연=현재연도, 주=현재 주차(1~5 중 1 임시)
        runCatching {
            nationalityStatsMonth = analyticsRepository.getNationalityByMonth(id, today.monthValue)
            nationalitySummaryMonth = analyticsRepository.getNationalitySummaryByMonth(id, today.monthValue)
        }
        runCatching { nationalityStatsYear = analyticsRepository.getNationalityByYear(id, today.year) }
        runCatching { nationalityStatsWeek = analyticsRepository.getNationalityByWeek(id, 1) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 🔼 고객 통계 (순서 변경됨)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "고객 현황",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    Row(
                        modifier = Modifier.clickable { dateRangeDialogTop = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(topPeriodText, fontSize = 14.sp, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val foreignList = when (topSelectedMode) {
                    "연도" -> nationalityStatsYear
                    "주" -> nationalityStatsWeek
                    else -> nationalityStatsMonth
                }
                val breakdown = foreignList.map { (nation, cnt) ->
                    val flag = com.voyz.presentation.screen.management.review.util.NationalityFlagMapper.flagFor(nation)
                    val display = if (flag.isNotBlank()) "$flag $nation" else nation
                    display to cnt.toInt()
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 좌: 요약 통계 카드(표면 톤 낮춤)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 1.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val totalNations = breakdown.size
                            val foreignCount = nationalitySummaryMonth?.foreignCount?.toInt() ?: breakdown.sumOf { it.second }
                            val localCount = nationalitySummaryMonth?.localCount?.toInt() ?: 0
                            Text("고객 요약", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Spacer(Modifier.height(8.dp))
                            NationalityStatBox(
                                totalNations = totalNations,
                                koreanCount = localCount,
                                foreignCount = foreignCount,
                                nationalityBreakdown = breakdown
                            )
                        }
                    }
                    // 우: 도넛 차트 + 범례(표면 톤 낮춤)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 1.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            NationalityPieChart(
                                koreanCount = nationalitySummaryMonth?.localCount?.toInt() ?: 0,
                                nationalityBreakdown = breakdown,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            )
                            Spacer(Modifier.height(8.dp))
                            val labels = listOf("내국인", "기타 상위 국적")
                            val colors = listOf(Color(0xFF42A5F5), Color(0xFFEF5350))
                            com.voyz.presentation.screen.management.review.component.NationalityLegend(labels = labels, colors = colors, maxItems = 2)
                        }
                    }
                }
            }
        }

        // 🔽 리뷰 통계 (아래로 이동)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "리뷰 현황",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    Row(
                        modifier = Modifier.clickable { dateRangeDialogBottom = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(bottomPeriodText, fontSize = 14.sp, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val summary = reviewSummary
                var positiveKeywords by remember { mutableStateOf<List<String>>(emptyList()) }
                var negativeKeywords by remember { mutableStateOf<List<String>>(emptyList()) }

                // 키워드 분석 호출 (OpenAI 모드)
                LaunchedEffect(userId, bottomPeriodText) {
                    val id = userId ?: return@LaunchedEffect
                    // 현재 선택된 하단 기간 텍스트를 월 범위로 파싱, 실패 시 기본 범위
                    val (startDate, endDate) = run {
                        val sel = bottomPeriodText
                        if (sel.startsWith("월:")) {
                            val seg = sel.substringAfter("월:").trim().split("~")
                            val sp = seg.first().trim().split(" ")
                            val ep = seg.last().trim().split(" ")
                            val sy = sp[0].toInt(); val sm = sp[1].dropLast(1).toInt()
                            val ey = ep[0].toInt(); val em = ep[1].dropLast(1).toInt()
                            java.time.LocalDate.of(sy, sm, 1) to java.time.LocalDate.of(ey, em, 1)
                        } else defaultStart to defaultEnd
                    }
                    val start = startDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                    val end = endDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                    val raw = analyticsRepository.getReviewKeywords(id, start, end, 4, 2, 5, "openai")
                    runCatching {
                        if (raw.isNotBlank()) {
                            val root = org.json.JSONObject(raw)
                            val overall = if (root.has("overall")) root.getJSONObject("overall") else null
                            positiveKeywords = overall?.optJSONArray("positiveKeywords")?.let { arr ->
                                List(arr.length()) { i -> arr.optString(i) }.filter { it.isNotBlank() }
                            } ?: emptyList()
                            negativeKeywords = overall?.optJSONArray("negativeKeywords")?.let { arr ->
                                List(arr.length()) { i -> arr.optString(i) }.filter { it.isNotBlank() }
                            } ?: emptyList()
                        }
                    }
                }

                ReviewStatBox(
                    totalReviews = summary?.totalReviews?.toInt() ?: 0,
                    averageRating = summary?.averageRating?.toFloat() ?: 0f,
                    positiveReviews = summary?.positiveCount?.toInt() ?: 0,
                    negativeReviews = summary?.negativeCount?.toInt() ?: 0,
                    topPositiveKeywords = positiveKeywords,
                    topNegativeKeywords = negativeKeywords
                )
            }
        }
    }

    if (dateRangeDialogTop) {
        DateRangePickerDialog(
            visible = dateRangeDialogTop,
            onDismiss = { dateRangeDialogTop = false },
            onConfirm = { (start, end) ->
                dateRangeDialogTop = false
                topPeriodText = "월: ${start.year} ${start.monthValue}월 ~ ${end.year} ${end.monthValue}월"
                val id = userId ?: return@DateRangePickerDialog
                scope.launch {
                    runCatching {
                        nationalityStatsMonth = analyticsRepository.getNationalityByMonth(id, end.monthValue)
                        nationalitySummaryMonth = analyticsRepository.getNationalitySummaryByMonth(id, end.monthValue)
                        topSelectedMode = "월"
                    }
                }
            }
        )
    }

    if (dateRangeDialogBottom) {
        DateRangePickerDialog(
            visible = dateRangeDialogBottom,
            onDismiss = { dateRangeDialogBottom = false },
            onConfirm = { (start, end) ->
                dateRangeDialogBottom = false
                bottomPeriodText = "월: ${start.year} ${start.monthValue}월 ~ ${end.year} ${end.monthValue}월"
                val id = userId ?: return@DateRangePickerDialog
                scope.launch {
                    runCatching {
                        reviewSummary = analyticsRepository.getReviewSummary(
                            id,
                            start.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            end.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            4,
                            2,
                        )
                    }
                }
            }
        )
    }
}



