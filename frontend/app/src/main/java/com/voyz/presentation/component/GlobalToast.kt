package com.voyz.presentation.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect

object GlobalToastManager {
    private var _currentToast by mutableStateOf<ToastData?>(null)
    val currentToast: ToastData? get() = _currentToast
    
    fun showToast(message: String, durationMs: Long = 3000) {
        _currentToast = ToastData(message, durationMs)
    }
    
    fun dismissToast() {
        _currentToast = null
    }
    
    fun showRegistrationCompleteToast() {
        showToast("회원가입이 완료되었습니다! 맞춤 제안을 준비하고 있습니다.", 3000)
    }
    
    fun showSuggestionsReadyToast() {
        showToast("맞춤 제안이 준비되었습니다!", 3000)
    }
}

data class ToastData(
    val message: String,
    val durationMs: Long
)

@Composable
fun GlobalToastHost() {
    val toastData = GlobalToastManager.currentToast
    
    // 자동 삭제 로직
    LaunchedEffect(toastData) {
        if (toastData != null) {
            delay(toastData.durationMs)
            GlobalToastManager.dismissToast()
        }
    }
    
    AnimatedVisibility(
        visible = toastData != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(400)
        )
    ) {
        if (toastData != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    com.voyz.ui.theme.AccentGreenLight,
                                    com.voyz.ui.theme.AccentGreen
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .align(Alignment.Center)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "성공",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = toastData.message,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}