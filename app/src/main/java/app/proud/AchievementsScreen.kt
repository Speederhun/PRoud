package app.proud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AchievementsScreen(store: Store, data: AppData) {
    val stats = remember(data) { computeStats(data) }
    val li = levelInfo(stats.xp)
    val byCategory = remember { ACHIEVEMENTS.groupBy { it.category } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Profil & achievementek", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Orange)
        }

        item {
            AppCard {
                Text("Lv. ${li.level} · ${li.title}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Text("${stats.xp} XP összesen", color = Gold, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                ProgressBar(li.xpInto.toFloat() / li.xpNeeded.toFloat(), Gold, 10)
                Text("${li.xpInto} / ${li.xpNeeded} XP a következő szintig", color = Muted, fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${stats.workouts}", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 18.sp)
                        Text("edzés", color = Muted, fontSize = 11.sp)
                    }
                    Column {
                        Text(fmtVolume(stats.volume), fontWeight = FontWeight.Bold, color = TextMain, fontSize = 18.sp)
                        Text("mozgatott súly", color = Muted, fontSize = 11.sp)
                    }
                    Column {
                        Text("${stats.sets}", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 18.sp)
                        Text("szett", color = Muted, fontSize = 11.sp)
                    }
                    Column {
                        Text("${stats.prs}", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 18.sp)
                        Text("rekord", color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            AppCard {
                Text("Beállítások", fontWeight = FontWeight.Bold, color = TextMain)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Heti cél", color = TextMain)
                        Text("edzés / hét (a szériához)", color = Muted, fontSize = 11.sp)
                    }
                    Stepper(
                        value = data.settings.weeklyGoal,
                        onChange = { store.setSettings(data.settings.copy(weeklyGoal = it)) },
                        min = 1,
                        max = 7
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Pihenőidő", color = TextMain)
                        Text("szettek között", color = Muted, fontSize = 11.sp)
                    }
                    Stepper(
                        value = data.settings.restSeconds,
                        onChange = { store.setSettings(data.settings.copy(restSeconds = it)) },
                        min = 15,
                        max = 600,
                        step = 15,
                        suffix = " mp"
                    )
                }
            }
        }

        item {
            Text(
                "🏆 ${data.unlocked.size} / ${ACHIEVEMENTS.size} megszerezve",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        }

        byCategory.forEach { (cat, list) ->
            item(key = "cat_$cat") {
                Text(cat.uppercase(), color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            list.chunked(2).forEachIndexed { rowIdx, pair ->
                item(key = "row_${cat}_$rowIdx") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { a ->
                            AchievementCard(a, stats, data.unlocked[a.id], Modifier.weight(1f))
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun AchievementCard(a: Achievement, stats: Stats, unlockedAt: Long?, modifier: Modifier) {
    val unlocked = unlockedAt != null
    val shape = RoundedCornerShape(16.dp)
    val border = if (unlocked) tierColor(a.tier) else Surf2
    Column(
        modifier
            .background(if (unlocked) Surf else Surf.copy(alpha = 0.6f), shape)
            .border(1.5.dp, border, shape)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(a.emoji, fontSize = 36.sp, modifier = Modifier.alpha(if (unlocked) 1f else 0.25f))
        Spacer(Modifier.height(4.dp))
        Text(
            a.title,
            fontWeight = FontWeight.Bold,
            color = if (unlocked) TextMain else Muted,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
        Text(a.desc, color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 4.dp))
        if (unlockedAt != null) {
            val d = unlockedAt.toLocalDate()
            Text(
                "✔ %d.%02d.%02d.".format(d.year, d.monthValue, d.dayOfMonth),
                color = tierColor(a.tier),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            val cur = a.progress(stats).coerceAtMost(a.target)
            ProgressBar((cur / a.target).toFloat(), tierColor(a.tier), 6)
            Text("${fmtNum(cur)} / ${fmtNum(a.target)}", color = Muted, fontSize = 10.sp)
        }
    }
}
