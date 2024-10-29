package tddy.ko.cardstack.desigh_system.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun CardStackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val view = LocalView.current
    val window = (view.context as Activity).window

    WindowCompat
        .getInsetsController(window, view)
        .apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography
    ) {
        Surface(content = content)
    }
}