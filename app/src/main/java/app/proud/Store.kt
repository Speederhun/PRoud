package app.proud

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class FinishResult(
    val session: Session,
    val newAchievements: List<Achievement>,
    val xpGained: Int,
    val levelBefore: Int,
    val levelAfter: Int
)

class Store(private val file: File) {
    private val state = MutableStateFlow(load())
    val data: StateFlow<AppData> = state

    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()
    private var seq = System.currentTimeMillis()

    private fun nextId(): Long {
        seq += 1
        return seq
    }

    private fun mutate(block: (AppData) -> AppData) {
        state.value = block(state.value)
        save()
    }

    private fun save() {
        io.launch {
            synchronized(lock) {
                try {
                    val json = encode(state.value).toString()
                    val tmp = File(file.path + ".tmp")
                    tmp.writeText(json)
                    if (!tmp.renameTo(file)) {
                        file.writeText(json)
                        tmp.delete()
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    // ---------- sablonok ----------

    fun saveTemplate(t: Template) = mutate { d ->
        val exists = d.templates.any { it.id == t.id }
        d.copy(
            templates = if (exists) d.templates.map { if (it.id == t.id) t else it }
            else d.templates + t
        )
    }

    fun deleteTemplate(id: String) = mutate { d ->
        d.copy(
            templates = d.templates.filter { it.id != id },
            plans = d.plans.filter { it.templateId != id }
        )
    }

    fun newTemplateId(): String = "t" + nextId()

    // ---------- napló lekérdezések ----------

    fun lastSets(exerciseId: String): List<FinishedSet> {
        val s = state.value.sessions.lastOrNull { sess -> sess.exercises.any { it.exerciseId == exerciseId } }
            ?: return emptyList()
        return s.exercises.first { it.exerciseId == exerciseId }.sets
    }

    fun historyBest(exerciseId: String): Double {
        var best = 0.0
        for (s in state.value.sessions) for (e in s.exercises) {
            if (e.exerciseId != exerciseId) continue
            for (st in e.sets) {
                val sc = score(st.weight, st.reps)
                if (sc > best) best = sc
            }
        }
        return best
    }

    // ---------- aktív edzés ----------

    fun startWorkout(name: String, items: List<TemplateItem>) = mutate { d ->
        val exs = items.map { item ->
            val last = lastSets(item.exerciseId)
            val sets = List(item.sets) { i ->
                val prev = last.getOrNull(i) ?: last.lastOrNull()
                SetEntry(nextId(), prev?.weight ?: 0.0, item.reps, false)
            }
            ActiveExercise(item.exerciseId, sets)
        }
        d.copy(active = ActiveWorkout(System.currentTimeMillis(), name, exs))
    }

    fun discardActive() = mutate { it.copy(active = null) }

    private fun updateActive(block: (ActiveWorkout) -> ActiveWorkout) = mutate { d ->
        val a = d.active
        if (a == null) d else d.copy(active = block(a))
    }

    fun updateSet(exIndex: Int, setId: Long, transform: (SetEntry) -> SetEntry) = updateActive { a ->
        a.copy(exercises = a.exercises.mapIndexed { i, e ->
            if (i != exIndex) e
            else e.copy(sets = e.sets.map { if (it.id == setId) transform(it) else it })
        })
    }

    fun addSet(exIndex: Int) = updateActive { a ->
        a.copy(exercises = a.exercises.mapIndexed { i, e ->
            if (i != exIndex) e
            else {
                val last = e.sets.lastOrNull()
                e.copy(sets = e.sets + SetEntry(nextId(), last?.weight ?: 0.0, last?.reps ?: 8, false))
            }
        })
    }

    fun removeSet(exIndex: Int, setId: Long) = updateActive { a ->
        a.copy(exercises = a.exercises.mapIndexed { i, e ->
            if (i != exIndex) e else e.copy(sets = e.sets.filter { it.id != setId })
        })
    }

    fun addExercise(exerciseId: String) = updateActive { a ->
        val last = lastSets(exerciseId)
        val sets = List(3) { i ->
            val prev = last.getOrNull(i) ?: last.lastOrNull()
            SetEntry(nextId(), prev?.weight ?: 0.0, prev?.reps ?: 10, false)
        }
        a.copy(exercises = a.exercises + ActiveExercise(exerciseId, sets))
    }

    /** Kicseréli a gyakorlatot (pl. ha nincs ilyen gép a teremben); a szettek újrakezdődnek. */
    fun replaceExercise(exIndex: Int, newId: String) = updateActive { a ->
        val last = lastSets(newId)
        a.copy(exercises = a.exercises.mapIndexed { i, e ->
            if (i != exIndex) e
            else {
                val sets = e.sets.mapIndexed { si, old ->
                    val prev = last.getOrNull(si) ?: last.lastOrNull()
                    SetEntry(nextId(), prev?.weight ?: 0.0, old.reps, false)
                }
                ActiveExercise(newId, sets)
            }
        })
    }

    fun removeExercise(exIndex: Int) = updateActive { a ->
        a.copy(exercises = a.exercises.filterIndexed { i, _ -> i != exIndex })
    }

    /** Befejezi az edzést. Null, ha nincs egyetlen teljesített szett sem. */
    fun finishActive(): FinishResult? {
        val d = state.value
        val a = d.active ?: return null
        val now = System.currentTimeMillis()

        val histBest = HashMap<String, Double>()
        for (s in d.sessions) for (e in s.exercises) for (st in e.sets) {
            val sc = score(st.weight, st.reps)
            if (sc > (histBest[e.exerciseId] ?: 0.0)) histBest[e.exerciseId] = sc
        }

        val exs = ArrayList<SessionExercise>()
        for (e in a.exercises) {
            val done = e.sets.filter { it.done }
            if (done.isEmpty()) continue
            val top = done.maxByOrNull { score(it.weight, it.reps) } ?: continue
            val topScore = score(top.weight, top.reps)
            val prId = if (topScore > 0.0 && topScore > (histBest[e.exerciseId] ?: 0.0)) top.id else -1L
            exs.add(SessionExercise(e.exerciseId, done.map { FinishedSet(it.weight, it.reps, it.id == prId) }))
        }
        if (exs.isEmpty()) return null

        val session = Session(now.toString(), a.startedAt, now, a.name, exs)
        val xpBefore = computeStats(d).xp
        val afterSession = d.copy(sessions = d.sessions + session, active = null)
        val statsAfter = computeStats(afterSession)
        val newly = ACHIEVEMENTS.filter { it.id !in d.unlocked && it.progress(statsAfter) >= it.target }
        val unlocked = d.unlocked + newly.associate { it.id to now }
        val finalData = afterSession.copy(unlocked = unlocked)
        val xpAfter = computeStats(finalData).xp

        state.value = finalData
        save()
        return FinishResult(
            session = session,
            newAchievements = newly,
            xpGained = xpAfter - xpBefore,
            levelBefore = levelInfo(xpBefore).level,
            levelAfter = levelInfo(xpAfter).level
        )
    }

    // ---------- naptár ----------

    fun addPlan(epochDay: Long, templateId: String) = mutate { d ->
        if (d.plans.any { it.epochDay == epochDay && it.templateId == templateId }) d
        else d.copy(plans = d.plans + Plan(epochDay, templateId))
    }

    fun removePlan(epochDay: Long, templateId: String) = mutate { d ->
        d.copy(plans = d.plans.filterNot { it.epochDay == epochDay && it.templateId == templateId })
    }

    // ---------- beállítások ----------

    fun setSettings(s: Settings) = mutate { it.copy(settings = s) }

    // ---------- betöltés / mentés ----------

    private fun load(): AppData {
        if (file.exists()) {
            try {
                return decode(JSONObject(file.readText()))
            } catch (e: Exception) {
            }
        }
        return AppData(
            templates = defaultTemplates(),
            sessions = emptyList(),
            plans = emptyList(),
            unlocked = emptyMap(),
            settings = Settings(),
            active = null
        )
    }

    companion object {
        const val SEED_VERSION = 3

        private fun ti(id: String, sets: Int, reps: Int) = TemplateItem(id, sets, reps)

        private fun tpl(key: String, group: String, quick: Boolean, items: List<TemplateItem>) =
            Template(
                id = "d_${key}_${if (quick) "quick" else "full"}",
                name = "$group – ${if (quick) "Gyors edzés" else "Teljes edzés"}",
                emoji = Hu.groupEmoji(group),
                items = items,
                group = group
            )

        fun defaultTemplates(): List<Template> = listOf(
            tpl("push", "Push", true, listOf(
                ti("Barbell_Bench_Press_-_Medium_Grip", 3, 8), ti("Barbell_Shoulder_Press", 3, 8),
                ti("Triceps_Pushdown", 3, 12))),
            tpl("push", "Push", false, listOf(
                ti("Barbell_Bench_Press_-_Medium_Grip", 4, 8), ti("Incline_Dumbbell_Press", 3, 10),
                ti("Barbell_Shoulder_Press", 3, 8), ti("Side_Lateral_Raise", 3, 12),
                ti("Triceps_Pushdown", 3, 12), ti("Dips_-_Triceps_Version", 3, 10))),
            tpl("pull", "Pull", true, listOf(
                ti("Pullups", 3, 8), ti("Bent_Over_Barbell_Row", 3, 8), ti("Barbell_Curl", 3, 10))),
            tpl("pull", "Pull", false, listOf(
                ti("Barbell_Deadlift", 3, 5), ti("Pullups", 4, 8), ti("Bent_Over_Barbell_Row", 4, 8),
                ti("Wide-Grip_Lat_Pulldown", 3, 10), ti("Face_Pull", 3, 15), ti("Barbell_Curl", 3, 10))),
            tpl("chest", "Mell", true, listOf(
                ti("Barbell_Bench_Press_-_Medium_Grip", 3, 8), ti("Incline_Dumbbell_Press", 3, 10), ti("Pushups", 2, 15))),
            tpl("chest", "Mell", false, listOf(
                ti("Barbell_Bench_Press_-_Medium_Grip", 4, 8), ti("Incline_Dumbbell_Press", 3, 10),
                ti("Dumbbell_Flyes", 3, 12), ti("Cable_Crossover", 3, 12),
                ti("Dips_-_Chest_Version", 3, 10), ti("Pushups", 2, 15))),
            tpl("back", "Hát", true, listOf(
                ti("Pullups", 3, 8), ti("Bent_Over_Barbell_Row", 3, 8), ti("Wide-Grip_Lat_Pulldown", 3, 10))),
            tpl("back", "Hát", false, listOf(
                ti("Barbell_Deadlift", 3, 5), ti("Pullups", 4, 8), ti("Bent_Over_Barbell_Row", 4, 8),
                ti("Seated_Cable_Rows", 3, 10), ti("Wide-Grip_Lat_Pulldown", 3, 10),
                ti("Hyperextensions_Back_Extensions", 3, 12))),
            tpl("legs", "Láb", true, listOf(
                ti("Barbell_Full_Squat", 3, 8), ti("Romanian_Deadlift", 3, 8), ti("Standing_Calf_Raises", 3, 12))),
            tpl("legs", "Láb", false, listOf(
                ti("Barbell_Full_Squat", 4, 6), ti("Romanian_Deadlift", 3, 8), ti("Leg_Press", 3, 12),
                ti("Barbell_Lunge", 3, 10), ti("Lying_Leg_Curls", 3, 12),
                ti("Barbell_Hip_Thrust", 3, 10), ti("Standing_Calf_Raises", 4, 12))),
            tpl("shoulders", "Váll", true, listOf(
                ti("Barbell_Shoulder_Press", 3, 8), ti("Side_Lateral_Raise", 3, 12), ti("Face_Pull", 3, 15))),
            tpl("shoulders", "Váll", false, listOf(
                ti("Barbell_Shoulder_Press", 4, 8), ti("Dumbbell_Shoulder_Press", 3, 10),
                ti("Side_Lateral_Raise", 4, 12), ti("Reverse_Flyes", 3, 15),
                ti("Face_Pull", 3, 15), ti("Dumbbell_Shrug", 3, 12))),
            tpl("arms", "Kar", true, listOf(
                ti("Barbell_Curl", 3, 10), ti("Triceps_Pushdown", 3, 12), ti("Hammer_Curls", 2, 12))),
            tpl("arms", "Kar", false, listOf(
                ti("Barbell_Curl", 3, 10), ti("Close-Grip_Barbell_Bench_Press", 3, 8),
                ti("Hammer_Curls", 3, 12), ti("EZ-Bar_Skullcrusher", 3, 10),
                ti("Dumbbell_Bicep_Curl", 3, 12), ti("Cable_Rope_Overhead_Triceps_Extension", 3, 12),
                ti("Triceps_Pushdown", 3, 12))),
            tpl("core", "Core", true, listOf(
                ti("Hanging_Leg_Raise", 3, 10), ti("Cable_Crunch", 3, 15), ti("Ab_Roller", 3, 10))),
            tpl("core", "Core", false, listOf(
                ti("Hanging_Leg_Raise", 3, 12), ti("Cable_Crunch", 3, 15), ti("Ab_Roller", 3, 10),
                ti("Decline_Crunch", 3, 15), ti("Rope_Crunch", 3, 15))),
            tpl("full", "Teljes test", true, listOf(
                ti("Barbell_Full_Squat", 3, 8), ti("Barbell_Bench_Press_-_Medium_Grip", 3, 8),
                ti("Bent_Over_Barbell_Row", 3, 8))),
            tpl("full", "Teljes test", false, listOf(
                ti("Barbell_Full_Squat", 3, 8), ti("Barbell_Bench_Press_-_Medium_Grip", 3, 8),
                ti("Bent_Over_Barbell_Row", 3, 8), ti("Barbell_Shoulder_Press", 3, 8),
                ti("Barbell_Curl", 2, 10), ti("Triceps_Pushdown", 2, 12)))
        )

        private fun encode(d: AppData): JSONObject {
            val o = JSONObject()
            o.put("templates", JSONArray().also { arr ->
                d.templates.forEach { t ->
                    arr.put(JSONObject().apply {
                        put("id", t.id)
                        put("name", t.name)
                        put("emoji", t.emoji)
                        put("group", t.group)
                        put("items", JSONArray().also { ia ->
                            t.items.forEach { i ->
                                ia.put(JSONObject().apply {
                                    put("ex", i.exerciseId)
                                    put("sets", i.sets)
                                    put("reps", i.reps)
                                })
                            }
                        })
                    })
                }
            })
            o.put("sessions", JSONArray().also { arr ->
                d.sessions.forEach { s ->
                    arr.put(JSONObject().apply {
                        put("id", s.id)
                        put("start", s.startedAt)
                        put("end", s.endedAt)
                        put("name", s.name)
                        put("exs", JSONArray().also { ea ->
                            s.exercises.forEach { e ->
                                ea.put(JSONObject().apply {
                                    put("ex", e.exerciseId)
                                    put("sets", JSONArray().also { sa ->
                                        e.sets.forEach { st ->
                                            sa.put(JSONObject().apply {
                                                put("w", st.weight)
                                                put("r", st.reps)
                                                put("pr", st.isPr)
                                            })
                                        }
                                    })
                                })
                            }
                        })
                    })
                }
            })
            o.put("seed", SEED_VERSION)
            o.put("plans", JSONArray().also { arr ->
                d.plans.forEach { p ->
                    arr.put(JSONObject().apply {
                        put("day", p.epochDay)
                        put("tpl", p.templateId)
                    })
                }
            })
            o.put("unlocked", JSONObject().also { u ->
                d.unlocked.forEach { (k, v) -> u.put(k, v) }
            })
            o.put("settings", JSONObject().apply {
                put("goal", d.settings.weeklyGoal)
                put("rest", d.settings.restSeconds)
            })
            val a = d.active
            if (a != null) {
                o.put("active", JSONObject().apply {
                    put("start", a.startedAt)
                    put("name", a.name)
                    put("exs", JSONArray().also { ea ->
                        a.exercises.forEach { e ->
                            ea.put(JSONObject().apply {
                                put("ex", e.exerciseId)
                                put("sets", JSONArray().also { sa ->
                                    e.sets.forEach { st ->
                                        sa.put(JSONObject().apply {
                                            put("id", st.id)
                                            put("w", st.weight)
                                            put("r", st.reps)
                                            put("done", st.done)
                                        })
                                    }
                                })
                            })
                        }
                    })
                })
            }
            return o
        }

        private fun decode(o: JSONObject): AppData {
            val templates = ArrayList<Template>()
            val ta = o.optJSONArray("templates") ?: JSONArray()
            for (i in 0 until ta.length()) {
                val t = ta.getJSONObject(i)
                val items = ArrayList<TemplateItem>()
                val ia = t.optJSONArray("items") ?: JSONArray()
                for (j in 0 until ia.length()) {
                    val ij = ia.getJSONObject(j)
                    items.add(TemplateItem(ij.getString("ex"), ij.optInt("sets", 3), ij.optInt("reps", 10)))
                }
                templates.add(Template(t.getString("id"), t.optString("name", "Edzés"), t.optString("emoji", "💪"), items, t.optString("group", "")))
            }

            val sessions = ArrayList<Session>()
            val sa = o.optJSONArray("sessions") ?: JSONArray()
            for (i in 0 until sa.length()) {
                val s = sa.getJSONObject(i)
                val exs = ArrayList<SessionExercise>()
                val ea = s.optJSONArray("exs") ?: JSONArray()
                for (j in 0 until ea.length()) {
                    val e = ea.getJSONObject(j)
                    val sets = ArrayList<FinishedSet>()
                    val seta = e.optJSONArray("sets") ?: JSONArray()
                    for (k in 0 until seta.length()) {
                        val st = seta.getJSONObject(k)
                        sets.add(FinishedSet(st.optDouble("w", 0.0), st.optInt("r", 0), st.optBoolean("pr", false)))
                    }
                    exs.add(SessionExercise(e.getString("ex"), sets))
                }
                sessions.add(Session(s.getString("id"), s.getLong("start"), s.getLong("end"), s.optString("name", "Edzés"), exs))
            }

            val plans = ArrayList<Plan>()
            val pa = o.optJSONArray("plans") ?: JSONArray()
            for (i in 0 until pa.length()) {
                val p = pa.getJSONObject(i)
                plans.add(Plan(p.getLong("day"), p.getString("tpl")))
            }

            val unlocked = HashMap<String, Long>()
            val uo = o.optJSONObject("unlocked")
            if (uo != null) {
                val keys = uo.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    unlocked[k] = uo.getLong(k)
                }
            }

            val so = o.optJSONObject("settings")
            val settings = if (so == null) Settings()
            else Settings(so.optInt("goal", 3), so.optInt("rest", 90))

            var active: ActiveWorkout? = null
            val ao = o.optJSONObject("active")
            if (ao != null) {
                val exs = ArrayList<ActiveExercise>()
                val ea = ao.optJSONArray("exs") ?: JSONArray()
                for (j in 0 until ea.length()) {
                    val e = ea.getJSONObject(j)
                    val sets = ArrayList<SetEntry>()
                    val seta = e.optJSONArray("sets") ?: JSONArray()
                    for (k in 0 until seta.length()) {
                        val st = seta.getJSONObject(k)
                        sets.add(SetEntry(st.getLong("id"), st.optDouble("w", 0.0), st.optInt("r", 0), st.optBoolean("done", false)))
                    }
                    exs.add(ActiveExercise(e.getString("ex"), sets))
                }
                active = ActiveWorkout(ao.getLong("start"), ao.optString("name", "Edzés"), exs)
            }

            var finalTemplates: List<Template> = templates
            if (o.optInt("seed", 1) < SEED_VERSION) {
                val defaults = defaultTemplates()
                val replaceIds = defaults.map { it.id }.toSet() + setOf("d_push", "d_pull", "d_legs", "d_full")
                finalTemplates = defaults + templates.filter { it.id !in replaceIds }
            }

            return AppData(finalTemplates, sessions, plans, unlocked, settings, active)
        }
    }
}
