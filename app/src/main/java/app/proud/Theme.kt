package app.proud

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

class Palette(
    val bg: Color,
    val surf: Color,
    val surf2: Color,
    val accent: Color,
    val onAccent: Color,
    val gold: Color,
    val green: Color,
    val blue: Color,
    val muted: Color,
    val text: Color,
    val danger: Color,
    val outline: Color
)

private val PremiumPalette = Palette(
    bg = Color(0xFF07080B),
    surf = Color(0xFF12141A),
    surf2 = Color(0xFF1D2028),
    accent = Color(0xFFFF7A3D),
    onAccent = Color(0xFF0B0B0D),
    gold = Color(0xFFFFC857),
    green = Color(0xFF3DDC97),
    blue = Color(0xFF4DA3FF),
    muted = Color(0xFF9AA0AC),
    text = Color(0xFFF2F3F5),
    danger = Color(0xFFFF5C5C),
    outline = Color(0x24FFFFFF)
)

private val GamerPalette = Palette(
    bg = Color(0xFF0A0618),
    surf = Color(0xFF181033),
    surf2 = Color(0xFF2B1F5E),
    accent = Color(0xFF8B5CF6),
    onAccent = Color.White,
    gold = Color(0xFFFFD34D),
    green = Color(0xFF39FFB0),
    blue = Color(0xFF38BDF8),
    muted = Color(0xFFA79FCB),
    text = Color(0xFFF4F1FF),
    danger = Color(0xFFFF5C8A),
    outline = Color(0x558B5CF6)
)

/** A kiválasztott stílus: "premium" vagy "gamer". Compose-állapot, ezért a felület azonnal frissül. */
object ThemeState {
    var style by mutableStateOf("premium")
    val gamer: Boolean get() = style == "gamer"
}

private fun pal(): Palette = if (ThemeState.gamer) GamerPalette else PremiumPalette

val Bg: Color get() = pal().bg
val Surf: Color get() = pal().surf
val Surf2: Color get() = pal().surf2
val Orange: Color get() = pal().accent
val OnAccent: Color get() = pal().onAccent
val Gold: Color get() = pal().gold
val Green: Color get() = pal().green
val Blue: Color get() = pal().blue
val Muted: Color get() = pal().muted
val TextMain: Color get() = pal().text
val Danger: Color get() = pal().danger
val Outline: Color get() = pal().outline

fun tierColor(tier: Int): Color = when (tier) {
    0 -> Color(0xFFCD7F32)
    1 -> Color(0xFFC0C7D1)
    2 -> Gold
    else -> Color(0xFFB66DFF)
}

@Composable
fun ProudTheme(content: @Composable () -> Unit) {
    val p = pal()
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = p.accent,
            onPrimary = p.onAccent,
            secondary = p.gold,
            onSecondary = Color.Black,
            background = p.bg,
            onBackground = p.text,
            surface = p.surf,
            onSurface = p.text,
            surfaceVariant = p.surf2,
            onSurfaceVariant = p.muted,
            outline = Color(0xFF3A3E4A)
        ),
        content = content
    )
}
