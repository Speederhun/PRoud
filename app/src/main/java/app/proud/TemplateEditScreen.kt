package app.proud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

private val EMOJIS = listOf("💪", "🔥", "🦵", "🏋️", "🦍", "⚡", "🧱", "🐺", "🚀", "🎯")

@Composable
fun TemplateEditScreen(
    store: Store,
    data: AppData,
    templateId: String?,
    onClose: () -> Unit
) {
    val existing = data.templates.firstOrNull { it.id == templateId }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var emoji by remember { mutableStateOf(existing?.emoji ?: "💪") }
    var group by remember { mutableStateOf(existing?.group ?: "") }
    var items by remember { mutableStateOf(existing?.items ?: emptyList()) }
    var showPicker by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val canSave = name.isNotBlank() && items.isNotEmpty()

    Scaffold(
        containerColor = Bg,
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Surf)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClose) { Text("Mégse", color = Muted) }
                Text(
                    if (existing == null) "Új edzésterv" else "Edzésterv szerkesztése",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextMain,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    enabled = canSave,
                    onClick = {
                        val id = existing?.id ?: store.newTemplateId()
                        store.saveTemplate(Template(id, name.trim(), emoji, items, group))
                        onClose()
                    }
                ) { Text("Mentés") }
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
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Az edzés neve (pl. Mell – Gyors edzés)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EMOJIS.forEach { e ->
                        Chip(e, emoji == e) { emoji = e }
                    }
                }
            }

            item {
                Text("Csoport", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Chip("Saját", group.isEmpty()) { group = "" }
                    Hu.templateGroups.forEach { g ->
                        Chip(g, group == g) { group = g }
                    }
                }
            }

            itemsIndexed(items) { i, item ->
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            ExerciseRepo.nameOf(item.exerciseId),
                            fontWeight = FontWeight.Bold,
                            color = TextMain,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "▲",
                            color = if (i > 0) TextMain else Surf2,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable(enabled = i > 0) {
                                    items = items.toMutableList().also { l ->
                                        val t = l[i]
                                        l[i] = l[i - 1]
                                        l[i - 1] = t
                                    }
                                }
                                .padding(8.dp)
                        )
                        Text(
                            "▼",
                            color = if (i < items.lastIndex) TextMain else Surf2,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable(enabled = i < items.lastIndex) {
                                    items = items.toMutableList().also { l ->
                                        val t = l[i]
                                        l[i] = l[i + 1]
                                        l[i + 1] = t
                                    }
                                }
                                .padding(8.dp)
                        )
                        Text(
                            "✕",
                            color = Danger,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable { items = items.filterIndexed { idx, _ -> idx != i } }
                                .padding(8.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Szettek", color = Muted)
                        Stepper(
                            value = item.sets,
                            onChange = { v -> items = items.mapIndexed { idx, x -> if (idx == i) x.copy(sets = v) else x } },
                            min = 1,
                            max = 10
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ismétlés", color = Muted)
                        Stepper(
                            value = item.reps,
                            onChange = { v -> items = items.mapIndexed { idx, x -> if (idx == i) x.copy(reps = v) else x } },
                            min = 1,
                            max = 50
                        )
                    }
                }
            }

            item {
                OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Gyakorlat hozzáadása")
                }
            }

            if (existing != null) {
                item {
                    TextButton(onClick = { showDelete = true }) {
                        Text("Edzésterv törlése", color = Danger)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showPicker) {
        ExercisePickerDialog(
            onPick = {
                items = items + TemplateItem(it.id, 3, 10)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }

    if (showDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor = Surf,
            title = { Text("Törlöd az edzéstervet?") },
            text = { Text(existing.name) },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    store.deleteTemplate(existing.id)
                    onClose()
                }) { Text("Törlés", color = Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Mégse") }
            }
        )
    }
}
