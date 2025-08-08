package com.voyz.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// VOYZ WEB palette (A안) mapping to Material3
private val DarkColorScheme = darkColorScheme(
    primary = KoreanRed,
    secondary = KoreanGold,
    tertiary = KoreanGold,
    background = Gray900,
    surface = Gray800,
    onPrimary = KoreanWhite,
    onSecondary = KoreanWhite,
    onBackground = KoreanWhite,
    onSurface = KoreanWhite
)

private val LightColorScheme = lightColorScheme(
    primary = KoreanRed,
    secondary = KoreanGold,
    tertiary = KoreanGold,
    background = KoreanLightGray,
    surface = KoreanWhite,
    onPrimary = KoreanWhite,
    onSecondary = KoreanWhite,
    onBackground = KoreanBlack,
    onSurface = KoreanBlack
)

private val VOYZShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

@Composable
fun VOYZTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // 고정 색상 사용
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = VOYZShapes,
        content = content
    )
}