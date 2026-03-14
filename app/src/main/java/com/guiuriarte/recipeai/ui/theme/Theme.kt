package com.guiuriarte.recipeai.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BrandOrange,
    onPrimary = White,
    primaryContainer = BrandOrangeLight,
    onPrimaryContainer = BrandOrangeDark,
    secondary = Green40,
    onSecondary = White,
    secondaryContainer = Green90,
    onSecondaryContainer = Green10,
    error = ErrorRed,
    onError = White,
    background = BackgroundWhite,
    onBackground = TextDark,
    surface = BackgroundWhite,
    onSurface = TextDark,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextMedium,
)

private val DarkColorScheme = darkColorScheme(
    primary = Orange80,
    onPrimary = Orange20,
    primaryContainer = Orange40,
    onPrimaryContainer = Orange90,
    secondary = Green80,
    onSecondary = Green20,
    secondaryContainer = Green40,
    onSecondaryContainer = Green90,
    error = Red80,
    onError = Neutral10,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
)

@Composable
fun RecipeAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        content = content
    )
}
