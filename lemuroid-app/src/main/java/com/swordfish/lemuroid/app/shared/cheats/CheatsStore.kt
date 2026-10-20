package com.swordfish.lemuroid.app.shared.cheats

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Cheat(
    val name: String,
    val code: String,
    val enabled: Boolean,
)

/**
 * Keeps each game's cheat list in SharedPreferences. Cheats are entered by the user
 * (raw libretro cheat codes, such as Game Genie or GameShark, depending on the core).
 */
class CheatsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(gameId: Int): List<Cheat> {
        val raw = prefs.getString(key(gameId), null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map {
                val item = array.getJSONObject(it)
                Cheat(
                    name = item.optString("name"),
                    code = item.optString("code"),
                    enabled = item.optBoolean("enabled", false),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(
        gameId: Int,
        cheats: List<Cheat>,
    ) {
        val array = JSONArray()
        cheats.forEach {
            array.put(
                JSONObject()
                    .put("name", it.name)
                    .put("code", it.code)
                    .put("enabled", it.enabled),
            )
        }
        prefs.edit().putString(key(gameId), array.toString()).apply()
    }

    private fun key(gameId: Int) = "cheats_$gameId"

    companion object {
        private const val PREFS_NAME = "lemuroid_cheats"
    }
}
