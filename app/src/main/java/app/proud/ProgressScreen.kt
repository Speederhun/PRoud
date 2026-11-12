package app.proud

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

private val BODY_TYPES = listOf("Testsúly", "Mellkas", "Derék", "Bicepsz", "Comb")
private val METRICS = listOf("Max súly", "Becsült 1RM", "Volumen")

@Composable
fun ProgressScreen(store: Store, data: AppData) {
    val sessions = data.sessions

    val freq = remember(sessions) {
        sessions
            .flatMap { s -> s.exercises.map { it.exerciseId } }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
    }
    var selId by remember { mutableStateOf<String?>(null) }
    var metric by remember { mutableStateOf(1) }
    var weekOffset by remember { mutableStateOf(0) }
    var bodyType by remember { mutableStateOf("Testsúly") }
    var bodyText by remember { mutableStateOf("") }
    var resetKey by remember { mutableStateOf(0) }

    val cur = selId ?: freq.firstOrNull()

    val series = remember(sessions, cur, metric) {
        if (cur == null) {
            emptyList<Pair<LocalDate, Double>>()
        } else {
            sessions.mapNotNull { s ->
                val e = s.exercises.firstOrNull { it.exerciseId == cur }
                if (e == null) null
                else {
                    val v = when (metric) {
                        0 -> e.sets.maxOfOrNull { it.weight } ?: 0.0
                        1 -> e.sets.maxOfOrNull { score(it.weight, it.reps) } ?: 0.0
                        else -> e.sets.sumOf { it.weight * it.reps }
                    }
                    s.date to v
                }
            }.takeLast(20)
        }
    }

    val thisWeek = weekIndex(LocalDate.now().toEpochDay())
    val week = thisWeek - weekOffset
    val groupSets = remember(sessions, week) {
        val m = LinkedHashMap<String, Int>()
        Hu.groups.keys.forEach { m[it] = 0 }
        sessions.filter { weekIndex(it.date.toEpochDay()) == week }.forEach { s ->
            s.exercises.forEach { e ->
                val first = ExerciseRepo.get(e.exerciseId)?.primary?.firstOrNull()
                val g = if (first != null) Hu.groupOf(first) else null
                if (g != null) m[g] = (m[g] ?: 0) + e.sets.size
            }
        }
        m
    }

    val bodyUnit = if (bodyType == "Testsúly") " kg" else " cm"
    val bodyEntries = data.body.filter { it.type == bodyType }
    val today = LocalDate.now().toEpochDay()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Haladás", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Orange)
        }

        item {
            AppCard(Modifier.fillMaxWidth()) {
                Text("📈 Gyakorlat fejlődése", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                if (freq.isEmpty()) {
                    Text("Az első edzés után itt látod a súlyaid alakulását.", color = Muted, fontSize = 13.sp)
                } else {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        freq.take(12).forEach { id ->
                            Chip(ExerciseRepo.nameOf(id), id == cur) { selId = id }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        METRICS.forEachIndexed { i, label ->
                            Chip(label, metric == i) { metric = i }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LineChart(
                        values = series.map { it.second },
                        labels = series.map { "%02d.%02d.".format(it.first.monthValue, it.first.dayOfMonth) },
                        unit = " kg"
                    )
                    if (series.size >= 2) {
                        val first = series.first().second
                        val last = series.last().second
                        val diff = last - first
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${fmtKg(Math.round(first * 10) / 10.0)} → ${fmtKg(Math.round(last * 10) / 10.0)} kg (" +
                                (if (diff >= 0) "+" else "") + fmtKg(Math.round(diff * 10) / 10.0) + " kg)",
                            color = if (diff >= 0) Green else Danger,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        item {
            AppCard(Modifier.fillMaxWidth()) {
                Text("🧬 Heti izomcsoport-összesítő", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("Ez a hét", weekOffset == 0) { weekOffset = 0 }
                    Chip("Múlt hét", weekOffset == 1) { weekOffset = 1 }
                }
                Spacer(Modifier.height(10.dp))
                groupSets.forEach { (g, n) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(g, color = TextMain, fontSize = 13.sp, modifier = Modifier.width(64.dp))
                        Column(Modifier.weight(1f)) {
                            ProgressBar(
                                n / 10f,
                                if (n >= 10) Green else Orange,
                                8
                            )
                        }
                        Text(
                            if (n == 0) "lemaradt" else "$n szett",
                            color = if (n == 0) Danger else Muted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(68.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Cél: heti 10 szett izomcsoportonként.", color = Muted, fontSize = 11.sp)
            }
        }

        item {
            AppCard(Modifier.fillMaxWidth()) {
                Text("📏 Test adatok", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BODY_TYPES.forEach { t -> Chip(t, bodyType == t) { bodyType = t } }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    key(resetKey) {
                        NumField(
                            initial = "",
                            placeholder = if (bodyType == "Testsúly") "kg" else "cm",
                            decimal = true,
                            onValue = { bodyText = it },
                            modifier = Modifier.width(110.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(onClick = {
                        val v = bodyText.toDoubleOrNull()
                        if (v != null && v > 0.0) {
                            store.addBody(today, bodyType, v)
                            bodyText = ""
                            resetKey += 1
                        }
                    }) { Text("Mentés mára") }
                }
                Spacer(Modifier.height(12.dp))
                val shownEntries = bodyEntries.takeLast(30)
                LineChart(
                    values = shownEntries.map { it.value },
                    labels = shownEntries.map {
                        val d = LocalDate.ofEpochDay(it.epochDay)
                        "%02d.%02d.".format(d.monthValue, d.dayOfMonth)
                    },
                    color = Blue,
                    unit = bodyUnit
                )
                Spacer(Modifier.height(8.dp))
                bodyEntries.takeLast(5).reversed().forEach { b ->
                    val d = LocalDate.ofEpochDay(b.epochDay)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "%d.%02d.%02d.".format(d.year, d.monthValue, d.dayOfMonth),
                            color = Muted,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${fmtKg(b.value)}$bodyUnit", color = TextMain, fontWeight = FontWeight.SemiBold)
                        Text(
                            "✕",
                            color = Muted,
                            modifier = Modifier
                                .clickable { store.removeBody(b) }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
