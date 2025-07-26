package com.voyz.presentation.fragment

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voyz.presentation.component.calendar.CalendarComponent
import com.voyz.presentation.component.fab.FloatingActionMenu
import com.voyz.presentation.component.sidebar.SidebarComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var isSidebarOpen by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val sidebarWidth = with(density) { 280.dp.toPx() }
    
    val animatedOffset by animateFloatAsState(
        targetValue = if (isSidebarOpen) sidebarWidth else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "sidebar_offset"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 메인 콘텐츠 (슬라이딩)
        Scaffold(
            modifier = Modifier
                .offset(x = with(density) { (animatedOffset + dragOffset).toDp() })
                .pointerInput(isSidebarOpen) {
                    if (!isSidebarOpen) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (dragOffset > sidebarWidth * 0.3f) {
                                    isSidebarOpen = true
                                }
                                dragOffset = 0f
                            }
                        ) { _, dragAmount ->
                            val newOffset = (dragOffset + dragAmount).coerceIn(0f, sidebarWidth)
                            dragOffset = newOffset
                        }
                    }
                },
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { isSidebarOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "메뉴 열기",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    windowInsets = WindowInsets(0)
                )
            },
            floatingActionButton = {
                FloatingActionMenu(
                    onTextPostClick = {
                        // TODO: 텍스트 게시글 작성 화면으로 이동
                    },
                    onPhotoPostClick = {
                        // TODO: 사진 게시글 작성 화면으로 이동
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CalendarComponent()
            }
        }
        
        // 사이드바 (최상단 오버레이)
        if (isSidebarOpen || animatedOffset > 0f) {
            SidebarComponent(
                isOpen = isSidebarOpen,
                animatedOffset = animatedOffset + dragOffset,
                onClose = { 
                    isSidebarOpen = false
                    dragOffset = 0f
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun MainScreenPreview() {
    MainScreen()
}