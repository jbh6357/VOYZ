package com.voyz.presentation.screen.management.operation

import StepProgressIndicator
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.voyz.presentation.fake.MenuItem
import coil.compose.AsyncImage
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import java.text.DecimalFormat
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults


data class EditMenuItem(
    var name: String,
    var price: String,
    var category: String
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationManagementMenuConfirmScreen(
    navController: NavController,
    imageUri: Uri?
) {
    val context = LocalContext.current

    // 초기 더미 분석 데이터
    var EditmenuList by remember {
        mutableStateOf(
            listOf(
                EditMenuItem("된장찌개", "7000", category = "음식"),
                EditMenuItem("김치찌개", "7500", category = "음식"),
                EditMenuItem("제육볶음", "8000", category = "음식")
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text("메뉴 확인 및 수정") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("operation_management_menu_upload") {
                            popUpTo("operation_management_menu_upload") {
                                inclusive = true
                            }
                        }
                    }) {
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
            StepProgressIndicator(
                steps = listOf("사진등록", "AI분석중", "결과"),
                currentStep = 2
            )
            Spacer(modifier = Modifier.height(16.dp))

            imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "업로드된 메뉴판 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(EditmenuList) { index, item ->
                    MenuEditRow(
                        item = item,
                        onChange = { updatedItem ->
                            EditmenuList = EditmenuList.toMutableList().also {
                                it[index] = updatedItem
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Button(
                onClick = {
                    Toast.makeText(context, "메뉴가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    navController.navigate("operation_management") {
                        popUpTo("operation_management_menu_upload") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("저장")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuEditRow(
    item: EditMenuItem,
    onChange: (EditMenuItem) -> Unit
) {
    val formattedPrice = remember(item.price) {
        val numeric = item.price.filter { it.isDigit() }
        val number = numeric.toLongOrNull() ?: 0L
        DecimalFormat("#,###").format(number)
    }

    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("음식", "주류")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ 메뉴명 (가장 왼쪽)
        OutlinedTextField(
            value = item.name,
            onValueChange = { onChange(item.copy(name = it)) },
            label = { Text("메뉴명") },
            modifier = Modifier.weight(1.5f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // ✅ 카테고리 드롭다운 (중간)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                readOnly = true,
                value = item.category,
                onValueChange = {},
                label = { Text("카테고리") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
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
                            onChange(item.copy(category = category))
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // ✅ 가격 (오른쪽)
        OutlinedTextField(
            value = formattedPrice,
            onValueChange = {
                val numeric = it.filter { ch -> ch.isDigit() }
                onChange(item.copy(price = numeric))
            },
            label = { Text("가격") },
            modifier = Modifier.weight(1f),
            trailingIcon = {
                Text("원", style = MaterialTheme.typography.bodySmall)
            },
            visualTransformation = object : VisualTransformation {
                override fun filter(text: AnnotatedString): TransformedText {
                    val digits = text.text.filter { it.isDigit() }
                    val number = digits.toLongOrNull() ?: 0L
                    val formatted = DecimalFormat("#,###").format(number)
                    return TransformedText(AnnotatedString(formatted), OffsetMapping.Identity)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
    }
}