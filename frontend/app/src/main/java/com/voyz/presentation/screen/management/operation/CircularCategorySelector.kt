package com.voyz.presentation.screen.management.operation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

data class CategoryItem(
    val id: String,
    val name: String,
    val color: Color = Color.Gray
)

@Composable
fun CircularCategorySelector(
    categories: List<CategoryItem>,
    selectedCategoryId: String,
    onCategorySelected: (CategoryItem) -> Unit,
    modifier: Modifier = Modifier,
    radius: Float = 120f
) {
    var rotation by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = if (isDragging) {
            spring(dampingRatio = 1f, stiffness = Spring.StiffnessHigh)
        } else {
            spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)
        },
        label = "rotation"
    )
    
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .size(280.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { 
                        isDragging = false
                        // 가장 가까운 카테고리로 스냅
                        val snapAngle = 360f / categories.size
                        val currentAngle = rotation % 360f
                        val targetIndex = (currentAngle / snapAngle).roundToInt()
                        rotation = targetIndex * snapAngle
                        
                        // 선택된 카테고리 업데이트
                        val selectedIndex = ((360f - rotation) / snapAngle).toInt() % categories.size
                        val correctedIndex = if (selectedIndex < 0) selectedIndex + categories.size else selectedIndex
                        onCategorySelected(categories[correctedIndex])
                    }
                ) { _, dragAmount ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dragVector = Offset(dragAmount.x, dragAmount.y)
                    val angle = atan2(dragVector.y, dragVector.x) * 180 / PI
                    rotation += (angle * 0.1f).toFloat()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircularCategories(
                categories = categories,
                rotation = animatedRotation,
                selectedCategoryId = selectedCategoryId,
                radius = radius,
                textMeasurer = textMeasurer,
                density = density
            )
        }
        
        // 중앙 선택 표시기
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

private fun DrawScope.drawCircularCategories(
    categories: List<CategoryItem>,
    rotation: Float,
    selectedCategoryId: String,
    radius: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: androidx.compose.ui.unit.Density
) {
    if (categories.isEmpty()) return
    
    val center = size.center
    val angleStep = 360f / categories.size
    
    categories.forEachIndexed { index, category ->
        try {
            val angle = (index * angleStep + rotation) * (PI / 180)
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            
            // 화면 경계 체크
            if (x < 0 || x > size.width || y < 0 || y > size.height) return@forEachIndexed
            
            val isSelected = category.id == selectedCategoryId
            val circleRadius = with(density) { 
                if (isSelected) 24.dp.toPx() else 20.dp.toPx() 
            }
            val textColor = if (isSelected) Color.White else Color.Black
            val backgroundColor = if (isSelected) Color(0xFF2196F3) else Color.LightGray
            
            // 카테고리 원 그리기
            drawCircle(
                color = backgroundColor,
                radius = circleRadius,
                center = Offset(x, y)
            )
            
            // 텍스트 그리기 - 안전하게 처리
            try {
                val textStyle = TextStyle(
                    fontSize = with(density) { if (isSelected) 12.sp else 10.sp },
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )
                
                val measuredText = textMeasurer.measure(category.name, textStyle)
                val textWidth = measuredText.size.width.toFloat()
                val textHeight = measuredText.size.height.toFloat()
                
                val textOffset = Offset(
                    (x - textWidth / 2f).coerceAtLeast(0f).coerceAtMost(size.width - textWidth),
                    (y - textHeight / 2f).coerceAtLeast(0f).coerceAtMost(size.height - textHeight)
                )
                
                drawText(
                    textLayoutResult = measuredText,
                    topLeft = textOffset
                )
            } catch (e: Exception) {
                // 텍스트 그리기 실패 시 무시
            }
        } catch (e: Exception) {
            // 개별 카테고리 그리기 실패 시 무시하고 계속
        }
    }
}