package com.voyz.presentation.component.reminder

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.delay
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavController
import androidx.compose.animation.with
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReminderListBox(
    events: List<ReminderCalendarEvent>,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    viewModel: ReminderCalendarViewModel = viewModel(),
    onEventCheckChange: (ReminderCalendarEvent, Boolean) -> Unit,
    navController: NavController,

) {
    val orderMap = remember(events) {
        events.mapIndexed { index, event -> event.id to index }.toMap()
    }
    // ✅ 정렬 로직
    val sortedEvents = events.sortedWith(
        compareBy<ReminderCalendarEvent> { it.isChecked }
            .thenBy { if (it.isChecked) Int.MAX_VALUE else orderMap[it.id] ?: 0 }
    )
    val currentSelectedDate by rememberUpdatedState(selectedDate)
    var lastSwipeTime by remember { mutableStateOf(0L) }
    val debounceMillis = 200L

    var previousDate by remember { mutableStateOf(selectedDate) }
    val animationDirection = if (selectedDate > previousDate) 1 else -1
    LaunchedEffect(selectedDate) {
        previousDate = selectedDate
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    val now = System.currentTimeMillis()
                    if (now - lastSwipeTime < debounceMillis) return@detectHorizontalDragGestures

                    if (dragAmount > 30) {
                        onDateChange(currentSelectedDate.minusDays(1))
                        lastSwipeTime = now
                    } else if (dragAmount < -30) {
                        onDateChange(currentSelectedDate.plusDays(1))
                        lastSwipeTime = now
                    }
                }
            }
    ) {
        AnimatedContent(
            targetState = selectedDate,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it * animationDirection }
                ) with slideOutHorizontally(
                    targetOffsetX = { -it * animationDirection }
                )
            },
            label = "reminder_slide"
        ) { date ->
            val dayEvents = if (date == selectedDate) events else emptyList() // 안정성
            if (events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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

                    sortedEvents.forEach { event ->
                        // ✅ 이게 중요: key는 이렇게 wrapping해야 함
                        key(event.id) {
                            ReminderItemCard(
                                title = event.title,
                                isChecked = event.isChecked,
                                onCheckChange = { newChecked ->
                                    // 바로 UI에 반영하지 않음
                                },
                                onDelayedCheckCommit = { finalChecked ->
                                    viewModel.updateEventCheckStatus(event.id, finalChecked)
                                    onEventCheckChange(event, finalChecked)
                                },
                                onClick = {
                                    navController.navigate("reminder_detail/${event.id}") // 🔥 상세화면 이동
                                }
                            )
                        }

                    }
                }
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
        @Composable
        fun ReminderItemCard(
            title: String,
            isChecked: Boolean,
            onCheckChange: (Boolean) -> Unit,
            onDelayedCheckCommit: (Boolean) -> Unit,
            onClick: () -> Unit,
            allDay: Boolean = false

        ) {
            val backgroundColor = Color(0xFFBBDEFB)
            var localChecked by remember { mutableStateOf(isChecked) }

            LaunchedEffect(isChecked) {
                if (localChecked != isChecked) {
                    localChecked = isChecked
                }
            }
            val textColor = if (localChecked) Color(0xFF757575) else Gray900

            val offsetY by animateDpAsState(
                targetValue = 0.dp, // 항상 0 (이동 느낌만 살릴 수도 있지만 생략 가능)
                animationSpec = tween(300)
            )

            LaunchedEffect(localChecked) {
                delay(300)
                onDelayedCheckCommit(localChecked)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .offset(y = offsetY)
                    .clickable { onClick() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleCheckbox(
                        checked = localChecked,
                        onCheckChange = {
                            localChecked = it
                            onCheckChange(it)
                        },
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
                            textDecoration = if (localChecked) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(
                            text = "하루종일",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textDecoration = if (localChecked) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                }
            }
        }


