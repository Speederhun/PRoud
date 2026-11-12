package app.proud

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Egyszerű vonaldiagram kitöltéssel. A labels és values azonos hosszúságú. */
@Composable
fun LineChart(
    values: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    color: Color = Orange,
    unit: String = ""
) {
    if (values.size < 2) {
        Text(
            if (values.isEmpty()) "Még nincs adat." else "Legalább két mérés kell a grafikonhoz.",
            color = Muted,
            fontSize = 13.sp
        )
    } else {
        val minV = values.minOrNull() ?: 0.0
        val maxV = values.maxOrNull() ?: 1.0
        val span = if (maxV - minV < 1e-9) 1.0 else maxV - minV
        Column(modifier) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("max ${fmtKg(maxV)}$unit", color = Muted, fontSize = 11.sp)
                Text("min ${fmtKg(minV)}$unit", color = Muted, fontSize = 11.sp)
            }
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val w = size.width
                val h = size.height
                val padY = 12f
                val n = values.size

                fun px(i: Int): Float = w * i / (n - 1).toFloat()
                fun py(v: Double): Float =
                    (padY + (h - 2f * padY) * (1.0 - (v - minV) / span)).toFloat()

                for (g in 0..2) {
                    val gy = padY + (h - 2f * padY) * g / 2f
                    drawLine(Outline, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                }

                val line = Path()
                val fill = Path()
                for (i in 0 until n) {
                    val x = px(i)
                    val y = py(values[i])
                    if (i == 0) {
                        line.moveTo(x, y)
                        fill.moveTo(x, h)
                        fill.lineTo(x, y)
                    } else {
                        line.lineTo(x, y)
                        fill.lineTo(x, y)
                    }
                }
                fill.lineTo(w, h)
                fill.close()

                drawPath(fill, brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.35f), Color.Transparent)))
                drawPath(
                    line,
                    color = color,
                    style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                for (i in 0 until n) {
                    drawCircle(
                        color = color,
                        radius = if (i == n - 1) 9f else 5f,
                        center = Offset(px(i), py(values[i]))
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(labels.firstOrNull() ?: "", color = Muted, fontSize = 11.sp)
                Text(labels.lastOrNull() ?: "", color = Muted, fontSize = 11.sp)
            }
        }
    }
}
