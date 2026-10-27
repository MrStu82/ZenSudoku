package com.mrstu82.zensudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

enum class Screen { Menu, Game, Records }

class GameState(val puzzle: Puzzle) {
    val current = List(9) { r -> puzzle.givens[r].copyOf() }
    val notes = List(9) { List(9) { mutableSetOf<Int>() } }
    var selected by mutableStateOf<Int?>(null)
    var noteMode by mutableStateOf(false)
    var mistakes by mutableIntStateOf(0)
    var seconds by mutableIntStateOf(0)
    val mistakeLimit = 3
    var version by mutableIntStateOf(0)
        private set
    private val history = ArrayDeque<Pair<List<IntArray>, List<List<Set<Int>>>>>()

    fun counts(): IntArray { version; return IntArray(10).also { n -> current.forEach { r -> r.forEach { n[it]++ } } } }

    fun press(k: Any) {
        if (k == "note") { noteMode = !noteMode; return }
        if (k is Int) input(k)
    }

    fun input(n: Int) {
        val i = selected ?: return; val r = i / 9; val c = i % 9
        if (puzzle.givens[r][c] != 0) return
        history.addLast(current.map { it.copyOf() } to notes.map { row -> row.map { it.toSet() } })
        when {
            n == 0 -> { current[r][c] = 0; notes[r][c].clear() }
            noteMode -> { current[r][c] = 0; if (!notes[r][c].remove(n)) notes[r][c].add(n) }
            else -> { if (n != puzzle.solution[r][c]) mistakes++; current[r][c] = n; notes[r][c].clear() }
        }
        version++
    }

    fun undo() {
        val (cur, nts) = history.removeLastOrNull() ?: return
        cur.forEachIndexed { r, row -> row.copyInto(current[r]) }
        nts.forEachIndexed { r, row -> row.forEachIndexed { c, s -> notes[r][c].clear(); notes[r][c].addAll(s) } }
        version++
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Phase-1 prototype: `--es proto game|records` opens a screen with fixture data for screenshots.
        val start = when (intent.getStringExtra("proto")) { "game" -> Screen.Game; "records" -> Screen.Records; else -> Screen.Menu }
        setContent {
            var screen by remember { mutableStateOf(start) }
            val game = remember { Proto.game() }
            CompositionLocalProvider(LocalZen provides Themes.getValue("classic")) {
                Box(Modifier.fillMaxSize().background(LocalZen.current.bgBase)) {
                    ZenBackground()
                    Column(Modifier.fillMaxSize()) {
                        PlayerBar("Silent", playing = false)
                        when (screen) {
                            Screen.Menu -> MenuScreen { screen = it }
                            Screen.Game -> GameScreen(game)
                            Screen.Records -> RecordsScreen(Proto.scores, Proto.stats) { screen = Screen.Menu }
                        }
                    }
                }
            }
        }
    }
}

/** Fixture state for the phase-1 screenshots only. */
object Proto {
    fun game() = GameState(generate("K7Q2M9XA", Difficulty.MEDIUM)).apply {
        val empty = (0 until 81).filter { puzzle.givens[it / 9][it % 9] == 0 }
        empty.take(7).forEach { selected = it; input(puzzle.solution[it / 9][it % 9]) }
        selected = empty[9]; noteMode = true
        listOf(2, 5, 8).forEach { input(it) }
        noteMode = false
        selected = empty[12]; mistakes = 1; seconds = 277
    }

    val scores = listOf(
        Score("K7Q2M9XA", Difficulty.MEDIUM, 512, "21/10/2026"), Score("P3ZT8W1C", Difficulty.EASY, 248, "22/10/2026"),
        Score("M0RNL4KE", Difficulty.HARD, 1103, "23/10/2026"), Score("Q9V5EBN2", Difficulty.EASY, 301, "24/10/2026"),
        Score("ZH6WT3AU", Difficulty.MEDIUM, 655, "25/10/2026"), Score("D2X8FJ7Y", Difficulty.IMPOSSIBLE, 1847, "26/10/2026"),
    )
    val stats = Stats(started = 9, won = 6, streak = 3)
}
