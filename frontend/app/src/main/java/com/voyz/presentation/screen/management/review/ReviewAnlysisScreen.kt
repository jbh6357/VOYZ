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
import com.voyz.presentation.screen.management.operation.PeriodSelectionDialog
import com.voyz.presentation.screen.management.operation.formatForDisplay
import java.time.LocalDate

@Composable
fun ReviewAnalysisScreen() {
    val today = LocalDate.now()
    val defaultStart = today.minusMonths(1).withDayOfMonth(1)
    val defaultEnd = today
    val defaultPeriod = "${defaultStart.year} ${defaultStart.monthValue}월 ~ ${defaultEnd.year} ${defaultEnd.monthValue}월"

    var topDialogOpen by remember { mutableStateOf(false) }
    var bottomDialogOpen by remember { mutableStateOf(false) }

    var topPeriodText by remember { mutableStateOf(defaultPeriod) }
    var bottomPeriodText by remember { mutableStateOf(defaultPeriod) }

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
                        modifier = Modifier.clickable { topDialogOpen = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(topPeriodText, fontSize = 14.sp, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                NationalityPieChart(
                    koreanCount = 80,
                    nationalityBreakdown = listOf(
                        "\uD83C\uDDFA\uD83C\uDDF8 미국" to 10,
                        "\uD83C\uDDEF\uD83C\uDDF5 일본" to 8,
                        "\uD83C\uDDE8\uD83C\uDDF3 중국" to 7,
                        "\uD83C\uDDEC\uD83C\uDDE7 영국" to 5,
                        "\uD83C\uDDE9\uD83C\uDDEA 독일" to 4
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(160.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                NationalityStatBox(
                    totalNations = 6,
                    koreanCount = 80,
                    foreignCount = 34,
                    nationalityBreakdown = listOf(
                        "\uD83C\uDDFA\uD83C\uDDF8 미국" to 10,
                        "\uD83C\uDDEF\uD83C\uDDF5 일본" to 8,
                        "\uD83C\uDDE8\uD83C\uDDF3 중국" to 7,
                        "\uD83C\uDDEC\uD83C\uDDE7 영국" to 5,
                        "\uD83C\uDDE9\uD83C\uDDEA 독일" to 4
                    )
                )
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
                        modifier = Modifier.clickable { bottomDialogOpen = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(bottomPeriodText, fontSize = 14.sp, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ReviewStatBox(
                    totalReviews = 124,
                    averageRating = 4.2f,
                    positiveReviews = 98,
                    negativeReviews = 26,
                    topPositiveKeywords = listOf("친절", "맛있음", "청결"),
                    topNegativeKeywords = listOf("늦음", "불친절", "비쌈")
                )
            }
        }
    }

    if (topDialogOpen) {
        PeriodSelectionDialog(
            onDismiss = { topDialogOpen = false },
            onPeriodSelected = {
                topPeriodText = formatForDisplay(it)
                topDialogOpen = false
            }
        )
    }

    if (bottomDialogOpen) {
        PeriodSelectionDialog(
            onDismiss = { bottomDialogOpen = false },
            onPeriodSelected = {
                bottomPeriodText = formatForDisplay(it)
                bottomDialogOpen = false
            }
        )
    }
}
