package app.proud

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF0E0F13)
val Surf = Color(0xFF181A21)
val Surf2 = Color(0xFF242732)
val Orange = Color(0xFFFF6B2C)
val Gold = Color(0xFFFFC857)
val Green = Color(0xFF3DDC97)
val Blue = Color(0xFF4DA3FF)
val Muted = Color(0xFF9AA0AC)
val TextMain = Color(0xFFECEDF0)
val Danger = Color(0xFFFF5C5C)

fun tierColor(tier: Int): Color = when (tier) {
    0 -> Color(0xFFCD7F32)
    1 -> Color(0xFFC0C7D1)
    2 -> Gold
    else -> Color(0xFFB66DFF)
}

@Composable
fun ProudTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Orange,
            onPrimary = Color.Black,
            secondary = Gold,
            onSecondary = Color.Black,
            background = Bg,
            onBackground = TextMain,
            surface = Surf,
            onSurface = TextMain,
            surfaceVariant = Surf2,
            onSurfaceVariant = Muted,
            outline = Color(0xFF3A3E4A)
        ),
        content = content
    )
}
