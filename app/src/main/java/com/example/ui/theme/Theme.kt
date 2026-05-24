package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LegalGoldDarkSecondary,
    secondary = LegalGoldTertiary,
    tertiary = LegalTextDarkSecondary,
    background = LegalNavyDarkBackground,
    surface = LegalNavyDarkSurface,
    onPrimary = LegalNavyDarkPrimary,
    onSecondary = LegalNavyDarkPrimary,
    onBackground = LegalTextDarkPrimary,
    onSurface = LegalTextDarkPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = LegalNavyPrimary,
    secondary = LegalGoldSecondary,
    tertiary = LegalGoldTertiary,
    background = LegalIceBackground,
    surface = LegalSoftSurface,
    onPrimary = LegalSoftSurface,
    onSecondary = LegalNavyPrimary,
    onBackground = LegalTextPrimary,
    onSurface = LegalTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false to enforce our elegant, custom-designed legal color palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
