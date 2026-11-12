package app.proud

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** A rekordfal PNG képpé alakítása és megosztása. */
object ShareCard {
    private fun fit(s: String, n: Int): String = if (s.length > n) s.take(n - 1) + "…" else s

    fun share(ctx: Context, rows: List<RecordRow>, levelText: String) {
        val w = 1080
        val rowH = 124
        val h = 380 + rows.size * rowH + 150
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val bold = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), Bg.toArgb(), Surf2.toArgb(), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        p.shader = null

        p.typeface = bold
        p.color = Orange.toArgb()
        p.textSize = 120f
        c.drawText("PRoud", 70f, 175f, p)
        p.color = TextMain.toArgb()
        p.textSize = 60f
        c.drawText("Személyes rekordjaim", 70f, 262f, p)
        p.typeface = Typeface.DEFAULT
        p.color = Muted.toArgb()
        p.textSize = 42f
        c.drawText(levelText, 70f, 325f, p)

        var y = 380f
        rows.forEachIndexed { i, r ->
            p.color = Surf.toArgb()
            c.drawRoundRect(RectF(50f, y, w - 50f, y + rowH - 16f), 28f, 28f, p)

            p.typeface = bold
            p.color = Gold.toArgb()
            p.textSize = 54f
            c.drawText("${i + 1}", 90f, y + 72f, p)

            p.color = TextMain.toArgb()
            p.textSize = 42f
            c.drawText(fit(ExerciseRepo.nameOf(r.exerciseId), 24), 175f, y + 54f, p)

            p.typeface = Typeface.DEFAULT
            p.color = Muted.toArgb()
            p.textSize = 32f
            c.drawText("becsült 1RM: ${fmtKg(Math.round(r.oneRm).toDouble())} kg", 175f, y + 92f, p)

            p.typeface = bold
            p.color = Gold.toArgb()
            p.textSize = 46f
            p.textAlign = Paint.Align.RIGHT
            c.drawText("${fmtKg(r.weight)} kg × ${r.reps}", w - 90f, y + 72f, p)
            p.textAlign = Paint.Align.LEFT

            y += rowH
        }

        p.typeface = Typeface.DEFAULT
        p.color = Muted.toArgb()
        p.textSize = 36f
        c.drawText("PRoud · minden ismétlés számít", 70f, h - 70f, p)

        val dir = File(ctx.cacheDir, "share")
        dir.mkdirs()
        val file = File(dir, "proud_rekordok.png")
        FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }

        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, "Rekordok megosztása")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(chooser)
    }
}
