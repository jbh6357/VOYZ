package com.voyz.presentation.fragment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voyz.presentation.component.calendar.CalendarComponent
import com.voyz.presentation.component.fab.FloatingActionMenu
import com.voyz.presentation.component.sidebar.SidebarComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var isSidebarOpen by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 메인 콘텐츠
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "VOYZ",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
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
                    )
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
                CalendarComponent(
                )
            }
        }
        
        // 사이드바 (오버레이)
        SidebarComponent(
            isOpen = isSidebarOpen,
            onClose = { isSidebarOpen = false }
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun MainScreenPreview() {
    MainScreen()
}