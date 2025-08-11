package com.voyz.presentation.screen.management.operation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.presentation.screen.management.operation.MenuRepository
import com.voyz.datas.model.dto.MenuItemDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MenuManagementCard(
    userId: String?,
    onMenuUpload: () -> Unit = {},
    onMenuInput: () -> Unit = {}
) {
    var menuItems by remember { mutableStateOf<List<MenuItemDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("전체") }
    var itemToDelete by remember { mutableStateOf<MenuItemDto?>(null) }
    var itemToEdit by remember { mutableStateOf<MenuItemDto?>(null) }
    var showAllMenus by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val menuRepository = remember { MenuRepository() }
    val scope = rememberCoroutineScope()

    // 메뉴 데이터 로드
    fun loadMenuItems() {
        val id = userId ?: return
        scope.launch {
            isLoading = true
            try {
                val result = menuRepository.getMenusByUserId(id)
                if (result.isSuccess) {
                    val allMenus = result.getOrNull() ?: emptyList()
                    
                    // 중복 제거 및 유효한 메뉴만 필터링
                    menuItems = allMenus
                        .filter { it.menuName != null && it.menuName.isNotBlank() }
                        .groupBy { it.menuName }
                        .mapValues { it.value.maxByOrNull { menu -> menu.menuIdx ?: 0 } }
                        .values
                        .filterNotNull()
                        .sortedBy { it.menuName }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // 초기 로드
    LaunchedEffect(userId) {
        if (userId != null) {
            loadMenuItems()
        }
    }

    // 카테고리별 필터링
    val categories = remember(menuItems) {
        val cats = menuItems
            .mapNotNull { it.category }
            .filter { it.isNotBlank() }
            .distinct()
        if (cats.isEmpty()) listOf("전체") else listOf("전체") + cats
    }

    val filteredMenus = remember(menuItems, selectedCategory) {
        if (selectedCategory == "전체") {
            menuItems
        } else {
            menuItems.filter { it.category == selectedCategory }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 카테고리 필터와 액션 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 카테고리 필터 (카메라 버튼 전까지의 공간 사용)
                if (categories.size > 1) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.take(4).forEach { category ->
                            val isSelected = category == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) Color(0xFFCD212A) else Color(0xFFF2F2F7)
                                    )
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Color(0xFF1D1D1F),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                }
                
                // 액션 버튼들
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // OCR 업로드 버튼
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = Color(0xFFF2F2F7),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onMenuUpload() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "메뉴 촬영",
                            tint = Color(0xFF1D1D1F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // 직접 입력 버튼
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = Color(0xFFCD212A),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onMenuInput() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "메뉴 추가",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFCD212A),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else if (filteredMenus.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "등록된 메뉴가 없습니다",
                            fontSize = 14.sp,
                            color = Color(0xFF8E8E93),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "우측 상단의 버튼으로 메뉴를 등록해보세요",
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // 메뉴 리스트
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val menusToShow = if (showAllMenus) filteredMenus else filteredMenus.take(4)
                    
                    menusToShow.forEach { menu ->
                        MenuItemRow(
                            item = menu,
                            onEditClick = { itemToEdit = it },
                            onDeleteClick = { itemToDelete = it }
                        )
                    }
                    
                    if (filteredMenus.size > 4 && !showAllMenus) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                onClick = { showAllMenus = true }
                            ) {
                                Text(
                                    text = "전체 ${filteredMenus.size}개 메뉴 보기",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCD212A)
                                )
                            }
                        }
                    }
                    
                    if (showAllMenus && filteredMenus.size > 4) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                onClick = { showAllMenus = false }
                            ) {
                                Text(
                                    text = "접기",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 삭제 확인 다이얼로그
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { 
                Text(
                    text = "메뉴 삭제",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ) 
            },
            text = { 
                Text(
                    text = "'${item.menuName}'을(를) 삭제하시겠습니까?",
                    fontSize = 14.sp
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            item.menuIdx?.let { menuIdx ->
                                try {
                                    val result = menuRepository.deleteMenu(menuIdx)
                                    if (result.isSuccess) {
                                        menuItems = menuItems.filterNot { it.menuIdx == item.menuIdx }
                                        android.widget.Toast.makeText(context, "메뉴가 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "메뉴 삭제에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "오류가 발생했습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                itemToDelete = null
                            }
                        }
                    }
                ) {
                    Text(
                        text = "삭제",
                        color = Color(0xFFCD212A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(
                        text = "취소",
                        color = Color(0xFF8E8E93)
                    )
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
                scope.launch {
                    try {
                        // TODO: 백엔드 UPDATE API 호출
                        // val result = menuRepository.updateMenu(updatedItem)
                        
                        // 임시로 로컬에서만 수정
                        menuItems = menuItems.map {
                            if (it.menuIdx == updatedItem.menuIdx) updatedItem else it
                        }
                        android.widget.Toast.makeText(context, "메뉴가 수정되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "메뉴 수정에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    itemToEdit = null
                }
            }
        )
    }
}

@Composable
private fun MenuItemRow(
    item: MenuItemDto,
    onEditClick: (MenuItemDto) -> Unit = {},
    onDeleteClick: (MenuItemDto) -> Unit = {}
) {
    var showDropdown by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFAFAFA),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.menuName ?: "메뉴명 없음",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1D1D1F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${String.format("%,d", item.menuPrice)}원",
                    fontSize = 12.sp,
                    color = Color(0xFF007AFF),
                    fontWeight = FontWeight.SemiBold
                )
                item.category?.let { category ->
                    if (category.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFE3F2FD),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 10.sp,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }
            }
        }
        
        // 더보기 버튼 (수정/삭제)
        Box {
            IconButton(
                onClick = { showDropdown = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "메뉴 옵션",
                    tint = Color(0xFF8E8E93),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier
                    .width(120.dp) // 30% 크기 축소
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "수정",
                                fontSize = 14.sp,
                                color = Color(0xFF1D1D1F)
                            )
                        }
                    },
                    onClick = {
                        showDropdown = false
                        onEditClick(item)
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFCD212A),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "삭제",
                                fontSize = 14.sp,
                                color = Color(0xFFCD212A)
                            )
                        }
                    },
                    onClick = {
                        showDropdown = false
                        onDeleteClick(item)
                    }
                )
            }
        }
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
        title = { 
            Text(
                text = "메뉴 수정",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            ) 
        },
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
                Text(
                    text = "저장",
                    color = Color(0xFFCD212A),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "취소",
                    color = Color(0xFF8E8E93)
                )
            }
        }
    )
}