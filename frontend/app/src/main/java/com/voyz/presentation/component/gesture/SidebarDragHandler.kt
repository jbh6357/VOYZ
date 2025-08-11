package com.voyz.presentation.component.gesture

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope

/**
 * 사이드바 드래그 제스처를 처리하는 Modifier
 */
@Composable
fun Modifier.sidebarDragGesture(
    isEnabled: Boolean,
    sidebarWidth: Float,
    currentDragOffset: Float,
    onDragOffsetChange: (Float) -> Unit,
    onSidebarOpen: () -> Unit
): Modifier {
    return if (isEnabled) {
        this.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { offset ->
                    // 왼쪽 가장자리에서만 드래그 시작 허용 (50px 이내)
                    if (offset.x > 50f) {
                        return@detectHorizontalDragGestures
                    }
                },
                onDragEnd = {
                    if (currentDragOffset > sidebarWidth * 0.3f) {
                        onSidebarOpen()
                    }
                    onDragOffsetChange(0f)
                }
            ) { _, dragAmount ->
                val newOffset = (currentDragOffset + dragAmount).coerceIn(0f, sidebarWidth)
                onDragOffsetChange(newOffset)
            }
        }
    } else {
        this
    }
}