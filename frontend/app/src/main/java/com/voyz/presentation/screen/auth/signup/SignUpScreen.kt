package com.voyz.presentation.screen.auth.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.UserRepository
import com.voyz.datas.repository.QrRepository
import com.voyz.presentation.screen.auth.signup.components.*
import com.voyz.presentation.component.GlobalToastManager
import com.voyz.ui.theme.KoreanRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit = {},
    onSignUpComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember {
        SignUpViewModel(
            userRepository = UserRepository(),
            userPreferencesManager = UserPreferencesManager(context),
            qrRepository = QrRepository()
        )
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isRegistrationComplete) {
        if (uiState.isRegistrationComplete) {
            // 회원가입 완료 Toast 표시
            GlobalToastManager.showRegistrationCompleteToast()
            // 즉시 로그인 화면으로 이동
            onSignUpComplete()
        }
    }
    
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회원가입") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep > 0) {
                            viewModel.previousStep()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                StepProgressIndicator(
                    currentStep = uiState.currentStep,
                    totalSteps = SignUpViewModel.TOTAL_STEPS,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                when (uiState.currentStep) {
                    0 -> AccountInfoStep(
                        userId = uiState.userId,
                        password = uiState.password,
                        confirmPassword = uiState.confirmPassword,
                        isUserIdValid = uiState.isUserIdValid,
                        isPasswordValid = uiState.isPasswordValid,
                        isPasswordMatch = uiState.isPasswordMatch,
                        onUserIdChange = viewModel::updateUserId,
                        onPasswordChange = viewModel::updatePassword,
                        onConfirmPasswordChange = viewModel::updateConfirmPassword
                    )
                    1 -> PersonalInfoStep(
                        userName = uiState.userName,
                        userPhone = uiState.userPhone,
                        isUserNameValid = uiState.isUserNameValid,
                        isUserPhoneValid = uiState.isUserPhoneValid,
                        onUserNameChange = viewModel::updateUserName,
                        onUserPhoneChange = viewModel::updateUserPhone
                    )
                    2 -> StoreInfoStep(
                        storeName = uiState.storeName,
                        storeCategory = uiState.storeCategory,
                        storeAddress = uiState.storeAddress,
                        tableCount = uiState.tableCount,
                        categories = SignUpViewModel.STORE_CATEGORIES,
                        isStoreNameValid = uiState.isStoreNameValid,
                        isStoreCategoryValid = uiState.isStoreCategoryValid,
                        isStoreAddressValid = uiState.isStoreAddressValid,
                        isTableCountValid = uiState.isTableCountValid,
                        onStoreNameChange = viewModel::updateStoreName,
                        onStoreCategoryChange = viewModel::updateStoreCategory,
                        onStoreAddressChange = viewModel::updateStoreAddress,
                        onTableCountChange = viewModel::updateTableCount
                    )
                }
            }
            
            Column {
                // 에러 메시지 표시
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                
                Button(
                    onClick = {
                        if (uiState.currentStep < 2) {
                            viewModel.nextStep()
                        } else {
                            viewModel.register()
                        }
                    },
                    enabled = viewModel.canProceedToNextStep() && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KoreanRed,
                        contentColor = Color.White
                    )
                ) {
                    if (uiState.isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text("회원가입 중...")
                        }
                    } else {
                        Text(
                            text = if (uiState.currentStep < 2) "다음" else "회원가입 완료",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}