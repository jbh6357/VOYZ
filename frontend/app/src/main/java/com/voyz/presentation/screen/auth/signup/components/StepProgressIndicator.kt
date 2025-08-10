package com.voyz.presentation.screen.auth.signup.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.ui.theme.KoreanRed

@Composable
fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = (currentStep + 1).toFloat() / totalSteps.toFloat(),
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )
    
    val stepTitles = listOf("계정 정보", "개인 정보", "매장 정보")
    
    Column(modifier = modifier) {
        // 프로그레스 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val strokeWidth = size.height
                
                // 배경 프로그레스 바
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                
                // 진행 프로그레스 바
                drawLine(
                    color = KoreanRed,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width * progress, size.height / 2),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 단계 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { step ->
                val isActive = step <= currentStep
                val isCompleted = step < currentStep
                
                val textColor by animateColorAsState(
                    targetValue = when {
                        step == currentStep -> KoreanRed
                        isCompleted -> KoreanRed
                        else -> Color.Gray
                    },
                    animationSpec = tween(300),
                    label = "textColor"
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 단계 번호
                    Box(
                        modifier = Modifier
                            .size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawCircle(
                                color = if (isActive) KoreanRed else Color.Gray.copy(alpha = 0.3f),
                                radius = size.minDimension / 2
                            )
                            
                            if (!isCompleted) {
                                drawCircle(
                                    color = Color.White,
                                    radius = size.minDimension / 2 - 2.dp.toPx()
                                )
                            }
                        }
                        
                        Text(
                            text = if (isCompleted) "✓" else "${step + 1}",
                            color = if (isActive) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 단계 제목
                    Text(
                        text = stepTitles[step],
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (step == currentStep) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}