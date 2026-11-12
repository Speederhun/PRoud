package app.proud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LibraryScreen(onOpen: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text(
            "Gyakorlatok",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Orange,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        ExerciseBrowser(Modifier.fillMaxSize()) { onOpen(it.id) }
    }
}

@Composable
fun ExerciseBrowser(modifier: Modifier = Modifier, onPick: (Exercise) -> Unit) {
    var query by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }
    var equip by remember { mutableStateOf("") }

    val results = remember(query, group, equip) {
        val q = query.trim()
        ExerciseRepo.all.filter { ex ->
            (q.isEmpty() || ex.huName.contains(q, ignoreCase = true) || ex.name.contains(q, ignoreCase = true)) &&
                (group.isEmpty() || ex.primary.any { Hu.groupOf(it) == group }) &&
                (equip.isEmpty() ||
                    (if (equip == "other") ex.equipment !in Hu.mainEquipment else ex.equipment == equip))
        }
    }

    Column(modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Keresés (pl. guggolás, bench, curl)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Chip("Mind", group.isEmpty()) { group = "" } }
            items(Hu.groups.keys.toList()) { g ->
                Chip(g, group == g) { group = if (group == g) "" else g }
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Chip("Minden eszköz", equip.isEmpty()) { equip = "" } }
            items(Hu.mainEquipment) { e ->
                Chip(Hu.equipment(e), equip == e) { equip = if (equip == e) "" else e }
            }
            item { Chip("Egyéb", equip == "other") { equip = if (equip == "other") "" else "other" } }
        }
        Text(
            "${results.size} gyakorlat",
            color = Muted,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results, key = { it.id }) { ex ->
                ExerciseRow(ex) { onPick(ex) }
            }
        }
    }
}

@Composable
fun ExerciseRow(ex: Exercise, onClick: () -> Unit) {
    AppCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExerciseThumb(ex, 56)
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(ex.huName, fontWeight = FontWeight.Bold, color = TextMain, fontSize = 15.sp)
                if (ex.huName != ex.name) {
                    Text(ex.name, color = Muted, fontSize = 11.sp)
                }
                Text(
                    ex.primary.joinToString(", ") { Hu.muscle(it) } + " · " + Hu.equipment(ex.equipment),
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ExercisePickerDialog(onPick: (Exercise) -> Unit, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Gyakorlat választása",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "✕",
                        color = Muted,
                        fontSize = 22.sp,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(8.dp)
                    )
                }
                ExerciseBrowser(Modifier.fillMaxSize(), onPick = onPick)
            }
        }
    }
}

@Composable
fun ExerciseDetailDialog(data: AppData, id: String, onDismiss: () -> Unit) {
    val ex = ExerciseRepo.get(id)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            if (ex == null) {
                Column(Modifier.padding(24.dp)) {
                    Text("A gyakorlat nem található.", color = TextMain)
                    Text("Bezár", color = Orange, modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp))
                }
            } else {
                val tips = Hu.tips(ex.id)
                val history = remember(data.sessions, ex.id) {
                    data.sessions.filter { s -> s.exercises.any { it.exerciseId == ex.id } }
                }
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ex.huName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                            if (ex.huName != ex.name) Text(ex.name, color = Muted, fontSize = 12.sp)
                        }
                        Text(
                            "✕",
                            color = Muted,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable(onClick = onDismiss)
                                .padding(8.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    ExerciseDemo(
                        ex.imageUrls,
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f)
                    )
                    Text(
                        "Az animáció két kulcshelyzetet mutat: kiindulás és végpont.",
                        color = Muted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ex.primary.forEach { Tag(Hu.muscle(it), Orange, androidx.compose.ui.graphics.Color.Black) }
                        ex.secondary.take(3).forEach { Tag(Hu.muscle(it)) }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${Hu.equipment(ex.equipment)} · ${Hu.level(ex.level)}" +
                            (if (ex.mechanic == "compound") " · összetett" else if (ex.mechanic == "isolation") " · izolációs" else ""),
                        color = Muted,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(16.dp))
                    if (tips.isNotEmpty()) {
                        AppCard(Modifier.fillMaxWidth()) {
                            Text("✅ Szabályos technika", fontWeight = FontWeight.Bold, color = Green, fontSize = 16.sp)
                            Spacer(Modifier.height(6.dp))
                            tips.forEach {
                                Text("• $it", color = TextMain, fontSize = 14.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    AppCard(Modifier.fillMaxWidth()) {
                        Text("Végrehajtás (angolul)", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
                        Spacer(Modifier.height(6.dp))
                        ex.instructions.forEachIndexed { i, step ->
                            Text("${i + 1}. $step", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    AppCard(Modifier.fillMaxWidth()) {
                        Text("📈 Eredményeid", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
                        if (history.isEmpty()) {
                            Text("Még nem végezted el ezt a gyakorlatot.", color = Muted, fontSize = 13.sp)
                        } else {
                            var bestW = 0.0
                            var bestR = 0
                            var bestScore = 0.0
                            history.forEach { s ->
                                s.exercises.filter { it.exerciseId == ex.id }.forEach { e ->
                                    e.sets.forEach { st ->
                                        val sc = score(st.weight, st.reps)
                                        if (sc > bestScore) {
                                            bestScore = sc
                                            bestW = st.weight
                                            bestR = st.reps
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Legjobb: ${fmtKg(bestW)} kg × $bestR" +
                                    (if (bestW > 0.0) " (becsült 1RM: ${fmtKg(Math.round(bestScore).toDouble())} kg)" else ""),
                                color = Gold,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(6.dp))
                            history.takeLast(5).reversed().forEach { s ->
                                val sets = s.exercises.first { it.exerciseId == ex.id }.sets
                                Text(
                                    "%02d.%02d.  ".format(s.date.monthValue, s.date.dayOfMonth) +
                                        sets.joinToString("  ") { fmtKg(it.weight) + "×" + it.reps },
                                    color = Muted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun SwapDialog(exerciseId: String, onPick: (Exercise) -> Unit, onDismiss: () -> Unit) {
    val orig = ExerciseRepo.get(exerciseId)
    var equip by remember { mutableStateOf("") }
    val candidates = remember(exerciseId) {
        if (orig == null) {
            emptyList()
        } else {
            ExerciseRepo.all
                .filter { c -> c.id != orig.id && c.primary.any { m -> m in orig.primary } }
                .sortedWith(
                    compareBy<Exercise>(
                        { it.mechanic != orig.mechanic },
                        { it.primary.firstOrNull() != orig.primary.firstOrNull() },
                        { it.huName }
                    )
                )
        }
    }
    val shown = if (equip.isEmpty()) candidates else candidates.filter {
        if (equip == "other") it.equipment !in Hu.mainEquipment else it.equipment == equip
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Gyakorlat cseréje", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        Text("Nincs meg: ${orig?.huName ?: exerciseId}", color = Muted, fontSize = 12.sp)
                    }
                    Text(
                        "✕",
                        color = Muted,
                        fontSize = 22.sp,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(8.dp)
                    )
                }
                Text(
                    "Ugyanazt az izomcsoportot dolgozó gyakorlatok. Szűrj arra az eszközre, ami van a termedben.",
                    color = Muted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Chip("Minden eszköz", equip.isEmpty()) { equip = "" } }
                    items(Hu.mainEquipment) { e ->
                        Chip(Hu.equipment(e), equip == e) { equip = if (equip == e) "" else e }
                    }
                    item { Chip("Egyéb", equip == "other") { equip = if (equip == "other") "" else "other" } }
                }
                Text(
                    "${shown.size} lehetőség",
                    color = Muted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(shown, key = { it.id }) { ex ->
                        ExerciseRow(ex) { onPick(ex) }
                    }
                }
            }
        }
    }
}
