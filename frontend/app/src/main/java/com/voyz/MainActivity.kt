package com.voyz

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.voyz.presentation.navigation.NavGraph

import com.voyz.ui.theme.Blue500
import com.voyz.ui.theme.VOYZTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VOYZTheme {
                val view = LocalView.current
                SideEffect {
                    val window = (view.context as ComponentActivity).window
                    window.statusBarColor = Blue500.toArgb()
                    window.navigationBarColor = Blue500.toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                }

                val navController = rememberNavController()
                    NavGraph(navController = navController)

            }
        }
    }

}

