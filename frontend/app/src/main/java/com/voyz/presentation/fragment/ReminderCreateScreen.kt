package com.voyz.presentation.fragment

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.model.dto.ReminderDto
import com.voyz.datas.network.ApiClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCreateScreen(
    navController: NavController,
    initialTitle: String = "",
    initialContent: String = "",
    initialDate: String = "",
    modifier: Modifier = Modifier,
    onReminderCreated: (() -> Unit)? = null // 리마인더 생성 완료 콜백
) {
        // 백엔드 API에 맞는 필드들만 사용
    var title by remember { mutableStateOf(if (initialTitle.isNotEmpty()) java.net.URLDecoder.decode(initialTitle, "UTF-8") else "") }
    var content by remember { mutableStateOf(if (initialContent.isNotEmpty()) java.net.URLDecoder.decode(initialContent, "UTF-8") else "") }
    var startDate by remember { 
        mutableStateOf(
            if (initialDate.isNotEmpty()) {
                android.util.Log.d("ReminderCreate", "Using initialDate: $initialDate")
                try { 
                    val parsed = LocalDate.parse(initialDate)
                    android.util.Log.d("ReminderCreate", "Parsed date: $parsed")
                    // 2015년처럼 이상한 연도면 현재 날짜로 대체
                    if (parsed.year < 2020 || parsed.year > 2030) {
                        android.util.Log.w("ReminderCreate", "Invalid year ${parsed.year}, using current date")
                        LocalDate.now()
                    } else {
                        parsed
                    }
                } catch (e: Exception) { 
                    android.util.Log.e("ReminderCreate", "Failed to parse initialDate: $initialDate", e)
                    LocalDate.now()
                }
            } else {
                android.util.Log.d("ReminderCreate", "Using LocalDate.now(): ${LocalDate.now()}")
                LocalDate.now()
            }
        )
    }
    var endDate by remember { mutableStateOf(startDate) }
    
    // 오늘 날짜로 리셋하는 함수
    fun resetToToday() {
        startDate = LocalDate.now()
        endDate = LocalDate.now()  
        android.util.Log.d("ReminderCreate", "Reset to today: ${LocalDate.now()}")
    }
    
    // UI 상태 관리
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // 컨텍스트와 Coroutine Scope
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    
    // 리마인더 생성 함수
    fun createReminder() {
        coroutineScope.launch {
            try {
                isLoading = true
                errorMessage = null
                
                // 유효성 검사
                if (title.isBlank()) {
                    errorMessage = "제목을 입력해주세요."
                    return@launch
                }
                
                if (content.isBlank()) {
                    errorMessage = "내용을 입력해주세요."
                    return@launch
                }
                
                if (endDate.isBefore(startDate)) {
                    errorMessage = "종료 날짜는 시작 날짜보다 늦어야 합니다."
                    return@launch
                }
                
                // 사용자 ID 가져오기
                val userId = userPreferencesManager.userId.first()
                if (userId.isNullOrBlank()) {
                    errorMessage = "로그인이 필요합니다."
                    return@launch
                }
                
                // API 호출
                val reminderDto = ReminderDto(
                    title = title.trim(),
                    content = content.trim(),
                    startDate = startDate,
                    endDate = endDate
                )
                
                android.util.Log.d("ReminderCreate", "=== Creating Reminder ===")
                android.util.Log.d("ReminderCreate", "title: ${reminderDto.title}")
                android.util.Log.d("ReminderCreate", "content: ${reminderDto.content}")
                android.util.Log.d("ReminderCreate", "startDate: ${reminderDto.startDate}")
                android.util.Log.d("ReminderCreate", "endDate: ${reminderDto.endDate}")
                android.util.Log.d("ReminderCreate", "userId: $userId")
                
                val response = ApiClient.calendarApiService.createReminder(reminderDto, userId)
                android.util.Log.d("ReminderCreate", "API Response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    // 성공시 캘린더 캐시 무효화 (새로고침)
                    try {
                        val calendarDataStore = com.voyz.datas.datastore.CalendarDataStore(context)
                        val currentDate = java.time.LocalDate.now()
                        val monthKey = "${currentDate.year}-${String.format("%02d", currentDate.monthValue)}"
                        calendarDataStore.clearCache(userId, monthKey)
                    } catch (e: Exception) {
                        android.util.Log.e("ReminderCreateScreen", "Failed to clear cache", e)
                    }
                    onReminderCreated?.invoke()
                    navController.navigateUp()
                } else {
                    errorMessage = "리마인더 생성에 실패했습니다. (${response.code()})"
                }
                
            } catch (e: Exception) {
                errorMessage = "오류가 발생했습니다: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "리마인더 생성",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더 섹션
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "새 리마인더",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "중요한 일정을 놓치지 마세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            // 리마인더 제목
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("리마인더 제목") },
                placeholder = { Text("예: 회의 참석, 약속 등") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 리마인더 내용
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("리마인더 내용") },
                placeholder = { Text("리마인더 상세 내용을 입력하세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
            
            // 리마인더 기간 설정
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 시작 날짜
                OutlinedTextField(
                    value = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    onValueChange = { },
                    label = { Text("시작 날짜") },
                    readOnly = true,
                    leadingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "시작 날짜 선택"
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStartDatePicker = true }
                )
                
                // 종료 날짜
                OutlinedTextField(
                    value = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    onValueChange = { },
                    label = { Text("종료 날짜") },
                    readOnly = true,
                    leadingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "종료 날짜 선택"
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndDatePicker = true }
                )
            }
            
            // 우선순위 선택
            var expandedPriority by remember { mutableStateOf(false)             }
            
            // 날짜 범위 설정 도움말
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 시작 날짜와 종료 날짜를 동일하게 설정하면 하루 리마인더가 됩니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = { resetToToday() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("📅 오늘 날짜로 설정")
                    }
                }
            }
            
            // 에러 메시지 표시
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 액션 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("취소")
                }
                
                Button(
                    onClick = { createReminder() },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && title.isNotBlank() && content.isNotBlank()
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Text("생성 중...")
                        }
                    } else {
                        Text("생성하기")
                    }
                }
            }
        }
    }
    
    // 시작 날짜 선택기
    if (showStartDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        // 종료 날짜가 시작 날짜보다 이르면 시작 날짜로 맞춤
                        if (endDate.isBefore(startDate)) {
                            endDate = startDate
                        }
                    }
                    showStartDatePicker = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
    
    // 종료 날짜 선택기
    if (showEndDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = endDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        // 종료 날짜는 시작 날짜보다 늦어야 함
                        if (selectedDate.isBefore(startDate)) {
                            endDate = startDate
                        } else {
                            endDate = selectedDate
                        }
                    }
                    showEndDatePicker = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ReminderCreateScreenPreview() {
    val navController = rememberNavController()
    ReminderCreateScreen(
        navController = navController,
        initialTitle = "샘플 리마인더",
        initialContent = "이것은 샘플 리마인더 내용입니다.",
        initialDate = "2024-01-15"
    )
}