package com.voyz.presentation.component.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.ui.theme.Blue500
import com.voyz.ui.theme.Gray900
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voyz.presentation.component.reminder.ReminderCalendarEvent


@Composable
fun ReminderListBox(
    events: List<ReminderCalendarEvent>,
    selectedDate: LocalDate,
    viewModel: ReminderCalendarViewModel = viewModel(),
    onEventCheckChange: (ReminderCalendarEvent, Boolean) -> Unit
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "해당 날짜에 리마인더가 없습니다.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.Start
        ) {
            val sortedEvents = events.sortedWith(
                compareBy<ReminderCalendarEvent> { it.isChecked }.thenBy { it.id }
            )

            sortedEvents.forEachIndexed { index, event ->
                ReminderItemCard(
                    title = event.title,
                    isChecked = event.isChecked,
                    onCheckChange = { checked ->
                        viewModel.updateEventCheckStatus(event, checked)
                        onEventCheckChange(event, checked)
                    }
                )
            }
        }
    }
}
        @Composable
        fun ReminderItemCard(
            title: String,
            isChecked: Boolean,
            onCheckChange: (Boolean) -> Unit,
            allDay: Boolean = false
        ) {
            val backgroundColor = Color(0xFFBBDEFB)
            val textColor = if (isChecked) Color(0xFF757575) else Gray900


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                )

            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleCheckbox(
                        checked = isChecked,
                        onCheckChange = onCheckChange,
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.Top) // 텍스트 기준으로 정렬 (중앙 아님)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(
                            text = "하루종일",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                }
            }
        }
        @Composable
        fun CircleCheckbox(
            checked: Boolean,
            onCheckChange: (Boolean) -> Unit,
            modifier: Modifier = Modifier,
            checkedColor: Color = Blue500,
            uncheckedColor: Color = Blue500,
            size: Dp = 20.dp
        ) {
            Box(
                modifier = modifier
                    .size(size)
                    .clickable { onCheckChange(!checked) },
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Checked",
                        tint = checkedColor,
                        modifier = Modifier.size(size * 1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape)
                            .border(
                                width = 1.5.dp,
                                color = uncheckedColor,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

