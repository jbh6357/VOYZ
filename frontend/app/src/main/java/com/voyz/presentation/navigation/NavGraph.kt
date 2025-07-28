package com.voyz.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.voyz.presentation.fragment.AlarmScreen
import com.voyz.presentation.fragment.LoginScreen
import com.voyz.presentation.fragment.SignUpScreen
import com.voyz.presentation.fragment.MainScreen
import com.voyz.presentation.fragment.IdPwFindScreen
import com.voyz.presentation.fragment.ReminderScreen
import com.voyz.presentation.fragment.OperationManagementScreen
import com.voyz.presentation.fragment.CustomerManagementScreen
import com.voyz.presentation.fragment.SettingsScreen
import com.voyz.presentation.fragment.UserProfileScreen
import com.voyz.presentation.fragment.MarketingCreateScreen
import com.voyz.presentation.fragment.ReminderCreateScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginClick = { id, pw ->
                    if (id == "ad" && pw == "ad") {
                        navController.navigate("main")
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
        composable("signup") { SignUpScreen() }
        composable("find") { IdPwFindScreen() }
        composable("main") {
            MainScreen(navController = navController)
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
        composable("reminder_create") {
            ReminderCreateScreen(navController = navController)
        }

    }}