package com.voyz.presentation.fragment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCreateScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var priority by remember { mutableStateOf("보통") }
    var isRepeating by remember { mutableStateOf(false) }
    
    val priorityOptions = listOf("낮음", "보통", "높음", "긴급")
    
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
            
            // 리마인더 설명
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("상세 설명") },
                placeholder = { Text("추가 정보나 메모를 입력하세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )
            
            // 날짜 및 시간 선택
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 날짜 선택
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    onValueChange = { },
                    label = { Text("날짜") },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "날짜 선택"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                
                // 시간 선택
                OutlinedTextField(
                    value = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = { },
                    label = { Text("시간") },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "시간 선택"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 우선순위 선택
            var expandedPriority by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expandedPriority,
                onExpandedChange = { expandedPriority = !expandedPriority }
            ) {
                OutlinedTextField(
                    value = priority,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("우선순위") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedPriority,
                    onDismissRequest = { expandedPriority = false }
                ) {
                    priorityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                priority = option
                                expandedPriority = false
                            }
                        )
                    }
                }
            }
            
            // 반복 설정
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "반복 알림",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "매일 같은 시간에 알림",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isRepeating,
                    onCheckedChange = { isRepeating = it }
                )
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
                    onClick = { 
                        // TODO: 리마인더 생성 로직
                        navController.navigateUp()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = title.isNotBlank()
                ) {
                    Text("생성하기")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ReminderCreateScreenPreview() {
    val navController = rememberNavController()
    ReminderCreateScreen(navController = navController)
}