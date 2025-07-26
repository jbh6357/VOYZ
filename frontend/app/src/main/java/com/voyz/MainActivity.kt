package com.voyz

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.Modifier
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
                    // 시스템바를 반투명하게 설정하여 앱과 구분
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    window.navigationBarColor = android.graphics.Color.TRANSPARENT
                    
                    val insetsController = WindowCompat.getInsetsController(window, view)
                    // 밝은 배경이므로 어두운 아이콘 사용
                    insetsController.isAppearanceLightStatusBars = true
                    insetsController.isAppearanceLightNavigationBars = true
                }

                val navController = rememberNavController()
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues())
                ) {
                    NavGraph(navController = navController)
                }

            }
        }
    }

}

