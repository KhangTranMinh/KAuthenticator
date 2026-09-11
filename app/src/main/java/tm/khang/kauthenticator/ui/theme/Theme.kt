package tm.khang.kauthenticator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E5FF),
    onPrimaryContainer = Color(0xFF211A6A),
    secondary = Color(0xFF5B5F71),
    secondaryContainer = Color(0xFFE1E2F4),
    background = Color(0xFFF7F8FC),
    surface = Color(0xFFFDFBFF),
    surfaceVariant = Color(0xFFE7E7EF),
    outline = Color(0xFF777680),
    error = Color(0xFFBA1A1A),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC5C0FF),
    onPrimary = Color(0xFF282078),
    primaryContainer = Color(0xFF3F3895),
    onPrimaryContainer = Color(0xFFE7E5FF),
    secondary = Color(0xFFC5C5D9),
    secondaryContainer = Color(0xFF444654),
    background = Color(0xFF111116),
    surface = Color(0xFF19191F),
    surfaceVariant = Color(0xFF46464F),
    outline = Color(0xFF91909A),
    error = Color(0xFFFFB4AB),
)

@Composable
fun KAuthenticatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
