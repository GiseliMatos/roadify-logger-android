package br.edu.utfpr.roadifylogger.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Neutral99,
    primaryContainer = Purple90,
    onPrimaryContainer = Purple10,

    secondary = Grey40,
    onSecondary = Neutral99,
    secondaryContainer = Grey90,
    onSecondaryContainer = Grey10,

    tertiary = Rose40,
    onTertiary = Neutral99,
    tertiaryContainer = Rose90,
    onTertiaryContainer = Rose10,

    error = Red40,
    onError = Neutral99,
    errorContainer = Red90,
    onErrorContainer = Red10,

    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    outline = NeutralVariant50,

    surfaceContainer = NeutralVariant90 // fundo das barras
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Purple30,
    primaryContainer = Purple30,
    onPrimaryContainer = Purple90,

    secondary = Grey80,
    onSecondary = Grey30,
    secondaryContainer = Grey30,
    onSecondaryContainer = Grey90,

    tertiary = Rose80,
    onTertiary = Rose30,
    tertiaryContainer = Rose30,
    onTertiaryContainer = Rose90,

    error = Red80,
    onError = Red10,
    errorContainer = Red40,
    onErrorContainer = Red90,

    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    outline = NeutralVariant50,

    surfaceContainer = Color(0xFF232128) // fundo das barras
)

@Composable
fun RoadifyLoggerTheme(
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}