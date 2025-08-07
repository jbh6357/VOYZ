package com.voyz.presentation.screen.management.operation

import StepProgressIndicator
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.LaunchedEffect
import com.voyz.datas.model.dto.MenuItemDto
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

// 메뉴 아이템 데이터 클래스
data class EditMenuItem(
    val id: String = java.util.UUID.randomUUID().toString(), // 고유 ID 추가
    var name: String,
    var price: String,
    var category: String,
    var isDuplicate: Boolean = false, // 중복 여부를 데이터에 포함
    var originalName: String = "" // 원본 이름 저장 (중복 체크용)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationManagementMenuConfirmScreen(
    navController: NavController,
    imageUri: Uri?,
    ocrResults: List<MenuItemDto> = emptyList()
) {
    val context = LocalContext.current
    val menuRepository = remember { MenuRepository() }
    val coroutineScope = rememberCoroutineScope()
    val userPreferencesManager = remember { com.voyz.datas.datastore.UserPreferencesManager(context) }
    
    // 상태 관리
    var isSaving by remember { mutableStateOf(false) }
    var existingMenus by remember { mutableStateOf<List<MenuItemDto>>(emptyList()) }
    var menuList by remember { mutableStateOf<List<EditMenuItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var focusedItemId by remember { mutableStateOf<String?>(null) }
    
    // 카테고리 목록
    val categories = listOf("메인", "사이드", "주류", "디저트", "음료", "세트메뉴", "시그니처")
    
    // 중복 체크 함수
    fun checkDuplicates(items: List<EditMenuItem>, existing: List<MenuItemDto>): List<EditMenuItem> {
        return items.map { item ->
            val isDuplicate = existing.any { existingMenu ->
                existingMenu.menuName.equals(item.name.trim(), ignoreCase = true) && 
                item.name.isNotBlank()
            }
            item.copy(
                isDuplicate = isDuplicate,
                originalName = if (item.originalName.isEmpty()) item.name else item.originalName
            )
        }
    }
    
    // 메뉴 리스트 정렬 함수
    fun sortMenuList(items: List<EditMenuItem>): List<EditMenuItem> {
        return items.sortedWith(compareBy(
            { !it.isDuplicate }, // 중복된 메뉴가 먼저 오도록
            { it.name.lowercase() } // 그 다음 이름순으로 정렬
        ))
    }
    
    // 초기 데이터 로드
    LaunchedEffect(Unit) {
        try {
            // 기존 메뉴 목록 로드
            val userId = userPreferencesManager.userId.first() ?: "unknown_user"
            val result = menuRepository.getMenusByUserId(userId)
            if (result.isSuccess) {
                existingMenus = result.getOrNull() ?: emptyList()
            }
            
            // OCR 결과를 EditMenuItem으로 변환
            val initialMenuList = if (ocrResults.isNotEmpty()) {
                ocrResults.map { ocrItem ->
                    EditMenuItem(
                        name = ocrItem.menuName,
                        price = ocrItem.menuPrice.toString(),
                        category = "메인" // 기본값
                    )
                }
            } else {
                // 테스트용 더미 데이터
                listOf(
                    EditMenuItem(name = "된장찌개", price = "7000", category = "메인"),
                    EditMenuItem(name = "김치찌개", price = "7500", category = "메인"),
                    EditMenuItem(name = "제육볶음", price = "8000", category = "메인")
                )
            }
            
            // 중복 체크 및 정렬
            menuList = sortMenuList(checkDuplicates(initialMenuList, existingMenus))
            isLoading = false
        } catch (e: Exception) {
            android.util.Log.e("MenuConfirm", "초기 데이터 로드 실패", e)
            isLoading = false
        }
    }
    
    // 메뉴 업데이트 함수
    fun updateMenuItem(id: String, updatedItem: EditMenuItem) {
        menuList = menuList.map { item ->
            if (item.id == id) {
                // 이름이 변경되었으면 중복 체크
                val newItem = if (item.name != updatedItem.name) {
                    val isDuplicate = existingMenus.any { existingMenu ->
                        existingMenu.menuName.equals(updatedItem.name.trim(), ignoreCase = true) && 
                        updatedItem.name.isNotBlank()
                    }
                    updatedItem.copy(isDuplicate = isDuplicate)
                } else {
                    updatedItem
                }
                newItem
            } else {
                item
            }
        }
        // 포커스 중이 아닐 때만 정렬 적용
        if (focusedItemId == null) {
            menuList = sortMenuList(menuList)
        }
    }
    
    // 메뉴 추가 함수
    fun addMenuItem() {
        val newItem = EditMenuItem(
            name = "",
            price = "0",
            category = "메인"
        )
        menuList = listOf(newItem) + menuList
    }
    
    // 메뉴 삭제 함수
    fun deleteMenuItem(id: String) {
        menuList = menuList.filter { it.id != id }
    }
    
    // 메뉴 저장 함수
    fun saveMenus() {
        if (isSaving) return
        
        // 중복 메뉴 자동 제거
        val duplicateCount = menuList.count { it.isDuplicate && it.name.isNotBlank() }
        val validMenus = menuList.filter { !it.isDuplicate || it.name.isBlank() }
        
        if (duplicateCount > 0) {
            Toast.makeText(
                context,
                "중복된 메뉴 ${duplicateCount}개를 자동으로 제거했습니다.",
                Toast.LENGTH_SHORT
            ).show()
            // 중복 제거된 리스트로 업데이트
            menuList = validMenus
        }
        
        if (validMenus.isEmpty()) {
            Toast.makeText(context, "저장할 메뉴가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        
        isSaving = true
        coroutineScope.launch {
            try {
                val userId = userPreferencesManager.userId.first() ?: "unknown_user"
                var successCount = 0
                val totalCount = validMenus.size
                val failedItems = mutableListOf<EditMenuItem>()
                
                validMenus.forEach { menu ->
                    // 빈 메뉴명이나 가격이 0인 메뉴는 건너뛰기
                    if (menu.name.trim().isEmpty()) {
                        return@forEach
                    }
                    
                    val priceInt = menu.price.toIntOrNull() ?: 0
                    
                    val result = menuRepository.createMenu(
                        userId = userId,
                        menuName = menu.name.trim(),
                        menuPrice = priceInt,
                        menuDescription = "",
                        category = menu.category
                    )
                    
                    if (result.isSuccess) {
                        successCount++
                    } else {
                        failedItems.add(menu)
                    }
                }
                
                if (failedItems.isEmpty()) {
                    Toast.makeText(
                        context, 
                        "모든 메뉴가 저장되었습니다. (${successCount}개)", 
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate("operation_management") {
                        popUpTo("operation_management_menu_upload") { inclusive = true }
                    }
                } else if (successCount > 0) {
                    // 실패한 메뉴만 남기고 다시 로드
                    menuList = failedItems
                    Toast.makeText(
                        context,
                        "저장 완료: ${successCount}개\n실패한 메뉴를 수정 후 다시 저장하세요.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "메뉴 저장에 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context, 
                    "메뉴 저장 중 오류가 발생했습니다: ${e.message}", 
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                isSaving = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text("메뉴 확인 및 수정") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate("operation_management_menu_upload") {
                                popUpTo("operation_management_menu_upload") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // 진행 상태 표시
            StepProgressIndicator(
                steps = listOf("사진등록", "AI분석중", "결과"),
                currentStep = 2
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 업로드된 이미지 표시
            imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "업로드된 메뉴판 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 메뉴 추가 버튼
            OutlinedButton(
                onClick = { addMenuItem() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "메뉴 추가")
                Spacer(modifier = Modifier.width(8.dp))
                Text("메뉴 추가하기")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 중복 메뉴 알림
            val duplicateCount = menuList.count { it.isDuplicate }
            if (duplicateCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️ 이미 존재하는 메뉴 ${duplicateCount}개",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 로딩 상태
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // 메뉴 목록
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(
                        items = menuList,
                        key = { _, item -> item.id }
                    ) { _, item ->
                        MenuEditCard(
                            item = item,
                            categories = categories,
                            onUpdate = { updatedItem ->
                                updateMenuItem(item.id, updatedItem)
                            },
                            onDelete = {
                                deleteMenuItem(item.id)
                            },
                            onFocusChanged = { hasFocus ->
                                focusedItemId = if (hasFocus) item.id else null
                                // 포커스를 잃을 때 정렬 적용
                                if (!hasFocus) {
                                    menuList = sortMenuList(menuList)
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 저장 버튼
            Button(
                onClick = { saveMenus() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving && menuList.isNotEmpty()
            ) {
                if (isSaving) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("저장 중...")
                    }
                } else {
                    Text("메뉴 저장하기")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuEditCard(
    item: EditMenuItem,
    categories: List<String>,
    onUpdate: (EditMenuItem) -> Unit,
    onDelete: () -> Unit,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        
        // 메뉴명
        OutlinedTextField(
            value = item.name,
            onValueChange = { newName ->
                onUpdate(item.copy(name = newName))
            },
            label = { Text("메뉴명", fontSize = 12.sp) },
            modifier = Modifier
                .weight(1.8f)
                .onFocusChanged { focusState ->
                    onFocusChanged(focusState.hasFocus)
                },
            singleLine = true,
            isError = item.isDuplicate && item.name.isNotBlank(),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (item.isDuplicate && item.name.isNotBlank()) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                unfocusedBorderColor = if (item.isDuplicate && item.name.isNotBlank()) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        )
        
        // 카테고리
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1.2f)
        ) {
            OutlinedTextField(
                readOnly = true,
                value = item.category,
                onValueChange = {},
                label = { Text("카테고리", fontSize = 12.sp) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category, fontSize = 14.sp) },
                        onClick = {
                            onUpdate(item.copy(category = category))
                            expanded = false
                        }
                    )
                }
            }
        }
        
        // 가격
        OutlinedTextField(
            value = remember(item.price) {
                val numeric = item.price.filter { it.isDigit() }
                val number = numeric.toLongOrNull() ?: 0L
                if (number == 0L) "" else DecimalFormat("#,###").format(number)
            },
            onValueChange = { newValue ->
                val numeric = newValue.filter { it.isDigit() }
                onUpdate(item.copy(price = numeric))
            },
            label = { Text("가격", fontSize = 12.sp) },
            modifier = Modifier.weight(1.2f),
            suffix = { Text("원", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
        
        // 삭제 버튼 (작게)
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "삭제",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}