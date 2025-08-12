package com.voyz.presentation.screen.management.operation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.presentation.screen.management.operation.MenuRepository
import com.voyz.datas.model.dto.MenuItemDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import android.widget.Toast
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.voyz.utils.Constants

// 편집 가능한 메뉴 아이템 데이터 클래스
data class EditableMenuItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    var menuIdx: Int? = null,
    var name: String,
    var price: String,
    var category: String,
    var description: String = "",
    var imageUri: Uri? = null,
    var imageUrl: String? = null,
    var isNew: Boolean = false,
    var isEditing: Boolean = false,
    // 원본 데이터 (변경 감지용)
    var originalName: String = name,
    var originalPrice: String = price,
    var originalCategory: String = category,
    var originalDescription: String = description,
    var originalImageUrl: String? = imageUrl
)

// 이미지 URL을 전체 URL로 변환하는 함수
fun getFullImageUrl(imageUrl: String?): String? {
    if (imageUrl.isNullOrEmpty()) return null
    return if (imageUrl.startsWith("http")) {
        imageUrl // 이미 전체 URL
    } else {
        // 상대 경로면 BASE_URL의 서버 부분과 결합
        val baseUrl = Constants.BASE_URL.removeSuffix("api/").removeSuffix("/") // http://192.168.219.205:8081
        val cleanImageUrl = imageUrl.removePrefix("/") // uploads/menuImages/...
        "$baseUrl/$cleanImageUrl"
    }
}

// 메뉴 아이템이 변경되었는지 확인하는 함수
fun EditableMenuItem.hasChanges(): Boolean {
    val textChanged = name != originalName ||
            price != originalPrice ||
            category != originalCategory ||
            description != originalDescription
    
    val imageChanged = imageUri != null // 새로운 이미지가 선택됨
    
    return textChanged || imageChanged
}

@Composable
fun MenuManagementCard(
    userId: String?,
    onMenuUpload: () -> Unit = {},
    onMenuInput: () -> Unit = {}
) {
    var menuItems by remember { mutableStateOf<List<EditableMenuItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val menuRepository = remember { MenuRepository() }
    val scope = rememberCoroutineScope()
    
    // 화면 높이의 60% 계산
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val cardHeight = screenHeightDp * 0.6f

    // 메뉴 데이터 로드
    fun loadMenuItems() {
        val id = userId ?: return
        scope.launch {
            isLoading = true
            try {
                val result = menuRepository.getMenusByUserId(id)
                if (result.isSuccess) {
                    val allMenus = result.getOrNull() ?: emptyList()
                    
                    // MenuItemDto를 EditableMenuItem으로 변환
                    menuItems = allMenus
                        .filter { it.menuName != null && it.menuName.isNotBlank() }
                        .map { menu ->
                            android.util.Log.d("MenuImage", "메뉴 로드: ${menu.menuName}, imageUrl: ${menu.imageUrl}")
                            EditableMenuItem(
                                menuIdx = menu.menuIdx,
                                name = menu.menuName,
                                price = menu.menuPrice.toString(),
                                category = menu.category ?: "기타",
                                description = menu.menuDescription ?: "",
                                imageUrl = menu.imageUrl,
                                isNew = false,
                                isEditing = false,
                                originalName = menu.menuName,
                                originalPrice = menu.menuPrice.toString(),
                                originalCategory = menu.category ?: "기타",
                                originalDescription = menu.menuDescription ?: "",
                                originalImageUrl = menu.imageUrl
                            )
                        }
                        .sortedBy { it.category }
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

    // 새 메뉴 추가
    fun addNewMenuItem() {
        val newItem = EditableMenuItem(
            name = "",
            price = "0",
            category = menuItems.firstOrNull()?.category ?: "메인",
            description = "",
            isNew = true,
            isEditing = true
        )
        menuItems = listOf(newItem) + menuItems
    }

    // 메뉴 저장
    fun saveMenuItem(item: EditableMenuItem) {
        android.util.Log.d("MenuEdit", "saveMenuItem 호출: ${item.name}, isNew=${item.isNew}, imageUri=${item.imageUri}")
        
        if (item.name.trim().isEmpty()) {
            Toast.makeText(context, "메뉴명을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isSaving = true
            try {
                val priceInt = item.price.toIntOrNull() ?: 0
                
                val result = if (item.isNew) {
                    // 새 메뉴 생성
                    menuRepository.createMenuWithImage(
                        userId = userId ?: "",
                        menuName = item.name.trim(),
                        menuPrice = priceInt,
                        menuDescription = item.description.trim(),
                        category = item.category,
                        imageUri = item.imageUri,
                        context = context
                    )
                } else {
                    // 기존 메뉴 수정 - 새로운 updateMenuWithImage API 사용
                    item.menuIdx?.let { menuIdx ->
                        menuRepository.updateMenuWithImage(
                            menuIdx = menuIdx,
                            menuName = item.name.trim(),
                            menuPrice = priceInt,
                            menuDescription = item.description.trim(),
                            category = item.category,
                            imageUri = item.imageUri, // null이면 기존 이미지 유지
                            context = context
                        )
                    } ?: Result.failure(Exception("메뉴 ID가 없습니다"))
                }
                
                if (result.isSuccess) {
                    val message = if (item.isNew) "메뉴가 저장되었습니다" else "메뉴가 수정되었습니다"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    loadMenuItems() // 목록 새로고침
                } else {
                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "저장 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "메뉴 저장 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            } finally {
                isSaving = false
            }
        }
    }

    // 메뉴 삭제
    fun deleteMenuItem(item: EditableMenuItem) {
        if (item.isNew) {
            // 새로 추가된 항목은 그냥 목록에서 제거
            menuItems = menuItems.filter { it.id != item.id }
        } else {
            // 기존 메뉴는 API 호출
            scope.launch {
                item.menuIdx?.let { menuIdx ->
                    try {
                        val result = menuRepository.deleteMenu(menuIdx)
                        if (result.isSuccess) {
                            menuItems = menuItems.filter { it.id != item.id }
                            Toast.makeText(context, "메뉴가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "삭제 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // 카테고리별로 그룹화 - 수정 중인 메뉴는 원래 카테고리에 유지
    val groupedMenus = menuItems.groupBy { item ->
        // 수정 중이고 카테고리가 변경된 경우에만 원래 카테고리에 유지
        if (item.isEditing && item.category != item.originalCategory) {
            android.util.Log.d("MenuCategory", "메뉴 ${item.name}: 편집중, 원래카테고리=${item.originalCategory}, 현재카테고리=${item.category}")
            item.originalCategory
        } else {
            item.category
        }
    }
    
    // 카테고리 우선순위 정의 (우선순위 높은 순)
    val categoryPriority = mapOf(
        "메인" to 1,
        "세트메뉴" to 2, 
        "사이드" to 3,
        "주류" to 4,
        "음료" to 5,
        "디저트" to 6,
        "시그니처" to 7,
        "기타" to 8
    )
    
    // 기본 카테고리 + 기존 메뉴 카테고리 합치기
    val defaultCategories = categoryPriority.keys.toList()
    val existingCategories = groupedMenus.keys.toSet()
    val allCategories = (defaultCategories + existingCategories).distinct()
    
    // 메뉴가 있는 카테고리만 필터링하고 우선순위로 정렬
    val categories = groupedMenus.keys.sortedWith { a, b ->
        val aPriority = categoryPriority[a] ?: 999
        val bPriority = categoryPriority[b] ?: 999
        aPriority.compareTo(bPriority)
    }

    if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFCD212A),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .background(Color.White) // 배경색을 흰색으로 설정
                ) {
                    if (menuItems.isEmpty()) {
                        // 빈 상태: 중앙에 Card로 감싼 버튼들 표시
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "등록된 메뉴가 없습니다",
                                    fontSize = 16.sp,
                                    color = Color(0xFF8E8E93),
                                    modifier = Modifier.padding(bottom = 32.dp)
                                )
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // OCR 업로드 버튼 (큰 버튼)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .background(
                                                    color = Color(0xFFF2F2F7),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clickable { onMenuUpload() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "메뉴 촬영",
                                                tint = Color(0xFF1D1D1F),
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "촬영하기",
                                            fontSize = 12.sp,
                                            color = Color(0xFF8E8E93)
                                        )
                                    }
                                    
                                    // 메뉴 추가 버튼 (큰 버튼)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .background(
                                                    color = Color(0xFFCD212A),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clickable { addNewMenuItem() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "메뉴 추가",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "직접 입력",
                                            fontSize = 12.sp,
                                            color = Color(0xFF8E8E93)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // 메뉴가 있는 상태: Column 사용 (부모가 이미 스크롤 가능)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                        ) {
                            categories.forEachIndexed { categoryIndex, category ->
                                // 카테고리 간 간격 추가 (첫 번째가 아닐 때)
                                if (categoryIndex > 0) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                                
                                // 카테고리 제목과 버튼 (첫 번째 카테고리일 때만 버튼 표시)
                                if (categoryIndex == 0) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 0.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = category,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1D1D1F)
                                        )
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
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
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            // 메뉴 추가 버튼
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(
                                                        color = Color(0xFFCD212A),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { addNewMenuItem() },
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
                                } else {
                                    // 나머지 카테고리는 제목만
                                    Text(
                                        text = category,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D1D1F),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 0.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // 카테고리 구분선 (전체 너비)
                                Divider(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFFCD212A).copy(alpha = 0.3f),
                                    thickness = 2.dp
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // 해당 카테고리의 메뉴들
                                groupedMenus[category]?.forEachIndexed { itemIndex, item ->
                                    ExpandableMenuCard(
                                        item = item,
                                        categories = allCategories,
                                        onUpdate = { updatedItem ->
                                            menuItems = menuItems.map { 
                                                if (it.id == item.id) updatedItem else it 
                                            }
                                        },
                                        onSave = { saveMenuItem(item) },
                                        onDelete = { deleteMenuItem(item) },
                                        isSaving = isSaving
                                    )
                                    
                                    // 메뉴 아이템 사이 간격
                                    if (itemIndex < (groupedMenus[category]?.size ?: 0) - 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        } // Column 닫기 (메뉴가 있는 상태)
                    } // else 닫기
                } // Box 닫기
            }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableMenuCard(
    item: EditableMenuItem,
    categories: List<String>,
    onUpdate: (EditableMenuItem) -> Unit,
    onSave: (EditableMenuItem) -> Unit,
    onDelete: () -> Unit,
    isSaving: Boolean
) {
    var isExpanded by remember(item.id) { mutableStateOf(item.isEditing) }
    
    // 확장 상태가 변경될 때 isEditing도 업데이트
    LaunchedEffect(isExpanded) {
        if (isExpanded && !item.isNew) {
            onUpdate(item.copy(isEditing = true))
        } else if (!isExpanded && !item.isNew && !item.hasChanges()) {
            onUpdate(item.copy(isEditing = false))
        }
    }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onUpdate(item.copy(imageUri = it))
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (item.isNew) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = Color(0xFF007AFF).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 기본 상태: 메뉴명 + 가격
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        isExpanded = !isExpanded
                        // 카드를 닫을 때 변경사항이 없으면 isEditing을 false로
                        if (!isExpanded && !item.hasChanges() && !item.isNew) {
                            onUpdate(item.copy(isEditing = false))
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (item.name.isBlank()) "새 메뉴" else item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (item.name.isBlank()) Color(0xFF8E8E93) else Color(0xFF1D1D1F),
                    modifier = Modifier.weight(1f)
                )
                
                if (!item.isNew) {
                    Text(
                        text = "${DecimalFormat("#,###").format(item.price.toIntOrNull() ?: 0)}원",
                        fontSize = 14.sp,
                        color = Color(0xFF007AFF),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93)
                )
            }
            
            // 확장된 상태: 편집 필드들
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 카테고리 변경 알림 (카테고리가 변경된 경우)
                    if (!item.isNew && item.category != item.originalCategory) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3CD)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "카테고리가 '${item.originalCategory}'에서 '${item.category}'로 변경됩니다.",
                                fontSize = 12.sp,
                                color = Color(0xFF856404),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // 이미지 추가/변경 버튼 (메뉴명 위)
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (item.imageUri != null || item.imageUrl != null) "이미지 변경" else "이미지 추가",
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 이미지 표시 (이미지 추가 버튼 바로 아래)
                    if (item.imageUri != null || item.imageUrl != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                        ) {
                            AsyncImage(
                                model = item.imageUri ?: getFullImageUrl(item.imageUrl),
                                contentDescription = "메뉴 이미지",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF2F2F7)),
                                onError = { 
                                    android.util.Log.e("MenuImage", "이미지 로딩 실패: ${getFullImageUrl(item.imageUrl)}")
                                }
                            )
                            
                            // 이미지 삭제 버튼
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(24.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onUpdate(item.copy(imageUri = null)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "이미지 삭제",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // 메뉴명 입력
                    OutlinedTextField(
                        value = item.name,
                        onValueChange = { onUpdate(item.copy(name = it)) },
                        label = { Text("메뉴명", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 카테고리
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = item.category,
                                onValueChange = {},
                                label = { Text("카테고리", fontSize = 12.sp) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                singleLine = true
                            )
                            
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category, fontSize = 14.sp) },
                                        onClick = {
                                            onUpdate(item.copy(category = category))
                                            categoryExpanded = false
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor = Color(0xFF1D1D1F),
                                            leadingIconColor = Color(0xFF1D1D1F)
                                        )
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
                            modifier = Modifier.weight(1f),
                            suffix = { Text("원", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 메뉴 설명
                    OutlinedTextField(
                        value = item.description,
                        onValueChange = { onUpdate(item.copy(description = it)) },
                        label = { Text("메뉴 설명 (선택사항)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("예: 신선한 재료로 만든 시그니처 메뉴", fontSize = 11.sp) },
                        maxLines = 2,
                        singleLine = false
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 액션 버튼들
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.isNew) {
                            // 새 메뉴: 저장, 취소
                            Button(
                                onClick = { onSave(item) },
                                modifier = Modifier.weight(1f),
                                enabled = !isSaving && item.name.isNotBlank()
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("저장", fontSize = 12.sp)
                                }
                            }
                            
                            OutlinedButton(
                                onClick = onDelete,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("취소", fontSize = 12.sp)
                            }
                        } else {
                            // 기존 메뉴: 수정, 삭제
                            val hasChanges = item.hasChanges()
                            val buttonEnabled = !isSaving && item.name.isNotBlank() && hasChanges
                            
                            // 디버그 로그
                            if (item.imageUri != null) {
                                android.util.Log.d("MenuEdit", "메뉴 ${item.name}: hasChanges=$hasChanges, buttonEnabled=$buttonEnabled, imageUri=${item.imageUri}")
                            }
                            
                            Button(
                                onClick = { 
                                    android.util.Log.d("MenuEdit", "수정 버튼 클릭: ${item.name}")
                                    onSave(item) 
                                },
                                modifier = Modifier.weight(1f),
                                enabled = buttonEnabled
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("수정", fontSize = 12.sp)
                                }
                            }
                            
                            OutlinedButton(
                                onClick = onDelete,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("삭제", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}