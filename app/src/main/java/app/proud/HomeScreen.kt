package app.proud

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun HomeScreen(
    store: Store,
    data: AppData,
    onStarted: () -> Unit,
    onEdit: (String?) -> Unit
) {
    val stats = remember(data) { computeStats(data) }
    val li = levelInfo(stats.xp)
    val chs = remember(data) { challengesFor(weekIndex(LocalDate.now().toEpochDay()), data) }
    var pending by remember { mutableStateOf<Template?>(null) }
    var selGroup by remember { mutableStateOf("") }

    fun start(t: Template) {
        store.startWorkout(t.name, t.items)
        onStarted()
    }

    fun tryStart(t: Template) {
        if (data.active != null) pending = t else start(t)
    }

    val shown = when (selGroup) {
        "" -> data.templates
        "Saját" -> data.templates.filter { it.group.isEmpty() }
        else -> data.templates.filter { it.group == selGroup }
    }

    val today = LocalDate.now().toEpochDay()
    val plannedToday = data.plans
        .filter { it.epochDay == today }
        .mapNotNull { p -> data.templates.firstOrNull { it.id == p.templateId } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("PRoud", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Orange)
            Text("Minden ismétlés számít.", color = Muted, fontSize = 13.sp)
        }

        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelBadge(li.level, if (ThemeState.gamer) 64 else 56)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(li.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        Text("${li.xpInto} / ${li.xpNeeded} XP a következő szintig", color = Muted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                ProgressBar(li.xpInto.toFloat() / li.xpNeeded.toFloat(), Gold, 10)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBlock("🔥", "${stats.streak} hét", "széria")
                    StatBlock("📅", "${stats.workoutsThisWeek}/${data.settings.weeklyGoal}", "e héten")
                    StatBlock("🏆", "${data.unlocked.size}/${ACHIEVEMENTS.size}", "achievement")
                }
            }
        }

        item {
            AppCard(Modifier.fillMaxWidth()) {
                Text("🎯 Heti kihívások", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
                Spacer(Modifier.height(6.dp))
                chs.forEach { c ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 5.dp)
                    ) {
                        Text(c.emoji, fontSize = 20.sp)
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(c.title, color = if (c.done) Green else TextMain, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            ProgressBar((c.current / c.target).toFloat(), if (c.done) Green else Orange, 6)
                        }
                        Text(
                            if (c.done) "✔ +${c.xp} XP" else "${fmtNum(Math.min(c.current, c.target))}/${fmtNum(c.target)}",
                            color = if (c.done) Green else Muted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        val active = data.active
        if (active != null) {
            item {
                AppCard(onClick = onStarted) {
                    Text("▶ Folyamatban lévő edzés", color = Orange, fontWeight = FontWeight.Bold)
                    Text(active.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextMain)
                    Text("Koppints a folytatáshoz", color = Muted, fontSize = 12.sp)
                }
            }
        }

        items(plannedToday, key = { "plan_" + it.id }) { t ->
            AppCard {
                Text("📌 Mai terv", color = Gold, fontWeight = FontWeight.Bold)
                Text("${t.emoji} ${t.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { tryStart(t) }) { Text("Indítás") }
            }
        }

        item {
            Text("Edzéseid", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip("Mind", selGroup == "") { selGroup = "" }
                Hu.templateGroups.forEach { g ->
                    Chip(Hu.groupEmoji(g) + " " + g, selGroup == g) { selGroup = if (selGroup == g) "" else g }
                }
                Chip("Saját", selGroup == "Saját") { selGroup = if (selGroup == "Saját") "" else "Saját" }
            }
        }

        if (shown.isEmpty()) {
            item {
                Text("Ebben a csoportban még nincs edzés. Hozz létre egyet lent.", color = Muted, fontSize = 13.sp)
            }
        }

        items(shown, key = { it.id }) { t ->
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t.emoji, fontSize = 34.sp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(t.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        val sets = t.items.sumOf { it.sets }
                        Text(
                            "${t.items.size} gyakorlat · $sets szett · ~${(sets * 5 + 1) / 2} perc",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    t.items.joinToString(" · ") { ExerciseRepo.nameOf(it.exerciseId) },
                    color = Muted,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { tryStart(t) }) { Text("▶ Indítás") }
                    OutlinedButton(onClick = { onEdit(t.id) }) { Text("Szerkesztés") }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onEdit(null) }) { Text("+ Új edzésterv") }
                OutlinedButton(onClick = {
                    if (data.active != null) {
                        pending = Template("free", "Szabad edzés", "🏋️", emptyList())
                    } else {
                        start(Template("free", "Szabad edzés", "🏋️", emptyList()))
                    }
                }) { Text("Szabad edzés") }
            }
        }

        if (data.sessions.isNotEmpty()) {
            item {
                Text("Legutóbbi edzések", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextMain)
            }
            items(data.sessions.takeLast(5).reversed(), key = { "s" + it.id }) { s ->
                AppCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(s.name, fontWeight = FontWeight.Bold, color = TextMain)
                        Text(
                            "%d.%02d.".format(s.date.monthValue, s.date.dayOfMonth),
                            color = Muted
                        )
                    }
                    Text(
                        "${s.setCount} szett · ${fmtVolume(s.volume)} · ${s.minutes} perc" +
                            (if (s.prCount > 0) " · 🏆 ${s.prCount}" else ""),
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    val p = pending
    if (p != null) {
        AlertDialog(
            onDismissRequest = { pending = null },
            containerColor = Surf,
            title = { Text("Van folyamatban lévő edzés") },
            text = { Text("Az új edzés indítása elveti a jelenlegit. Folytatod?") },
            confirmButton = {
                TextButton(onClick = {
                    pending = null
                    store.discardActive()
                    start(p)
                }) { Text("Új edzés") }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text("Mégse") }
            }
        )
    }
}

@Composable
private fun StatBlock(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(value, fontWeight = FontWeight.Bold, color = TextMain)
        Text(label, color = Muted, fontSize = 11.sp)
    }
}
