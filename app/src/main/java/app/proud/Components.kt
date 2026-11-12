package app.proud

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.withFrameNanos
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.random.Random

fun vibrate(ctx: Context, long: Boolean = false) {
    try {
        val v = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        val effect = if (long) {
            VibrationEffect.createWaveform(longArrayOf(0, 250, 120, 250, 120, 400), -1)
        } else {
            VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        v.vibrate(effect)
    } catch (e: Exception) {
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val gamer = ThemeState.gamer
    val shape = RoundedCornerShape(if (gamer) 14.dp else 20.dp)
    var m = modifier.clip(shape)
    m = if (gamer) {
        m.background(Brush.verticalGradient(listOf(Surf2, Surf)))
    } else {
        m.background(Surf)
    }
    m = m.border(1.dp, Outline, shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(m.padding(14.dp), content = content)
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .clip(shape)
            .background(if (selected) Orange else Surf2)
            .border(1.dp, if (selected) Orange else Outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            color = if (selected) OnAccent else TextMain,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun Tag(text: String, color: Color = Surf2, textColor: Color = TextMain) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = textColor, fontSize = 11.sp)
    }
}

@Composable
fun ProgressBar(fraction: Float, color: Color = Orange, height: Int = 8, modifier: Modifier = Modifier) {
    val f = fraction.coerceIn(0f, 1f)
    Box(
        modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(50))
            .background(Surf2)
    ) {
        val fillMod = Modifier
            .fillMaxHeight()
            .fillMaxWidth(f)
            .clip(RoundedCornerShape(50))
        Box(
            if (ThemeState.gamer && color == Orange) {
                fillMod.background(Brush.horizontalGradient(listOf(Orange, Color(0xFFFF4FD8))))
            } else {
                fillMod.background(color)
            }
        )
    }
}

@Composable
fun Stepper(
    value: Int,
    onChange: (Int) -> Unit,
    min: Int,
    max: Int,
    step: Int = 1,
    suffix: String = ""
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepButton("−") { onChange((value - step).coerceAtLeast(min)) }
        Text(
            "$value$suffix",
            color = TextMain,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        StepButton("+") { onChange((value + step).coerceAtMost(max)) }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Surf2)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ExerciseThumb(ex: Exercise?, size: Int = 56) {
    Box(
        Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        val url = ex?.imageUrls?.firstOrNull()
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Két képkocka váltogatásával animált gyakorlat-bemutató. */
@Composable
fun ExerciseDemo(urls: List<String>, modifier: Modifier = Modifier) {
    var idx by remember(urls) { mutableIntStateOf(0) }
    LaunchedEffect(urls) {
        while (urls.size > 1) {
            delay(900)
            idx = (idx + 1) % urls.size
        }
    }
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        urls.forEachIndexed { i, u ->
            val a by animateFloatAsState(
                targetValue = if (i == idx) 1f else 0f,
                animationSpec = tween(300),
                label = "demo$i"
            )
            AsyncImage(
                model = u,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(a)
            )
        }
    }
}

@Composable
fun BouncyEmoji(emoji: String, size: TextUnit) {
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { go = true }
    val scale by animateFloatAsState(
        targetValue = if (go) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bounce"
    )
    Text(
        emoji,
        fontSize = size,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}

private class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    var rot: Float,
    val vr: Float
)

@Composable
fun Confetti(modifier: Modifier = Modifier, count: Int = 140) {
    val colors = remember { listOf(Orange, Gold, Green, Blue, Color(0xFFFF4D8D), Color.White) }
    val particles = remember { ArrayList<Particle>() }
    var frame by remember { mutableLongStateOf(0L) }
    var area by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(area) {
        if (area.width == 0 || area.height == 0) return@LaunchedEffect
        val w = area.width.toFloat()
        val h = area.height.toFloat()
        particles.clear()
        repeat(count) {
            particles.add(
                Particle(
                    x = Random.nextFloat() * w,
                    y = -Random.nextFloat() * h * 0.6f,
                    vx = (Random.nextFloat() - 0.5f) * 4f,
                    vy = 5f + Random.nextFloat() * 9f,
                    size = 14f + Random.nextFloat() * 18f,
                    color = colors[Random.nextInt(colors.size)],
                    rot = Random.nextFloat() * 360f,
                    vr = (Random.nextFloat() - 0.5f) * 12f
                )
            )
        }
        var last = withFrameNanos { it }
        val startTime = last
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - last) / 16_666_667f).coerceIn(0f, 3f)
            last = now
            val spawning = (now - startTime) < 4_000_000_000L
            for (p in particles) {
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.rot += p.vr * dt
                if (p.y > h + 40f && spawning) {
                    p.y = -20f
                    p.x = Random.nextFloat() * w
                }
            }
            frame = now
        }
    }

    Canvas(modifier.onSizeChanged { area = it }) {
        val tick = frame
        if (tick == Long.MIN_VALUE) return@Canvas
        for (p in particles) {
            if (p.y > size.height + 40f) continue
            rotate(p.rot, pivot = Offset(p.x, p.y)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(p.x - p.size / 2f, p.y - p.size / 4f),
                    size = Size(p.size, p.size / 2f)
                )
            }
        }
    }
}

fun fmtNum(d: Double): String =
    if (d >= 1000.0) String.format(java.util.Locale("hu"), "%,d", d.toLong()) else fmtKg(d)

@Composable
fun LevelBadge(level: Int, size: Int = 56) {
    val gamer = ThemeState.gamer
    var m = Modifier
        .size(size.dp)
        .clip(CircleShape)
    m = if (gamer) {
        m.background(Brush.verticalGradient(listOf(Orange, Color(0xFFFF4FD8))))
            .border(3.dp, Gold, CircleShape)
    } else {
        m.background(Orange)
    }
    Box(m, contentAlignment = Alignment.Center) {
        Text(
            "$level",
            color = OnAccent,
            fontSize = (size / 2.3f).sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
