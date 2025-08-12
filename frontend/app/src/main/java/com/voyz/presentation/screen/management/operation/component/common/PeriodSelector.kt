package com.voyz.presentation.screen.management.operation.component.common

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PeriodSelector(
    startDate: LocalDate,
    endDate: LocalDate,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "분석 기간",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2C)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 시작일 선택
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            isSelectingStartDate = true
                            showDatePicker = true
                        },
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "시작일",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2C2C2C)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "날짜 선택",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // 종료일 선택
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            isSelectingStartDate = false
                            showDatePicker = true
                        },
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "종료일",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = endDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2C2C2C)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "날짜 선택",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // 빠른 선택 버튼들
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickOptions = listOf(
                    "최근 7일" to 7,
                    "최근 30일" to 30,
                    "최근 90일" to 90
                )
                
                quickOptions.forEach { (label, days) ->
                    OutlinedButton(
                        onClick = {
                            val newEndDate = LocalDate.now()
                            val newStartDate = newEndDate.minusDays(days.toLong())
                            onDateRangeSelected(newStartDate, newEndDate)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
    
    // DatePicker Dialog (간단한 구현)
    if (showDatePicker) {
        // 실제 프로젝트에서는 DatePickerDialog나 MaterialDatePicker 사용
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = {
                Text(if (isSelectingStartDate) "시작일 선택" else "종료일 선택")
            },
            text = {
                Text("날짜를 선택해주세요")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        // 여기서는 간단히 오늘 날짜로 설정
                        if (isSelectingStartDate) {
                            onDateRangeSelected(LocalDate.now().minusDays(7), endDate)
                        } else {
                            onDateRangeSelected(startDate, LocalDate.now())
                        }
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        )
    }
}