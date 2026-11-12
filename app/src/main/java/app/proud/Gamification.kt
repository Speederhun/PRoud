package app.proud

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Stats(
    val workouts: Int,
    val volume: Double,
    val sets: Int,
    val prs: Int,
    val streak: Int,
    val bestStreak: Int,
    val distinct: Int,
    val bestSessionVolume: Double,
    val longestMin: Int,
    val early: Int,
    val night: Int,
    val best: Map<String, Double>,
    val fullBodyWeeks: Int,
    val bigThree: Double,
    val maxSetsInDay: Int,
    val xp: Int,
    val workoutsThisWeek: Int
)

class Achievement(
    val id: String,
    val category: String,
    val emoji: String,
    val title: String,
    val desc: String,
    val target: Double,
    val tier: Int,
    val progress: (Stats) -> Double
) {
    val xp: Int
        get() = when (tier) {
            0 -> 50
            1 -> 100
            2 -> 200
            else -> 400
        }
}

fun sessionXp(s: Session): Int =
    50 + s.setCount * 5 + (s.volume / 100.0).toInt() + s.prCount * 25

fun weekIndex(epochDay: Long): Long = Math.floorDiv(epochDay + 3L, 7L)

private const val SQUAT_A = "Barbell_Full_Squat"
private const val SQUAT_B = "Barbell_Squat"
private const val BENCH = "Barbell_Bench_Press_-_Medium_Grip"
private const val DEAD = "Barbell_Deadlift"
private const val OHP = "Barbell_Shoulder_Press"

private fun bestOf(s: Stats, vararg ids: String): Double =
    ids.maxOf { s.best[it] ?: 0.0 }

val ACHIEVEMENTS: List<Achievement> = buildList {
    fun ach(
        id: String, cat: String, emoji: String, title: String, desc: String,
        target: Double, tier: Int, progress: (Stats) -> Double
    ) = add(Achievement(id, cat, emoji, title, desc, target, tier, progress))

    // Edzések száma
    ach("w1", "Edzések", "🔰", "Első vas", "Teljesítsd az első edzésed", 1.0, 0) { it.workouts.toDouble() }
    ach("w5", "Edzések", "🔥", "Bemelegítés", "5 edzés a naplóban", 5.0, 0) { it.workouts.toDouble() }
    ach("w10", "Edzések", "💪", "Rendszeres", "10 edzés a naplóban", 10.0, 1) { it.workouts.toDouble() }
    ach("w25", "Edzések", "🏅", "Törzsvendég", "25 edzés a naplóban", 25.0, 1) { it.workouts.toDouble() }
    ach("w50", "Edzések", "🥈", "Vasbarát", "50 edzés a naplóban", 50.0, 2) { it.workouts.toDouble() }
    ach("w100", "Edzések", "🏆", "Százas klub", "100 edzés a naplóban", 100.0, 3) { it.workouts.toDouble() }
    ach("w250", "Edzések", "👑", "Teremlakó", "250 edzés a naplóban", 250.0, 3) { it.workouts.toDouble() }

    // Összmozgatott súly
    ach("v1k", "Mozgatott súly", "⚖️", "Egy tonna", "Mozgass meg összesen 1 000 kg-ot", 1000.0, 0) { it.volume }
    ach("v10k", "Mozgatott súly", "🚚", "Teherautó", "Összesen 10 000 kg", 10000.0, 1) { it.volume }
    ach("v50k", "Mozgatott súly", "📦", "Konténer", "Összesen 50 000 kg", 50000.0, 2) { it.volume }
    ach("v100k", "Mozgatott súly", "🚢", "Teherhajó", "Összesen 100 000 kg", 100000.0, 2) { it.volume }
    ach("v500k", "Mozgatott súly", "🚀", "Rakéta", "Összesen 500 000 kg", 500000.0, 3) { it.volume }
    ach("v1m", "Mozgatott súly", "⛰️", "Hegymozgató", "Összesen 1 000 000 kg", 1000000.0, 3) { it.volume }

    // Szettek
    ach("s50", "Szettek", "🎯", "Ötven szett", "50 teljesített szett", 50.0, 0) { it.sets.toDouble() }
    ach("s250", "Szettek", "🎲", "Szettgyűjtő", "250 teljesített szett", 250.0, 1) { it.sets.toDouble() }
    ach("s1000", "Szettek", "🤖", "Szettgép", "1000 teljesített szett", 1000.0, 3) { it.sets.toDouble() }

    // Rekordok
    ach("pr1", "Rekordok", "🌟", "Első rekord", "Állíts fel egy új egyéni rekordot", 1.0, 0) { it.prs.toDouble() }
    ach("pr10", "Rekordok", "⭐", "Rekordvadász", "10 egyéni rekord", 10.0, 1) { it.prs.toDouble() }
    ach("pr25", "Rekordok", "💫", "Rekordgyár", "25 egyéni rekord", 25.0, 2) { it.prs.toDouble() }
    ach("pr100", "Rekordok", "🌠", "Csillagpor", "100 egyéni rekord", 100.0, 3) { it.prs.toDouble() }

    // Heti széria (a heti cél teljesítése egymás után)
    ach("st2", "Széria", "📆", "Lendületben", "2 hét egymás után a heti cél teljesítve", 2.0, 0) { it.bestStreak.toDouble() }
    ach("st4", "Széria", "🗓️", "Egy hónap vas", "4 hét egymás után a heti cél teljesítve", 4.0, 1) { it.bestStreak.toDouble() }
    ach("st8", "Széria", "🔥", "Megállíthatatlan", "8 hét egymás után", 8.0, 2) { it.bestStreak.toDouble() }
    ach("st12", "Széria", "☄️", "Negyedév", "12 hét egymás után", 12.0, 3) { it.bestStreak.toDouble() }
    ach("st26", "Széria", "🌋", "Fél év acél", "26 hét egymás után", 26.0, 3) { it.bestStreak.toDouble() }

    // Erő mérföldkövek
    ach("bn40", "Erő", "🛏️", "Fekvenyomó", "Fekvenyomás 40 kg-mal", 40.0, 0) { bestOf(it, BENCH) }
    ach("bn60", "Erő", "💥", "Hatvanas mell", "Fekvenyomás 60 kg-mal", 60.0, 1) { bestOf(it, BENCH) }
    ach("bn100", "Erő", "🦍", "Százas fekvő", "Fekvenyomás 100 kg-mal", 100.0, 3) { bestOf(it, BENCH) }
    ach("sq60", "Erő", "🦵", "Guggoló", "Guggolás 60 kg-mal", 60.0, 0) { bestOf(it, SQUAT_A, SQUAT_B) }
    ach("sq100", "Erő", "🏋️", "Százas guggolás", "Guggolás 100 kg-mal", 100.0, 2) { bestOf(it, SQUAT_A, SQUAT_B) }
    ach("sq140", "Erő", "🐘", "Elefántlábak", "Guggolás 140 kg-mal", 140.0, 3) { bestOf(it, SQUAT_A, SQUAT_B) }
    ach("dl80", "Erő", "🧲", "Felhúzó", "Felhúzás 80 kg-mal", 80.0, 0) { bestOf(it, DEAD) }
    ach("dl140", "Erő", "⚙️", "Erőgép", "Felhúzás 140 kg-mal", 140.0, 2) { bestOf(it, DEAD) }
    ach("dl200", "Erő", "🦖", "Kétszázas húzás", "Felhúzás 200 kg-mal", 200.0, 3) { bestOf(it, DEAD) }
    ach("ohp40", "Erő", "🙌", "Vállból nyomó", "Vállból nyomás 40 kg-mal", 40.0, 1) { bestOf(it, OHP) }
    ach("big300", "Erő", "🔱", "Big Three 300", "A guggolás + fekvenyomás + felhúzás legjobbjai együtt: 300 kg", 300.0, 1) { it.bigThree }
    ach("big400", "Erő", "⚔️", "Big Three 400", "A három nagy gyakorlat összesen: 400 kg", 400.0, 2) { it.bigThree }
    ach("big500", "Erő", "🐉", "Big Three 500", "A három nagy gyakorlat összesen: 500 kg", 500.0, 3) { it.bigThree }

    // Változatosság
    ach("x5", "Változatosság", "🎨", "Kísérletező", "5 különböző gyakorlat kipróbálva", 5.0, 0) { it.distinct.toDouble() }
    ach("x15", "Változatosság", "🧰", "Eszköztár", "15 különböző gyakorlat", 15.0, 1) { it.distinct.toDouble() }
    ach("x40", "Változatosság", "📚", "Gyakorlat-enciklopédia", "40 különböző gyakorlat", 40.0, 2) { it.distinct.toDouble() }

    // Különleges
    ach("t1", "Különleges", "🐜", "Tonna egy menetben", "Egyetlen edzésen mozgass meg 1 000 kg-ot", 1000.0, 1) { it.bestSessionVolume }
    ach("t5", "Különleges", "🐋", "Öt tonna", "Egyetlen edzésen mozgass meg 5 000 kg-ot", 5000.0, 2) { it.bestSessionVolume }
    ach("long", "Különleges", "⏱️", "Maraton", "Egy edzés, ami legalább 90 percig tart", 90.0, 1) { it.longestMin.toDouble() }
    ach("early", "Különleges", "🌅", "Korán kelő", "Kezdj edzést reggel 7 óra előtt", 1.0, 1) { it.early.toDouble() }
    ach("night", "Különleges", "🦉", "Éjjeli bagoly", "Kezdj edzést este 9 óra után", 1.0, 1) { it.night.toDouble() }
    ach("day25", "Különleges", "💣", "Szetthalmozó", "25 szett egyetlen napon", 25.0, 2) { it.maxSetsInDay.toDouble() }
    ach("fb", "Különleges", "🧬", "Teljes test hete", "Egy héten edzd meg a mellet, hátat, lábat, vállat, kart és core-t", 1.0, 2) { it.fullBodyWeeks.toDouble() }
}

val ACH_BY_ID: Map<String, Achievement> = ACHIEVEMENTS.associateBy { it.id }

fun computeStats(data: AppData): Stats {
    val goal = data.settings.weeklyGoal
    var volume = 0.0
    var sets = 0
    var prs = 0
    var bestSessionVolume = 0.0
    var longest = 0
    var early = 0
    var night = 0
    var xp = 0
    val distinct = HashSet<String>()
    val best = HashMap<String, Double>()
    val setsPerDay = HashMap<Long, Int>()
    val weekCounts = HashMap<Long, Int>()
    val weekGroups = HashMap<Long, HashSet<String>>()

    for (s in data.sessions) {
        volume += s.volume
        sets += s.setCount
        prs += s.prCount
        if (s.volume > bestSessionVolume) bestSessionVolume = s.volume
        if (s.minutes > longest) longest = s.minutes
        val zdt = Instant.ofEpochMilli(s.startedAt).atZone(ZoneId.systemDefault())
        if (zdt.hour < 7) early++
        if (zdt.hour >= 21) night++
        val day = zdt.toLocalDate().toEpochDay()
        setsPerDay[day] = (setsPerDay[day] ?: 0) + s.setCount
        val week = weekIndex(day)
        weekCounts[week] = (weekCounts[week] ?: 0) + 1
        for (e in s.exercises) {
            distinct.add(e.exerciseId)
            for (st in e.sets) {
                if (st.weight > (best[e.exerciseId] ?: 0.0)) best[e.exerciseId] = st.weight
            }
            val ex = ExerciseRepo.get(e.exerciseId)
            if (ex != null) {
                val set = weekGroups.getOrPut(week) { HashSet() }
                for (m in ex.primary) {
                    val g = Hu.groupOf(m)
                    if (g != null) set.add(g)
                }
            }
        }
        xp += sessionXp(s)
    }

    for (id in data.unlocked.keys) xp += ACH_BY_ID[id]?.xp ?: 0
    for (k in data.challenges) xp += challengeXp(k)

    val okWeeks = weekCounts.filter { it.value >= goal }.keys
    val thisWeek = weekIndex(LocalDate.now().toEpochDay())
    var cur = if (okWeeks.contains(thisWeek)) thisWeek else thisWeek - 1
    var streak = 0
    while (okWeeks.contains(cur)) {
        streak++
        cur--
    }
    var bestStreak = 0
    var run = 0
    var prev = Long.MIN_VALUE
    for (w in okWeeks.sorted()) {
        run = if (prev != Long.MIN_VALUE && w == prev + 1) run + 1 else 1
        if (run > bestStreak) bestStreak = run
        prev = w
    }

    val squat = maxOf(best[SQUAT_A] ?: 0.0, best[SQUAT_B] ?: 0.0)
    val bigThree = squat + (best[BENCH] ?: 0.0) + (best[DEAD] ?: 0.0)

    return Stats(
        workouts = data.sessions.size,
        volume = volume,
        sets = sets,
        prs = prs,
        streak = streak,
        bestStreak = maxOf(bestStreak, streak),
        distinct = distinct.size,
        bestSessionVolume = bestSessionVolume,
        longestMin = longest,
        early = early,
        night = night,
        best = best,
        fullBodyWeeks = weekGroups.count { it.value.size >= 6 },
        bigThree = bigThree,
        maxSetsInDay = setsPerDay.values.maxOrNull() ?: 0,
        xp = xp,
        workoutsThisWeek = weekCounts[thisWeek] ?: 0
    )
}

data class LevelInfo(val level: Int, val title: String, val xpInto: Int, val xpNeeded: Int)

fun levelTitle(level: Int): String = when {
    level >= 25 -> "Legenda"
    level >= 18 -> "Bajnok"
    level >= 14 -> "Erőgép"
    level >= 10 -> "Vasmarok"
    level >= 7 -> "Súlyemelő"
    level >= 5 -> "Lelkes"
    level >= 3 -> "Vasbarát"
    level >= 2 -> "Kezdő"
    else -> "Újonc"
}

fun levelInfo(xp: Int): LevelInfo {
    val level = 1 + Math.floor(Math.sqrt(xp / 150.0)).toInt()
    val base = 150 * (level - 1) * (level - 1)
    val next = 150 * level * level
    return LevelInfo(level, levelTitle(level), xp - base, next - base)
}
