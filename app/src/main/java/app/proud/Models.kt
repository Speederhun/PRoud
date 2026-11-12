package app.proud

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Exercise(
    val id: String,
    val name: String,
    val primary: List<String>,
    val secondary: List<String>,
    val equipment: String,
    val level: String,
    val mechanic: String,
    val instructions: List<String>,
    val images: List<String>
) {
    val huName: String get() = Hu.exerciseName(id) ?: name
    val imageUrls: List<String> get() = images.map { Hu.IMG_BASE + it }
}

data class TemplateItem(val exerciseId: String, val sets: Int, val reps: Int)

data class Template(
    val id: String,
    val name: String,
    val emoji: String,
    val items: List<TemplateItem>
)

data class SetEntry(val id: Long, val weight: Double, val reps: Int, val done: Boolean)

data class ActiveExercise(val exerciseId: String, val sets: List<SetEntry>)

data class ActiveWorkout(
    val startedAt: Long,
    val name: String,
    val exercises: List<ActiveExercise>
)

data class FinishedSet(val weight: Double, val reps: Int, val isPr: Boolean)

data class SessionExercise(val exerciseId: String, val sets: List<FinishedSet>)

data class Session(
    val id: String,
    val startedAt: Long,
    val endedAt: Long,
    val name: String,
    val exercises: List<SessionExercise>
) {
    val volume: Double get() = exercises.sumOf { e -> e.sets.sumOf { it.weight * it.reps } }
    val setCount: Int get() = exercises.sumOf { it.sets.size }
    val prCount: Int get() = exercises.sumOf { e -> e.sets.count { it.isPr } }
    val minutes: Int get() = ((endedAt - startedAt) / 60000L).toInt()
    val date: LocalDate get() = startedAt.toLocalDate()
}

data class Plan(val epochDay: Long, val templateId: String)

data class Settings(val weeklyGoal: Int = 3, val restSeconds: Int = 90)

data class AppData(
    val templates: List<Template>,
    val sessions: List<Session>,
    val plans: List<Plan>,
    val unlocked: Map<String, Long>,
    val settings: Settings,
    val active: ActiveWorkout?
)

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun fmtKg(w: Double): String =
    if (w == Math.floor(w)) w.toLong().toString() else String.format(java.util.Locale.US, "%.1f", w)

fun fmtVolume(v: Double): String {
    val n = Math.round(v)
    return if (n >= 10000) String.format(java.util.Locale.US, "%.1f t", n / 1000.0)
    else "$n kg"
}

fun fmtDuration(totalSeconds: Long): String {
    val s = if (totalSeconds < 0) 0 else totalSeconds
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) String.format(java.util.Locale.US, "%d:%02d:%02d", h, m, sec)
    else String.format(java.util.Locale.US, "%d:%02d", m, sec)
}

/** Becsült egyismétléses maximum (Epley). Testsúlyos (0 kg) esetben az ismétlésszám a pontszám. */
fun score(weight: Double, reps: Int): Double =
    if (weight > 0.0) (if (reps <= 1) weight else weight * (1.0 + reps / 30.0)) else reps.toDouble()
