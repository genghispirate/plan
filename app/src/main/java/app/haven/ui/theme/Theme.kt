package app.haven.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Access point for the extended palette: `Haven.colors.brass`, etc. */
val LocalHavenColors = staticCompositionLocalOf { LightExtendedColors }

object Haven {
    val colors: HavenExtendedColors
        @Composable get() = LocalHavenColors.current
}

/**
 * The single theme wrapper. Supplies Material3 (color scheme, typography,
 * shapes) plus the extended [HavenExtendedColors], and syncs the system bars to
 * transparent with the correct icon contrast for edge-to-edge rendering.
 */
@Composable
fun HavenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) HavenDarkColors else HavenLightColors
    val extended = if (darkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val lightIcons = colorScheme.background.luminance() > 0.5f
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = lightIcons
                isAppearanceLightNavigationBars = lightIcons
            }
        }
    }

    CompositionLocalProvider(LocalHavenColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HavenTypography,
            shapes = HavenShapes,
            content = content,
        )
    }
}
