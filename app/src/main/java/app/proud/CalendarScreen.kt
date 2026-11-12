package app.proud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    store: Store,
    data: AppData,
    onStart: (Template) -> Unit
) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selected by remember { mutableStateOf(LocalDate.now()) }
    var expanded by remember { mutableStateOf<String?>(null) }
    var showPlan by remember { mutableStateOf(false) }

    val sessionsByDay = remember(data.sessions) { data.sessions.groupBy { it.date.toEpochDay() } }
    val plansByDay = remember(data.plans) { data.plans.groupBy { it.epochDay } }
    val today = LocalDate.now()

    val monthSessions = data.sessions.filter { YearMonth.from(it.date) == month }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { month = month.minusMonths(1) }) { Text("◀", fontSize = 18.sp) }
                Text(
                    "${month.year}. " + month.month.getDisplayName(TextStyle.FULL, Locale("hu")),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                TextButton(onClick = { month = month.plusMonths(1) }) { Text("▶", fontSize = 18.sp) }
            }
        }

        item {
            AppCard {
                Row(Modifier.fillMaxWidth()) {
                    listOf("H", "K", "Sze", "Cs", "P", "Szo", "V").forEach {
                        Text(
                            it,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))

                val first = month.atDay(1)
                val offset = first.dayOfWeek.value - 1
                val days = month.lengthOfMonth()
                val rows = (offset + days + 6) / 7

                for (r in 0 until rows) {
                    Row(Modifier.fillMaxWidth()) {
                        for (c in 0 until 7) {
                            val dayNum = r * 7 + c - offset + 1
                            Box(
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNum in 1..days) {
                                    val date = month.atDay(dayNum)
                                    val epoch = date.toEpochDay()
                                    val done = sessionsByDay[epoch].orEmpty().isNotEmpty()
                                    val planned = plansByDay[epoch].orEmpty().isNotEmpty()
                                    val isSel = date == selected
                                    val isToday = date == today
                                    var m = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                    m = if (done) m.background(Orange) else m
                                    m = when {
                                        isSel -> m.border(2.dp, Color.White, CircleShape)
                                        isToday -> m.border(2.dp, Gold, CircleShape)
                                        planned && !done -> m.border(2.dp, Orange, CircleShape)
                                        else -> m
                                    }
                                    Box(m.clickable { selected = date }, contentAlignment = Alignment.Center) {
                                        Text(
                                            "$dayNum",
                                            color = if (done) OnAccent else TextMain,
                                            fontWeight = if (done || isToday) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Ebben a hónapban: ${monthSessions.size} edzés · ${fmtVolume(monthSessions.sumOf { it.volume })}",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }

        val selEpoch = selected.toEpochDay()
        val daySessions = sessionsByDay[selEpoch].orEmpty()
        val dayPlans = plansByDay[selEpoch].orEmpty()

        item {
            Text(
                "%d. %s %d.".format(
                    selected.year,
                    selected.month.getDisplayName(TextStyle.FULL, Locale("hu")),
                    selected.dayOfMonth
                ),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        }

        dayPlans.forEach { p ->
            val t = data.templates.firstOrNull { it.id == p.templateId }
            if (t != null) {
                item(key = "plan_${p.epochDay}_${p.templateId}") {
                    AppCard {
                        Text("📌 Tervezett", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("${t.emoji} ${t.name}", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextMain)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (selected == today && data.active == null) {
                                Button(onClick = { onStart(t) }) { Text("▶ Indítás") }
                            }
                            OutlinedButton(onClick = { store.removePlan(p.epochDay, p.templateId) }) { Text("Törlés") }
                        }
                    }
                }
            }
        }

        daySessions.forEach { s ->
            item(key = "sess_" + s.id) {
                val open = expanded == s.id
                AppCard(onClick = { expanded = if (open) null else s.id }) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("✅ ${s.name}", fontWeight = FontWeight.Bold, color = TextMain)
                        Text(if (open) "▲" else "▼", color = Muted)
                    }
                    Text(
                        "${s.setCount} szett · ${fmtVolume(s.volume)} · ${s.minutes} perc" +
                            (if (s.prCount > 0) " · 🏆 ${s.prCount}" else ""),
                        color = Muted,
                        fontSize = 12.sp
                    )
                    if (open) {
                        Spacer(Modifier.height(8.dp))
                        s.exercises.forEach { e ->
                            Text(ExerciseRepo.nameOf(e.exerciseId), color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                e.sets.joinToString("  ") { fmtKg(it.weight) + "×" + it.reps + (if (it.isPr) "🏆" else "") },
                                color = Muted,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        item {
            if (daySessions.isEmpty() && dayPlans.isEmpty()) {
                Text("Nincs edzés ezen a napon.", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = { showPlan = true }) { Text("+ Edzés tervezése erre a napra") }
        }
    }

    if (showPlan) {
        AlertDialog(
            onDismissRequest = { showPlan = false },
            containerColor = Surf,
            title = { Text("Melyik edzést tervezed?") },
            text = {
                Column {
                    data.templates.forEach { t ->
                        TextButton(onClick = {
                            store.addPlan(selected.toEpochDay(), t.id)
                            showPlan = false
                        }) { Text("${t.emoji} ${t.name}") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlan = false }) { Text("Mégse") }
            }
        )
    }
}
