package app.proud

import android.content.Context
import org.json.JSONArray

object ExerciseRepo {
    var all: List<Exercise> = emptyList()
        private set
    private var byId: Map<String, Exercise> = emptyMap()

    fun load(ctx: Context) {
        if (all.isNotEmpty()) return
        val text = ctx.assets.open("exercises.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(text)
        val list = ArrayList<Exercise>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Exercise(
                    id = o.getString("id"),
                    name = o.getString("n"),
                    primary = o.getJSONArray("p").strings(),
                    secondary = o.getJSONArray("s").strings(),
                    equipment = o.optString("e", "other"),
                    level = o.optString("l", "beginner"),
                    mechanic = o.optString("m", ""),
                    instructions = o.getJSONArray("i").strings(),
                    images = o.getJSONArray("im").strings()
                )
            )
        }
        all = list.sortedBy { it.huName.lowercase() }
        byId = list.associateBy { it.id }
    }

    fun get(id: String): Exercise? = byId[id]

    fun nameOf(id: String): String = byId[id]?.huName ?: id
}

fun JSONArray.strings(): List<String> {
    val out = ArrayList<String>(length())
    for (i in 0 until length()) out.add(getString(i))
    return out
}
