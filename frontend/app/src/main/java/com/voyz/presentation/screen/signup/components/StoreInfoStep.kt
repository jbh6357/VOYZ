package com.voyz.presentation.fragment.signup.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreInfoStep(
    storeName: String,
    storeCategory: String,
    storeAddress: String,
    categories: List<String>,
    isStoreNameValid: Boolean,
    isStoreCategoryValid: Boolean,
    isStoreAddressValid: Boolean,
    onStoreNameChange: (String) -> Unit,
    onStoreCategoryChange: (String) -> Unit,
    onStoreAddressChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 제목
        Column {
            Text(
                text = "매장 정보를 입력해주세요",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "운영하시는 매장의 기본 정보를 등록하세요",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
        
        // 매장명
        Column {
            OutlinedTextField(
                value = storeName,
                onValueChange = onStoreNameChange,
                label = { Text("매장명") },
                placeholder = { Text("매장 이름을 입력해주세요") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (storeName.isNotEmpty() && isStoreNameValid) Color.Green else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (storeName.isNotEmpty() && !isStoreNameValid) Color.Red else Color.Gray.copy(alpha = 0.5f)
                ),
                isError = storeName.isNotEmpty() && !isStoreNameValid
            )
            
            if (storeName.isNotEmpty() && !isStoreNameValid) {
                Text(
                    text = "매장명을 입력해주세요",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // 매장 카테고리
        Column {
            Text(
                text = "매장 카테고리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 카테고리 선택 칩들
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    FilterChip(
                        onClick = { onStoreCategoryChange(category) },
                        label = { Text(category) },
                        selected = storeCategory == category,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            if (storeCategory.isEmpty()) {
                Text(
                    text = "매장 카테고리를 선택해주세요",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // 매장 주소
        Column {
            OutlinedTextField(
                value = storeAddress,
                onValueChange = onStoreAddressChange,
                label = { Text("매장 주소") },
                placeholder = { Text("매장의 상세 주소를 입력해주세요") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (storeAddress.isNotEmpty() && isStoreAddressValid) Color.Green else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (storeAddress.isNotEmpty() && !isStoreAddressValid) Color.Red else Color.Gray.copy(alpha = 0.5f)
                ),
                isError = storeAddress.isNotEmpty() && !isStoreAddressValid,
                minLines = 2
            )
            
            if (storeAddress.isNotEmpty() && !isStoreAddressValid) {
                Text(
                    text = "매장 주소를 입력해주세요",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // 추가 안내 정보
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Primary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "거의 다 완료되었습니다!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "매장 정보는 마케팅 기회 분석에 활용됩니다.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}