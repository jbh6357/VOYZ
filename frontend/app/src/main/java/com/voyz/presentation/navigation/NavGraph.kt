package com.voyz.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.voyz.presentation.screen.reminder.AlarmScreen
import com.voyz.presentation.screen.auth.LoginScreen
import com.voyz.presentation.screen.auth.signup.SignUpScreen
import com.voyz.presentation.screen.main.MainScreen
import com.voyz.presentation.screen.auth.IdPwFindScreen
import com.voyz.presentation.screen.reminder.ReminderScreen
import com.voyz.presentation.screen.management.operation.OperationManagementScreen
import com.voyz.presentation.screen.management.review.CustomerManagementScreen
import com.voyz.presentation.screen.management.SettingsScreen
import com.voyz.presentation.screen.management.UserProfileScreen
import com.voyz.presentation.screen.marketing.MarketingCreateScreen
import com.voyz.presentation.screen.reminder.ReminderCreateScreen
import com.voyz.presentation.screen.main.SearchScreen
import com.voyz.presentation.screen.marketing.MarketingOpportunityDetailScreen
import com.voyz.presentation.screen.reminder.ReminderDetailScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.voyz.presentation.screen.management.operation.OperationManagementMenuConfirmScreen
import com.voyz.presentation.screen.management.operation.OperationManagementMenuUploadScreen
import com.voyz.presentation.screen.management.operation.OperationManagementMenuInputScreen
import com.voyz.presentation.screen.management.operation.OperationManagementMenuProcessingScreen
import com.voyz.datas.model.dto.MenuItemDto
import com.voyz.presentation.screen.management.operation.MenuRepository
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavGraph(navController: NavHostController) {
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val ocrResults = remember { mutableStateOf<List<com.voyz.datas.model.dto.MenuItemDto>>(emptyList()) }
    val menuRepository = remember { MenuRepository() }
    val coroutineScope = rememberCoroutineScope()
    
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignupClick = {
                    navController.navigate("signup")
                },
                onFindClick = {
                    navController.navigate("find")
                }
            )
        }
        
        composable("signup") { 
            SignUpScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignUpComplete = { navController.navigate("login") }
            ) 
        }
        
        composable("find") { 
            IdPwFindScreen() 
        }
        
        composable("main") {
            MainScreen(
                navController = navController,
                onSearchClick = {
                    navController.navigate("search")
                },
                onAlarmClick = {
                    navController.navigate("alarm")
                }
            )
        }
        
        // ReminderScreen을 위한 두 가지 route 모두 지원 (하위 호환성)
        composable("dashboard") {
            ReminderScreen(
                navController = navController,
                onAlarmClick = { navController.navigate("alarm") }
            )
        }
        
        composable("reminder") {
            ReminderScreen(
                navController = navController,
                onAlarmClick = { navController.navigate("alarm") }
            )
        }
        
        composable("alarm") {
            AlarmScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("search") {
            SearchScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("operation_management") {
            OperationManagementScreen(navController = navController)
        }
        
        composable("operation_management_menu_upload") {
            OperationManagementMenuUploadScreen(
                navController = navController,
                imageUri = imageUri.value,
                onImageSelected = { 
                    android.util.Log.d("NavGraph", "Image selected: $it")
                    imageUri.value = it 
                },
                onDeleteImage = { 
                    android.util.Log.d("NavGraph", "Image deleted")
                    imageUri.value = null 
                },
                onNextStep = {
                    android.util.Log.d("NavGraph", "Moving to processing screen, imageUri: ${imageUri.value}")
                    navController.navigate("operation_management_menu_processing")
                }
            )
        }

        composable("operation_management_menu_processing") {
            OperationManagementMenuProcessingScreen(
                navController = navController,
                imageUri = imageUri.value,
                onOcrComplete = { results ->
                    ocrResults.value = results
                }
            )
        }

        composable("operation_management_menu_confirm") {
            OperationManagementMenuConfirmScreen(
                navController = navController,
                imageUri = imageUri.value,
                ocrResults = ocrResults.value
            )
        }

        composable("operation_management_menu_input") {
            OperationManagementMenuInputScreen(
                navController = navController,
                onSubmit = { imageUri, menuName, price, description, category ->
                    coroutineScope.launch {
                        try {
                            // TODO: userId를 실제 로그인된 사용자 ID로 변경
                            val userId = "test_user"
                            val priceInt = price.toIntOrNull() ?: 0
                            
                            val result = menuRepository.createMenu(
                                userId = userId,
                                menuName = menuName,
                                menuPrice = priceInt,
                                menuDescription = description,
                                category = category
                            )
                            
                            if (result.isSuccess) {
                                navController.popBackStack()
                            } else {
                                // 에러 처리
                                android.util.Log.e("MenuCreate", "메뉴 생성 실패: ${result.exceptionOrNull()}")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MenuCreate", "메뉴 생성 중 오류: ${e.message}")
                        }
                    }
                }
            )
        }
        
        composable("customer_management") {
            CustomerManagementScreen(navController = navController)
        }
        
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        
        composable("user_profile") {
            UserProfileScreen(navController = navController)
        }
        
        composable("marketing_create") {
            MarketingCreateScreen(navController = navController)
        }
        
        // 기본 리마인더 생성 (FAB에서 호출)
        composable("reminder_create") {
            ReminderCreateScreen(
                navController = navController,
                onReminderCreated = {
                    // 리마인더 생성 완료 후 처리는 ReminderCreateScreen에서 직접 처리
                }
            )
        }
        
        // 파라미터가 있는 리마인더 생성 (제안에서 호출)
        composable(
            "reminder_create?title={title}&content={content}&date={date}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType; defaultValue = "" },
                navArgument("content") { type = NavType.StringType; defaultValue = "" },
                navArgument("date") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val content = backStackEntry.arguments?.getString("content") ?: ""
            val date = backStackEntry.arguments?.getString("date") ?: ""
            
            android.util.Log.d("NavGraph", "=== Navigation to ReminderCreate ===")
            android.util.Log.d("NavGraph", "title: '$title'")
            android.util.Log.d("NavGraph", "content: '$content'") 
            android.util.Log.d("NavGraph", "date: '$date'")
            
            ReminderCreateScreen(
                navController = navController,
                initialTitle = title,
                initialContent = content,
                initialDate = date,
                onReminderCreated = {
                    // 리마인더 생성 완료 후 처리는 ReminderCreateScreen에서 직접 처리
                }
            )
        }
        
        // 제안 상세보기 (특일 제안)
        composable(
            "marketing_opportunity/{ssuIdx}",
            arguments = listOf(navArgument("ssuIdx") { type = NavType.IntType })
        ) { backStackEntry ->
            val ssuIdx = backStackEntry.arguments?.getInt("ssuIdx") ?: 0
            MarketingOpportunityDetailScreen(
                navController = navController,
                ssuIdx = ssuIdx
            )
        }
        
        // 리마인더 상세보기
        composable(
            "reminder_detail/{marketingIdx}",
            arguments = listOf(navArgument("marketingIdx") { type = NavType.IntType })
        ) { backStackEntry ->
            val marketingIdx = backStackEntry.arguments?.getInt("marketingIdx") ?: 0
            ReminderDetailScreen(
                navController = navController,
                marketingIdx = marketingIdx
            )
        }
    }
}