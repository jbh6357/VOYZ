package com.voyz.presentation.screen.management.operation

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.text.DecimalFormat

class WonNumberVisualTransformation : VisualTransformation {
    private val formatter = DecimalFormat("#,###")

    override fun filter(text: AnnotatedString): TransformedText {
        val digitsOnly = text.text.filter { it.isDigit() }.take(15)
        val rawValue = digitsOnly.toLongOrNull() ?: 0L
        val formatted = formatter.format(rawValue)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val commaCount = (formatted.length - digitsOnly.length)
                return (offset + commaCount).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return digitsOnly.length.coerceAtMost(offset)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OperationManagementMenuInputScreen(
    navController: NavController,
    onSubmit: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current

    var imageUri by remember { mutableStateOf<String?>(null) }
    var foodName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // 카테고리 관련 상태
    val predefinedCategories = listOf("음식", "주류", "디저트", "음료", "사이드", "세트메뉴", "시그니처", "직접입력")
    var selectedCategory by remember { mutableStateOf("음식") }
    var customCategory by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var showCustomInput by remember { mutableStateOf(false) }
    
    // 최종 카테고리 값 (선택된 카테고리 또는 커스텀 카테고리)
    val finalCategory = if (showCustomInput) customCategory else selectedCategory

    // 이미지 선택 런처
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = {
                    Text(
                        text = "메뉴 직접 입력",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                        .clickable {
                            launcher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "선택된 이미지",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "사진 선택",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("메뉴 명") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 카테고리 선택 UI
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "카테고리",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (showCustomInput) "직접입력: $customCategory" else selectedCategory,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("카테고리 선택") },
                            trailingIcon = { 
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) 
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            predefinedCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (category == "직접입력") {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(category)
                                        }
                                    },
                                    onClick = {
                                        if (category == "직접입력") {
                                            showCustomInput = true
                                            customCategory = ""
                                        } else {
                                            selectedCategory = category
                                            showCustomInput = false
                                        }
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // 직접입력 모드일 때 텍스트 필드 표시
                    if (showCustomInput) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customCategory,
                            onValueChange = { customCategory = it },
                            label = { Text("카테고리 직접입력") },
                            placeholder = { Text("예: 시그니처, 스페셜 등") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                if (customCategory.isNotEmpty()) {
                                    IconButton(
                                        onClick = { customCategory = "" }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "지우기",
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        )
                        
                        // 미리 정의된 카테고리로 돌아가기 버튼
                        TextButton(
                            onClick = { 
                                showCustomInput = false
                                selectedCategory = "음식"
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("기본 카테고리로 돌아가기", fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it.filter { c -> c.isDigit() }.take(15)
                    },
                    label = { Text("가격") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = WonNumberVisualTransformation(),
                    trailingIcon = {
                        Text(
                            text = "원",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("설명") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("취소")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        onSubmit(imageUri ?: "", foodName, price, description, finalCategory)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = foodName.isNotBlank() && price.isNotBlank() && 
                        (!showCustomInput || customCategory.isNotBlank())
                ) {
                    Text("등록")
                }
            }
        }
    }
}
