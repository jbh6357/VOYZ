package com.voyz.presentation.component.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun ReminderDayInfoBox(
    selectedDate: LocalDate,
    weatherIcon: String,
    temperature: String,
    events: List<ReminderCalendarEvent>
) {
    val day = selectedDate.dayOfMonth         // 숫자 날짜
    val dayOfWeek =
        selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)  // "월요일" 형식

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 8.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ⬆ 날짜 숫자 (예: 28)
                Text(
                    text = "${selectedDate.dayOfMonth}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                // ⬇ 요일 (예: 월요일)
                val dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
                Text(
                    text = dayOfWeek,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            // ⛅ 날씨 & 온도
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = weatherIcon,
                    fontSize = 20.sp
                )
                Text(
                    text = temperature,
                    fontSize = 16.sp
                )
            }
        }
    }
}