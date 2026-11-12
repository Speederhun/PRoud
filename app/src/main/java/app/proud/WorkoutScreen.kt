package app.proud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun WorkoutScreen(
    store: Store,
    data: AppData,
    onClose: () -> Unit,
    onInfo: (String) -> Unit
) {
    val active = data.active
    var result by remember { mutableStateOf<FinishResult?>(null) }
    var showFinish by remember { mutableStateOf(false) }
    var showDiscard by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    var swapIdx by remember { mutableStateOf<Int?>(null) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var restEnd by remember { mutableLongStateOf(0L) }
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }

    LaunchedEffect(restEnd) {
        if (restEnd > 0L) {
            val wait = restEnd - System.currentTimeMillis()
            if (wait > 0) delay(wait)
            vibrate(ctx, long = true)
            restEnd = 0L
        }
    }

    // Ha nincs aktív edzés és nincs összegzés sem, kilépünk.
    if (active == null && result == null) {
        LaunchedEffect(Unit) { onClose() }
    }

    if (active != null) {
        val doneCount = active.exercises.sumOf { e -> e.sets.count { it.done } }
        val totalCount = active.exercises.sumOf { it.sets.size }
        val volume = active.exercises.sumOf { e -> e.sets.filter { it.done }.sumOf { it.weight * it.reps } }

        Scaffold(
            containerColor = Bg,
            topBar = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Surf)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(active.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextMain, maxLines = 1)
                        Text(
                            "⏱ ${fmtDuration((now - active.startedAt) / 1000L)} · $doneCount/$totalCount szett · ${fmtVolume(volume)}",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                    TextButton(onClick = onClose) { Text("Kilépés", color = Muted) }
                    Button(onClick = { showFinish = true }) { Text("Befejezés") }
                }
            },
            bottomBar = {
                if (restEnd > 0L) {
                    val remaining = ((restEnd - now + 999L) / 1000L).coerceAtLeast(0L)
                    RestBar(
                        remaining = remaining,
                        total = data.settings.restSeconds,
                        onAdd = { restEnd += it * 1000L },
                        onSkip = { restEnd = 0L }
                    )
                }
            }
        ) { pad ->
            LazyColumn(
                modifier = Modifier
                    .padding(pad)
                    .imePadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(active.exercises) { exIdx, ex ->
                    val isLastExercise = exIdx == active.exercises.lastIndex
                    ExerciseBlock(
                        store = store,
                        exIdx = exIdx,
                        ex = ex,
                        onInfo = onInfo,
                        onSwap = { swapIdx = exIdx },
                        onSetDone = { isLastSet ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (!(isLastExercise && isLastSet)) {
                                restEnd = System.currentTimeMillis() + data.settings.restSeconds * 1000L
                            }
                        }
                    )
                }
                item {
                    OutlinedButton(
                        onClick = { showPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("+ Gyakorlat hozzáadása") }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        val swapAt = swapIdx
        if (swapAt != null && swapAt < active.exercises.size) {
            SwapDialog(
                exerciseId = active.exercises[swapAt].exerciseId,
                onPick = {
                    store.replaceExercise(swapAt, it.id)
                    swapIdx = null
                },
                onDismiss = { swapIdx = null }
            )
        }

        if (showPicker) {
            ExercisePickerDialog(
                onPick = {
                    store.addExercise(it.id)
                    showPicker = false
                },
                onDismiss = { showPicker = false }
            )
        }

        if (showFinish) {
            AlertDialog(
                onDismissRequest = { showFinish = false },
                containerColor = Surf,
                title = { Text(if (doneCount == 0) "Nincs kész szett" else "Edzés befejezése") },
                text = {
                    Column {
                        Text(
                            if (doneCount == 0) "Még egy szettet sem jelöltél késznek. Elveted az edzést?"
                            else "$doneCount / $totalCount szett kész. A be nem jelölt szettek nem kerülnek a naplóba."
                        )
                        if (doneCount > 0) {
                            TextButton(onClick = {
                                showFinish = false
                                showDiscard = true
                            }) { Text("Edzés elvetése", color = Danger) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showFinish = false
                        if (doneCount == 0) {
                            store.discardActive()
                        } else {
                            result = store.finishActive()
                            vibrate(ctx, long = true)
                        }
                    }) { Text(if (doneCount == 0) "Elvetés" else "Befejezés") }
                },
                dismissButton = {
                    TextButton(onClick = { showFinish = false }) { Text("Vissza") }
                }
            )
        }

        if (showDiscard) {
            AlertDialog(
                onDismissRequest = { showDiscard = false },
                containerColor = Surf,
                title = { Text("Edzés elvetése") },
                text = { Text("Az eddigi adatok elvesznek.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDiscard = false
                        store.discardActive()
                    }) { Text("Elvetés") }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscard = false }) { Text("Mégse") }
                }
            )
        }
    }

    val r = result
    if (r != null) {
        SummaryDialog(r, onDone = {
            result = null
            onClose()
        })
    }
}

@Composable
private fun RestBar(remaining: Long, total: Int, onAdd: (Int) -> Unit, onSkip: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Surf)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⏳ Pihenő", color = Muted, fontSize = 13.sp)
            Text(
                fmtDuration(remaining),
                color = Orange,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            TextButton(onClick = { onAdd(-15) }) { Text("−15") }
            TextButton(onClick = { onAdd(15) }) { Text("+15") }
            TextButton(onClick = onSkip) { Text("Kihagyás") }
        }
        ProgressBar(remaining.toFloat() / total.toFloat().coerceAtLeast(1f), Orange, 6)
    }
}

@Composable
private fun ExerciseBlock(
    store: Store,
    exIdx: Int,
    ex: ActiveExercise,
    onInfo: (String) -> Unit,
    onSwap: () -> Unit,
    onSetDone: (isLastSet: Boolean) -> Unit
) {
    val info = ExerciseRepo.get(ex.exerciseId)
    val last = remember(ex.exerciseId) { store.lastSets(ex.exerciseId) }
    val histBest = remember(ex.exerciseId) { store.historyBest(ex.exerciseId) }
    val bestDone = ex.sets.filter { it.done }.maxOfOrNull { score(it.weight, it.reps) } ?: 0.0

    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExerciseThumb(info, 52)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .clickable { onInfo(ex.exerciseId) }
            ) {
                Text(
                    info?.huName ?: ex.exerciseId,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextMain
                )
                Text(
                    if (last.isEmpty()) "Még nem csináltad" else
                        "Legutóbb: " + last.joinToString(", ") { "${fmtKg(it.weight)}×${it.reps}" },
                    color = Muted,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }
            if (ex.sets.none { it.done }) {
                Text(
                    "🔄",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clickable(onClick = onSwap)
                        .padding(8.dp)
                )
            }
            Text(
                "ⓘ",
                color = Blue,
                fontSize = 22.sp,
                modifier = Modifier
                    .clickable { onInfo(ex.exerciseId) }
                    .padding(8.dp)
            )
            Text(
                "✕",
                color = Muted,
                fontSize = 18.sp,
                modifier = Modifier
                    .clickable { store.removeExercise(exIdx) }
                    .padding(8.dp)
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("SZETT", color = Muted, fontSize = 10.sp, modifier = Modifier.width(36.dp))
            Text("KG", color = Muted, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Text("ISM", color = Muted, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Spacer(Modifier.size(88.dp))
        }

        ex.sets.forEachIndexed { sIdx, set ->
            key(set.id) {
                val sc = score(set.weight, set.reps)
                val livePr = set.done && sc > 0.0 && sc > histBest && sc >= bestDone
                SetRow(
                    index = sIdx + 1,
                    set = set,
                    isPr = livePr,
                    onWeight = { s ->
                        val w = s.toDoubleOrNull() ?: 0.0
                        store.updateSet(exIdx, set.id) { it.copy(weight = w) }
                    },
                    onReps = { s ->
                        val r = s.toIntOrNull() ?: 0
                        store.updateSet(exIdx, set.id) { it.copy(reps = r) }
                    },
                    onToggle = {
                        val nowDone = !set.done
                        store.updateSet(exIdx, set.id) { it.copy(done = nowDone) }
                        if (nowDone) onSetDone(sIdx == ex.sets.lastIndex)
                    },
                    onDelete = { store.removeSet(exIdx, set.id) }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { store.addSet(exIdx) }) { Text("+ Szett") }
        }
    }
}

@Composable
private fun SetRow(
    index: Int,
    set: SetEntry,
    isPr: Boolean,
    onWeight: (String) -> Unit,
    onReps: (String) -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(width = 36.dp, height = 44.dp), contentAlignment = Alignment.Center) {
            Text(
                if (isPr) "🏆" else "$index",
                color = if (set.done) Green else Muted,
                fontWeight = FontWeight.Bold,
                fontSize = if (isPr) 18.sp else 15.sp
            )
        }
        NumField(
            initial = if (set.weight == 0.0) "" else fmtKg(set.weight),
            placeholder = "kg",
            decimal = true,
            onValue = onWeight,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )
        NumField(
            initial = if (set.reps == 0) "" else set.reps.toString(),
            placeholder = "ism",
            decimal = false,
            onValue = onReps,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )
        Box(
            Modifier
                .padding(start = 4.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(if (set.done) Green else Surf2)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = if (set.done) Color.Black else Muted, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier
                .size(40.dp)
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Text("🗑", fontSize = 15.sp, color = Muted)
        }
    }
}

@Composable
fun NumField(
    initial: String,
    placeholder: String,
    decimal: Boolean,
    onValue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(initial) }
    BasicTextField(
        value = text,
        onValueChange = { v ->
            val f = v.replace(',', '.').filter { it.isDigit() || (decimal && it == '.') }
            if (f.count { it == '.' } <= 1 && f.length <= 6) {
                text = f
                onValue(f)
            }
        },
        singleLine = true,
        textStyle = TextStyle(
            color = TextMain,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(Orange),
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        modifier = modifier
            .height(44.dp)
            .background(Surf2, RoundedCornerShape(10.dp)),
        decorationBox = { inner ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (text.isEmpty()) Text(placeholder, color = Muted, fontSize = 16.sp)
                inner()
            }
        }
    )
}

@Composable
fun SummaryDialog(r: FinishResult, onDone: () -> Unit) {
    Dialog(
        onDismissRequest = onDone,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Bg)
        ) {
            Confetti(Modifier.fillMaxSize())
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                BouncyEmoji("🎉", 72.sp)
                Text("Edzés kész!", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Orange)
                Text(r.session.name, color = Muted)
                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SummaryStat("⏱", "${r.session.minutes} perc")
                    SummaryStat("🎯", "${r.session.setCount} szett")
                    SummaryStat("⚖️", fmtVolume(r.session.volume))
                }
                Spacer(Modifier.height(20.dp))

                AppCard(Modifier.fillMaxWidth()) {
                    Text("+${r.xpGained} XP", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                    if (r.levelAfter > r.levelBefore) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "🆙 SZINTLÉPÉS! ${r.levelBefore} → ${r.levelAfter} · ${levelTitle(r.levelAfter)}",
                            color = Green,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val prs = r.session.exercises.flatMap { e ->
                    e.sets.filter { it.isPr }.map { e.exerciseId to it }
                }
                if (prs.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    AppCard(Modifier.fillMaxWidth()) {
                        Text("🏆 Új személyes rekordok", fontWeight = FontWeight.Bold, color = Gold)
                        Spacer(Modifier.height(6.dp))
                        prs.forEach { (id, st) ->
                            Text(
                                "${ExerciseRepo.nameOf(id)} – ${fmtKg(st.weight)} kg × ${st.reps}",
                                color = TextMain,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                if (r.newAchievements.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Új achievementek!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextMain)
                    r.newAchievements.forEach { a ->
                        Spacer(Modifier.height(10.dp))
                        AppCard(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BouncyEmoji(a.emoji, 44.sp)
                                Column(Modifier.padding(start = 14.dp)) {
                                    Text(a.title, fontWeight = FontWeight.Bold, color = tierColor(a.tier), fontSize = 17.sp)
                                    Text(a.desc, color = Muted, fontSize = 12.sp)
                                    Text("+${a.xp} XP", color = Gold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Kész") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummaryStat(emoji: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Text(value, fontWeight = FontWeight.Bold, color = TextMain)
    }
}
