package com.voyz.presentation.screen.management.operation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.voyz.presentation.component.fab.FloatingActionMenu
import com.voyz.presentation.component.sidebar.SidebarComponent
import com.voyz.presentation.component.topbar.CommonTopBar
import kotlinx.coroutines.delay
import java.time.LocalDate
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import com.voyz.presentation.fake.MenuItem
import com.voyz.presentation.fake.sampleMenuItems
import androidx.compose.foundation.lazy.items




@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationManagementScreen(
    navController: NavController,
    onSearchClick: () -> Unit = {},
    onAlarmClick: () -> Unit = {},
    onTodayClick: () -> Unit = {},
    today: LocalDate = LocalDate.now()
) {
    var isSidebarOpen by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val sidebarWidth = with(density) { 280.dp.toPx() }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isSidebarOpen) sidebarWidth else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "sidebar_offset"
    )
    var showContent by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("매출", "메뉴")
    var menuItems by remember { mutableStateOf(sampleMenuItems) }
    var itemToDelete by remember { mutableStateOf<MenuItem?>(null) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500) // 원하는 시간으로 조절 (ms)
        showContent = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            topBar = {
                CommonTopBar(
                    onMenuClick = { isSidebarOpen = true },
                    onSearchClick = onSearchClick,
                    onAlarmClick = onAlarmClick,
                    onTodayClick = onTodayClick,
                    today = today
                )
            }
        ) { innerPadding ->

            // ✅ Column 하나로 통일
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        )
                    }
                }

                // ✅ 탭 콘텐츠는 TabRow 아래로!
                when (selectedTabIndex) {
                    0 -> {
                        OperationManagementRevenueScreen()
                    }

                    1 -> {
                        var selectedCategory by remember { mutableStateOf("음식") }
                        val filteredMenuItems = menuItems.filter { it.category == selectedCategory }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // ✅ 음식/주류 버튼 항상 출력
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Row(modifier = Modifier.width(160.dp)) {
                                    Button(
                                        onClick = { selectedCategory = "음식" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedCategory == "음식") MaterialTheme.colorScheme.primary else Color.LightGray
                                        ),
                                        shape = RoundedCornerShape(
                                            topStart = 8.dp,
                                            bottomStart = 8.dp
                                        ),
                                        contentPadding = PaddingValues(horizontal = 0.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text("음식")
                                    }

                                    Button(
                                        onClick = { selectedCategory = "주류" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedCategory == "주류") MaterialTheme.colorScheme.primary else Color.LightGray
                                        ),
                                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                                        contentPadding = PaddingValues(horizontal = 0.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text("주류")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // ✅ 메뉴가 없으면 EmptyState
                            if (filteredMenuItems.isEmpty()) {
                                OperationMenuEmptyState(
                                    navController = navController,
                                    onUploadClick = {},
                                    onManualInputClick = {},
                                    startTyping = true
                                )
                            } else {
                                // ✅ 메뉴 리스트
                                LazyColumn(modifier = Modifier.weight(1f)) {
                                    items(filteredMenuItems) { item ->
                                        MenuListItem(
                                            item = item,
                                            onEditToggle = { toggleItem ->
                                                menuItems = menuItems.map {
                                                    if (it.id == toggleItem.id) it.copy(isEditing = !it.isEditing) else it
                                                }
                                            },
                                            onDeleteRequest = { itemToDelete = it },
                                            onEditConfirm = { updatedItem ->
                                                menuItems = menuItems.map {
                                                    if (it.id == updatedItem.id) updatedItem.copy(
                                                        isEditing = false,
                                                        name = updatedItem.name,
                                                        price = updatedItem.price
                                                    ) else it
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // ✅ FAB 고정
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                var isFabExpanded by remember { mutableStateOf(false) }

                                FloatingActionMenu(
                                    isExpanded = isFabExpanded,
                                    onExpandedChange = { isFabExpanded = it },
                                    isOperationMode = true,
                                    onMarketingCreateClick = {},
                                    onReminderCreateClick = {},
                                    onMenuUploadClick = {
                                        navController.navigate("operation_management_menu_upload")
                                    },
                                    onMenuDirectClick = {
                                        navController.navigate("operation_management_menu_input")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        itemToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("메뉴 삭제") },
                text = { Text("이 메뉴를 삭제하시겠습니까?") },
                confirmButton = {
                    TextButton(onClick = {
                        // 삭제 수행
                        menuItems = menuItems.filterNot { it.id == item.id }
                        itemToDelete = null
                    }) {
                        Text("삭제")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("취소")
                    }
                }
            )
        }




            if (isSidebarOpen || animatedOffset > 0f) {
                SidebarComponent(
                    isOpen = isSidebarOpen,
                    animatedOffset = animatedOffset + dragOffset,
                    onClose = {
                        isSidebarOpen = false
                        dragOffset = 0f
                    },
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    @Composable
    private fun OperationMenuEmptyState(
        navController: NavController,
        onUploadClick: () -> Unit,
        onManualInputClick: () -> Unit,
        startTyping: Boolean
    ) {
        var isFabExpanded by remember { mutableStateOf(false) }

        val firstLine = "등록된 메뉴가 없습니다"
        val secondLine = "우측 하단의 + 버튼을 눌러 메뉴를 등록해보세요"
        val fullText = "$firstLine\n\n$secondLine"
        val animatedText = fullText

        LaunchedEffect(Unit) {
            delay(500L)
            isFabExpanded = true
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 중앙 메시지
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = BiasAlignment(0f, -0.2f)
            ) {
                Text(
                    text = animatedText,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // FAB 메뉴
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            ) {
                FloatingActionMenu(
                    isExpanded = isFabExpanded,
                    onExpandedChange = { isFabExpanded = it },
                    isOperationMode = true,
                    onMarketingCreateClick = {},
                    onReminderCreateClick = {},
                    onMenuUploadClick = {
                        navController.navigate("operation_management_menu_upload")
                    },
                    onMenuDirectClick = {
                        navController.navigate("operation_management_menu_input")
                    }
                )
            }
        }
    }

@Composable
private fun MenuHeaderTyping(
    modifier: Modifier = Modifier,
    leadingInset: Dp = 26.dp,
    horizontalEdgeInset: Dp = 16.dp
) {
    val title = "메뉴"

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = leadingInset,
                    end = horizontalEdgeInset,
                    top = 12.dp,
                    bottom = 8.dp
                )
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = horizontalEdgeInset,
                    end = horizontalEdgeInset,
                    bottom = 8.dp
                ),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            thickness = 1.dp
        )
    }
}
@Composable
fun MenuListItem(
    item: MenuItem,
    onEditToggle: (MenuItem) -> Unit,
    onDeleteRequest: (MenuItem) -> Unit,
    onEditConfirm: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var editedName by remember { mutableStateOf(item.name) }
    var editedPrice by remember { mutableStateOf(item.price.toString()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (item.isEditing) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("메뉴명") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editedPrice,
                    onValueChange = { editedPrice = it },
                    label = { Text("가격") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = {
                        onEditConfirm(item.copy(name = editedName, price = editedPrice.toIntOrNull() ?: 0))
                    }) {
                        Text("저장")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { onEditToggle(item) }) {
                        Text("취소")
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text("${String.format("%,d", item.price)}원", color = Color.Gray)
                        Text(item.category, style = MaterialTheme.typography.bodySmall)
                    }

                    IconButton(onClick = { onEditToggle(item) }) {
                        Icon(Icons.Default.Edit, contentDescription = "수정")
                    }
                    IconButton(onClick = { onDeleteRequest(item) }) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제")
                    }
                }
            }
        }
    }
}