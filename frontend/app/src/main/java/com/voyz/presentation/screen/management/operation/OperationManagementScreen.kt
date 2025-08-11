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
import androidx.compose.foundation.lazy.LazyRow
import com.voyz.datas.model.dto.MenuItemDto
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.voyz.datas.datastore.UserPreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope




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
    var menuItems by remember { mutableStateOf<List<MenuItemDto>>(emptyList()) }
    var itemToDelete by remember { mutableStateOf<MenuItemDto?>(null) }
    var itemToEdit by remember { mutableStateOf<MenuItemDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val menuRepository = remember { MenuRepository() }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500) // 원하는 시간으로 조절 (ms)
        showContent = true
        
        // 메뉴 로드
        isLoading = true
        try {
            val userId = userPreferencesManager.userId.first()
            android.util.Log.d("MenuLoad", "로그인된 userId: $userId")
            
            if (userId != null) {
                val result = menuRepository.getMenusByUserId(userId)
                android.util.Log.d("MenuLoad", "API 결과: ${if (result.isSuccess) "성공" else "실패"}")
                
                if (result.isSuccess) {
                    val allMenus = result.getOrNull() ?: emptyList()
                    android.util.Log.d("MenuLoad", "전체 메뉴 개수: ${allMenus.size}")
                    
                    // 로그로 각 메뉴 확인
                    allMenus.forEachIndexed { index, menu ->
                        android.util.Log.d("MenuLoad", "메뉴 $index: name=${menu.menuName}, category=${menu.category}")
                    }
                    
                    // 중복 제거 전 필터링 로그
                    val validMenus = allMenus.filter { it.menuName != null && it.menuName.isNotBlank() }
                    android.util.Log.d("MenuLoad", "유효한 메뉴 개수: ${validMenus.size}")
                    
                    // 중복 메뉴 제거 (menuName 기준으로 최신것만 유지)
                    menuItems = validMenus
                        .groupBy { it.menuName }
                        .mapValues { it.value.maxByOrNull { menu -> menu.menuIdx ?: 0 } }
                        .values
                        .filterNotNull()
                        .sortedBy { it.menuName }
                        
                    android.util.Log.d("MenuLoad", "최종 메뉴 개수: ${menuItems.size}")
                } else {
                    android.util.Log.e("MenuLoad", "API 실패: ${result.exceptionOrNull()?.message}")
                }
            } else {
                android.util.Log.e("MenuLoad", "userId가 null입니다")
            }
        } catch (e: Exception) {
            // 에러 처리
            android.util.Log.e("MenuLoad", "메뉴 로드 실패", e)
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .offset(x = with(density) { animatedOffset.toDp() }),
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

                // ✅ 운영관리 메인 대시보드 + 탭 콘텐츠
                when (selectedTabIndex) {
                    0 -> {
                        OperationManagementRevenueScreen()
                    }

                    1 -> {
                        // 동적 카테고리 생성
                        val uniqueCategories = remember(menuItems) {
                            val categories = menuItems
                                .mapNotNull { it.category }
                                .filter { it.isNotBlank() }
                                .distinct()
                            if (categories.isEmpty()) {
                                listOf("전체")
                            } else {
                                listOf("전체") + categories
                            }
                        }
                        
                        var selectedCategory by remember(uniqueCategories) { 
                            mutableStateOf(uniqueCategories.firstOrNull() ?: "전체") 
                        }
                        
                        val filteredMenuItems = remember(menuItems, selectedCategory) {
                            if (selectedCategory == "전체") {
                                menuItems
                            } else {
                                menuItems.filter { it.category == selectedCategory }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // 수평 스크롤 카테고리 selector  
                            if (uniqueCategories.size > 1) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    items(uniqueCategories) { category ->
                                        val isSelected = category == selectedCategory
                                        val safeCategory = category ?: "전체"
                                        FilterChip(
                                            onClick = { selectedCategory = safeCategory },
                                            label = { Text(safeCategory) },
                                            selected = isSelected,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // ✅ 로딩 상태 표시
                            if (isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else if (filteredMenuItems.isEmpty()) {
                                // ✅ 메뉴가 없으면 EmptyState
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
                                        MenuDtoListItem(
                                            item = item,
                                            onEditRequest = { editItem ->
                                                itemToEdit = editItem
                                            },
                                            onDeleteRequest = { itemToDelete = it }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
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

        // 삭제 다이얼로그
        itemToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("메뉴 삭제") },
                text = { Text("이 메뉴를 삭제하시겠습니까?") },
                confirmButton = {
                    TextButton(onClick = {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            item.menuIdx?.let { menuIdx ->
                                android.util.Log.d("MenuDelete", "삭제 시도: menuIdx=$menuIdx")
                                val result = menuRepository.deleteMenu(menuIdx)
                                
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    if (result.isSuccess) {
                                        android.util.Log.d("MenuDelete", "삭제 성공")
                                        // DB 삭제 성공 후 로컬 목록에서도 제거
                                        menuItems = menuItems.filterNot { it.menuIdx == item.menuIdx }
                                        android.widget.Toast.makeText(context, "메뉴가 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.util.Log.e("MenuDelete", "삭제 실패: ${result.exceptionOrNull()?.message}")
                                        android.widget.Toast.makeText(context, "메뉴 삭제에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    itemToDelete = null
                                }
                            } ?: run {
                                android.widget.Toast.makeText(context, "메뉴 ID가 없습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                itemToDelete = null
                            }
                        }
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
        
        // 수정 다이얼로그
        itemToEdit?.let { item ->
            MenuEditDialog(
                item = item,
                onDismiss = { itemToEdit = null },
                onSave = { updatedItem ->
                    // TODO: 백엔드 UPDATE API 호출
                    // 임시로 로컬에서만 수정
                    menuItems = menuItems.map {
                        if (it.menuIdx == updatedItem.menuIdx) updatedItem else it
                    }
                    itemToEdit = null
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuEditDialog(
    item: MenuItemDto,
    onDismiss: () -> Unit,
    onSave: (MenuItemDto) -> Unit
) {
    var editedName by remember { mutableStateOf(item.menuName) }
    var editedPrice by remember { mutableStateOf(item.menuPrice.toString()) }
    var editedCategory by remember { mutableStateOf(item.category ?: "음식") }
    var editedDescription by remember { mutableStateOf(item.menuDescription ?: "") }
    
    val categories = listOf("음식", "주류", "디저트", "음료", "사이드", "세트메뉴", "시그니처")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("메뉴 수정") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("메뉴명") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = editedPrice,
                    onValueChange = { editedPrice = it.filter { ch -> ch.isDigit() } },
                    label = { Text("가격") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 카테고리 드롭다운
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = editedCategory,
                        onValueChange = {},
                        label = { Text("카테고리") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    editedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = editedDescription,
                    onValueChange = { editedDescription = it },
                    label = { Text("설명 (선택사항)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val priceInt = editedPrice.toIntOrNull() ?: 0
                    val updatedItem = item.copy(
                        menuName = editedName,
                        menuPrice = priceInt,
                        category = editedCategory,
                        menuDescription = editedDescription.takeIf { it.isNotBlank() }
                    )
                    onSave(updatedItem)
                },
                enabled = editedName.isNotBlank() && editedPrice.isNotBlank()
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun MenuDtoListItem(
    item: MenuItemDto,
    onEditRequest: (MenuItemDto) -> Unit,
    onDeleteRequest: (MenuItemDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.menuName ?: "메뉴명 없음",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${String.format("%,d", item.menuPrice)}원",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val categoryText = item.category ?: "분류 없음"
                if (categoryText.isNotBlank()) {
                    Text(
                        text = categoryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = { onEditRequest(item) }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "수정",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = { onDeleteRequest(item) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
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