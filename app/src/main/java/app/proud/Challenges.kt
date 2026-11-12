package app.proud

import java.time.Instant
import java.time.ZoneId

data class Challenge(
    val key: String,
    val emoji: String,
    val title: String,
    val current: Double,
    val target: Double,
    val xp: Int
) {
    val done: Boolean get() = current >= target
}

private class WeekStats(
    val workouts: Int,
    val volume: Double,
    val sets: Int,
    val prs: Int,
    val groups: Int,
    val newExercises: Int,
    val early: Int
)

private fun weekStats(week: Long, data: AppData): WeekStats {
    val inWeek = data.sessions.filter { weekIndex(it.date.toEpochDay()) == week }
    val earlier = HashSet<String>()
    for (s in data.sessions) {
        if (weekIndex(s.date.toEpochDay()) < week) {
            for (e in s.exercises) earlier.add(e.exerciseId)
        }
    }
    val newEx = HashSet<String>()
    val groups = HashSet<String>()
    var early = 0
    for (s in inWeek) {
        val hour = Instant.ofEpochMilli(s.startedAt).atZone(ZoneId.systemDefault()).hour
        if (hour < 8) early++
        for (e in s.exercises) {
            if (!earlier.contains(e.exerciseId)) newEx.add(e.exerciseId)
            val ex = ExerciseRepo.get(e.exerciseId)
            if (ex != null) {
                for (m in ex.primary) {
                    val g = Hu.groupOf(m)
                    if (g != null) groups.add(g)
                }
            }
        }
    }
    return WeekStats(
        workouts = inWeek.size,
        volume = inWeek.sumOf { it.volume },
        sets = inWeek.sumOf { it.setCount },
        prs = inWeek.sumOf { it.prCount },
        groups = groups.size,
        newExercises = newEx.size,
        early = early
    )
}

/** A hét 3 kihívása (hétről hétre forognak a készletből). */
fun challengesFor(week: Long, data: AppData): List<Challenge> {
    val w = weekStats(week, data)
    val goal = data.settings.weeklyGoal

    fun mk(id: Int, emoji: String, title: String, cur: Double, target: Double, xp: Int) =
        Challenge("$week|$id|$xp", emoji, title, cur, target, xp)

    val pool = listOf(
        mk(0, "📅", "Edzz $goal-szor ezen a héten", w.workouts.toDouble(), goal.toDouble(), 75),
        mk(1, "⚖️", "Mozgass meg 5 000 kg-ot a héten", w.volume, 5000.0, 75),
        mk(2, "🏆", "Állíts fel 2 új rekordot", w.prs.toDouble(), 2.0, 100),
        mk(3, "🎯", "Teljesíts 40 szettet a héten", w.sets.toDouble(), 40.0, 75),
        mk(4, "🧬", "Edzd meg mind a 6 izomcsoportot", w.groups.toDouble(), 6.0, 125),
        mk(5, "🎨", "Próbálj ki 3 új gyakorlatot", w.newExercises.toDouble(), 3.0, 100),
        mk(6, "🌅", "Edzz egyszer reggel 8 óra előtt", w.early.toDouble(), 1.0, 75)
    )
    return List(3) { k -> pool[Math.floorMod(week * 3L + k.toLong(), 7L).toInt()] }
}

fun challengeXp(key: String): Int = key.substringAfterLast('|').toIntOrNull() ?: 0

/** Egy gyakorlat legjobb szettje (rekordfalhoz és megosztáshoz). */
data class RecordRow(
    val exerciseId: String,
    val weight: Double,
    val reps: Int,
    val oneRm: Double,
    val epochDay: Long
)

fun recordRows(data: AppData, limit: Int = 10): List<RecordRow> {
    val best = HashMap<String, RecordRow>()
    for (s in data.sessions) {
        for (e in s.exercises) {
            for (st in e.sets) {
                if (st.weight <= 0.0) continue
                val sc = score(st.weight, st.reps)
                val cur = best[e.exerciseId]
                if (cur == null || sc > cur.oneRm) {
                    best[e.exerciseId] = RecordRow(e.exerciseId, st.weight, st.reps, sc, s.date.toEpochDay())
                }
            }
        }
    }
    return best.values.sortedByDescending { it.oneRm }.take(limit)
}
