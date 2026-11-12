package app.proud

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

sealed interface Route {
    data object Workout : Route
    data class TemplateEdit(val id: String?) : Route
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        val store = (application as ProudApplication).store
        setContent {
            ProudTheme {
                ProudApp(store)
            }
        }
    }
}

@Composable
fun ProudApp(store: Store) {
    val data by store.data.collectAsState()
    var tab by rememberSaveable { mutableStateOf(0) }
    var stack by remember { mutableStateOf(listOf<Route>()) }
    var detailId by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = stack.isNotEmpty()) {
        stack = stack.dropLast(1)
    }

    fun startTemplate(t: Template) {
        store.startWorkout(t.name, t.items)
        stack = stack + Route.Workout
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        when (val route = stack.lastOrNull()) {
            null -> MainScaffold(
                tab = tab,
                onTab = { tab = it },
                store = store,
                data = data,
                onOpenWorkout = { stack = stack + Route.Workout },
                onEdit = { stack = stack + Route.TemplateEdit(it) },
                onStartTemplate = { startTemplate(it) },
                onOpenDetail = { detailId = it }
            )

            is Route.Workout -> WorkoutScreen(
                store = store,
                data = data,
                onClose = { stack = stack.dropLast(1) },
                onInfo = { detailId = it }
            )

            is Route.TemplateEdit -> TemplateEditScreen(
                store = store,
                data = data,
                templateId = route.id,
                onClose = { stack = stack.dropLast(1) }
            )
        }
    }

    val id = detailId
    if (id != null) {
        ExerciseDetailDialog(data = data, id = id, onDismiss = { detailId = null })
    }
}

@Composable
private fun MainScaffold(
    tab: Int,
    onTab: (Int) -> Unit,
    store: Store,
    data: AppData,
    onOpenWorkout: () -> Unit,
    onEdit: (String?) -> Unit,
    onStartTemplate: (Template) -> Unit,
    onOpenDetail: (String) -> Unit
) {
    val tabs = listOf(
        "🏋️" to "Edzés",
        "📅" to "Naptár",
        "📈" to "Haladás",
        "🏆" to "Profil",
        "📚" to "Gyakorlat"
    )
    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Surf) {
                tabs.forEachIndexed { i, (emoji, label) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { onTab(i) },
                        icon = { Text(emoji, fontSize = 20.sp) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = Orange,
                            unselectedTextColor = Muted,
                            indicatorColor = Surf2
                        )
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (tab) {
                0 -> HomeScreen(store, data, onStarted = onOpenWorkout, onEdit = onEdit)
                1 -> CalendarScreen(store, data, onStart = onStartTemplate)
                2 -> ProgressScreen(store, data)
                3 -> AchievementsScreen(store, data)
                else -> LibraryScreen(onOpen = onOpenDetail)
            }
        }
    }
}
