package com.voyz.presentation.fragment.signup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.UserRepository
import com.voyz.presentation.fragment.signup.components.AccountInfoStep
import com.voyz.presentation.fragment.signup.components.PersonalInfoStep
import com.voyz.presentation.fragment.signup.components.StoreInfoStep
import com.voyz.presentation.fragment.signup.components.StepProgressIndicator
import com.voyz.ui.theme.Primary

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onSignUpComplete: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember {
        SignUpViewModel(
            userRepository = UserRepository(),
            userPreferencesManager = UserPreferencesManager(context)
        )
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // 회원가입 완료 처리
    LaunchedEffect(uiState.isRegistrationComplete) {
        if (uiState.isRegistrationComplete) {
            onSignUpComplete()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        TopAppBar(
            title = {
                Text(
                    text = "회원가입",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
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
                containerColor = Color.White
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // 프로그레스 인디케이터
            StepProgressIndicator(
                currentStep = uiState.currentStep,
                totalSteps = SignUpViewModel.TOTAL_STEPS,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 단계별 콘텐츠
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = uiState.currentStep,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { if (targetState > initialState) it else -it }
                        ) + fadeIn() togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { if (targetState > initialState) -it else it }
                        ) + fadeOut()
                    },
                    label = "StepTransition"
                ) { step ->
                    when (step) {
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
                            categories = SignUpViewModel.STORE_CATEGORIES,
                            isStoreNameValid = uiState.isStoreNameValid,
                            isStoreCategoryValid = uiState.isStoreCategoryValid,
                            isStoreAddressValid = uiState.isStoreAddressValid,
                            onStoreNameChange = viewModel::updateStoreName,
                            onStoreCategoryChange = viewModel::updateStoreCategory,
                            onStoreAddressChange = viewModel::updateStoreAddress
                        )
                    }
                }
            }
            
            // 에러 메시지
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 하단 버튼
            Button(
                onClick = {
                    if (uiState.currentStep == SignUpViewModel.TOTAL_STEPS - 1) {
                        viewModel.register()
                    } else {
                        viewModel.nextStep()
                    }
                },
                enabled = viewModel.canProceedToNextStep() && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (uiState.currentStep == SignUpViewModel.TOTAL_STEPS - 1) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "회원가입 완료",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "다음 단계",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}