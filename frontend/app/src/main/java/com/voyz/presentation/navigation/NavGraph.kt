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

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginClick = { id, pw ->
                    if (id == "admin" && pw == "admin") {
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
        composable("main") { MainScreen() }



        composable("reminder") { // 리마인더에서 알람 클릭
            ReminderScreen(
                onAlarmClick = { navController.navigate("alarm") }
            )
        }
        composable("alarm") {
            AlarmScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }

}