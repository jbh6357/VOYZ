package com.voyz.presentation.component.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReminderListBox(events: List<CalendarEvent>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        if (events.isEmpty()) {
            Text(
                text = "해당 날짜에 리마인더가 없습니다.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            events.forEach { event ->
                Text(
                    text = "• ${event.title}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
