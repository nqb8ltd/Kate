package co.nqb8.kate.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

val darkColorPalette = darkColorScheme(
    primary = Color(0xFF3A7BFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0044BB),
    onPrimaryContainer = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF444444)
)

@Composable
fun KateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorPalette
    ) {
        CompositionLocalProvider(LocalTextStyle provides TextStyle.Default.copy(fontFamily = Inter())){
            content()
        }
    }
}