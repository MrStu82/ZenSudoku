package com.mrstu82.zensudoku

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.store by preferencesDataStore("zen")

/** Everything the Options screen controls. Stored as one pipe-separated string so it loads in a single read. */
data class Settings(
    val theme: String = "classic",
    val season: Int = 0,          // 0 none, 1 sakura, 2 momiji
    val track: Int = 0,           // index into Tracks, -1 = silence
    val music: Int = 2,           // volume step 0..3
    val sfx: Int = 2,             // volume step 0..3
    val mistakeLimit: Int = 3,    // 0 = no limit
    val clarity: Boolean = true,
    val bloom: Boolean = true,
    val dots: Boolean = false,
    val haptics: Boolean = true,
    val calm: Boolean = false,
    val tidy: Boolean = true,
    val large: Boolean = false,
    val left: Boolean = false,
    val level: Difficulty = Difficulty.MEDIUM,
) {
    fun encode() = listOf(theme, season, track, music, sfx, mistakeLimit, clarity, bloom, dots, haptics, calm, tidy, large, left, level.name)
        .joinToString("|")

    companion object {
        fun decode(s: String?): Settings {
            val p = s?.split("|") ?: return Settings()
            if (p.size != 15) return Settings()
            fun b(i: Int) = p[i] == "true"
            return Settings(
                p[0].takeIf { it in Themes } ?: "classic", p[1].toInt(), p[2].toInt(), p[3].toInt(), p[4].toInt(), p[5].toInt(),
                b(6), b(7), b(8), b(9), b(10), b(11), b(12), b(13), Difficulty.valueOf(p[14]),
            )
        }
    }
}

class Score(
    val seed: String, val difficulty: Difficulty, val seconds: Int, val at: Long,
    val hinted: Boolean = false, val untimed: Boolean = false, val daily: Boolean = false,
) {
    /** Counts towards "best" times: timed and unaided. */
    val clean get() = !hinted && !untimed
    fun encode() = "$seed|${difficulty.name}|$seconds|$at|$hinted|$untimed|$daily"

    companion object {
        fun decode(s: String) = s.split("|").let { Score(it[0], Difficulty.valueOf(it[1]), it[2].toInt(), it[3].toLong(), it[4] == "true", it[5] == "true", it[6] == "true") }
    }
}

class Stats(val started: Int = 0, val won: Int = 0, val streak: Int = 0, val longest: Int = 0, val lastDaily: Long = -1)

private val SETTINGS = stringPreferencesKey("settings")
private val SCORES = stringSetPreferencesKey("scores")
private val RESUME = stringPreferencesKey("resume")
private val STARTED = intPreferencesKey("started")
private val WON = intPreferencesKey("won")
private val STREAK = intPreferencesKey("streak")
private val LONGEST = intPreferencesKey("longest")
private val LAST_DAILY = stringPreferencesKey("lastDaily")

/** Snapshot of the whole store; small enough to read synchronously before the first frame (no theme flash). */
class Saved(p: Preferences) {
    val settings = Settings.decode(p[SETTINGS])
    val scores = p[SCORES].orEmpty().mapNotNull { runCatching { Score.decode(it) }.getOrNull() }
    val resume = p[RESUME]
    val stats = Stats(p[STARTED] ?: 0, p[WON] ?: 0, p[STREAK] ?: 0, p[LONGEST] ?: 0, p[LAST_DAILY]?.toLong() ?: -1)
}

class Repo(private val ctx: Context) {
    fun load() = runBlocking { Saved(ctx.store.data.first()) }

    private fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) =
        runBlocking { ctx.store.edit(block) }

    fun saveSettings(s: Settings) = edit { it[SETTINGS] = s.encode() }
    fun saveResume(s: String?) = edit { if (s == null) it.remove(RESUME) else it[RESUME] = s }
    fun started() = edit { it[STARTED] = (it[STARTED] ?: 0) + 1 }

    /** Records a win; a daily win on the day after the last one extends the streak. */
    fun won(score: Score, today: Long) = edit {
        it[SCORES] = it[SCORES].orEmpty() + score.encode()
        it[WON] = (it[WON] ?: 0) + 1
        val last = it[LAST_DAILY]?.toLong() ?: -1
        if (score.daily && last != today) {
            val streak = if (last == today - 1) (it[STREAK] ?: 0) + 1 else 1
            it[STREAK] = streak; it[LONGEST] = maxOf(streak, it[LONGEST] ?: 0); it[LAST_DAILY] = "$today"
        }
    }
}
