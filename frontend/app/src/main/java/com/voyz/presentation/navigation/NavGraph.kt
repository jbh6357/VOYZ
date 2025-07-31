package com.voyz.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.voyz.presentation.screen.reminder.AlarmScreen
import com.voyz.presentation.screen.auth.LoginScreen
import com.voyz.presentation.screen.auth.signup.SignUpScreen
import com.voyz.presentation.screen.main.MainScreen
import com.voyz.presentation.screen.auth.IdPwFindScreen
import com.voyz.presentation.screen.reminder.ReminderScreen
import com.voyz.presentation.screen.management.OperationManagementScreen
import com.voyz.presentation.screen.management.CustomerManagementScreen
import com.voyz.presentation.screen.management.SettingsScreen
import com.voyz.presentation.screen.management.UserProfileScreen
import com.voyz.presentation.screen.marketing.MarketingCreateScreen
import com.voyz.presentation.screen.reminder.ReminderCreateScreen
import com.voyz.presentation.screen.main.SearchScreen
import com.voyz.presentation.screen.marketing.MarketingOpportunityDetailScreen
import com.voyz.presentation.screen.reminder.ReminderDetailScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun NavGraph(navController: NavHostController) {
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
        composable("find") { IdPwFindScreen() }
        composable("main") {
            MainScreen(navController = navController,
                onSearchClick = {
                    navController.navigate("search")  // ✅ 검색 페이지로 이동
                },
                onAlarmClick = {
                    navController.navigate("alarm")  // ✅ 알림 → 알림제안 화면으로 이동
                })
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
                onBackClick = { navController.popBackStack() })
        }
        composable("operation_management") {
            OperationManagementScreen(navController = navController)
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
    }}