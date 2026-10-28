package com.mrstu82.zensudoku

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class GameState(val puzzle: Puzzle, val daily: Boolean = false) {
    val current = List(9) { r -> puzzle.givens[r].copyOf() }
    val notes = List(9) { List(9) { mutableSetOf<Int>() } }
    var selected by mutableStateOf<Int?>(null)
    var noteMode by mutableStateOf(false)
    /** Digit-first mode: pick a digit on the pad, then tap cells to place it. */
    var digitFirst by mutableStateOf(false)
    var armed by mutableStateOf<Int?>(null)
    var mistakes by mutableIntStateOf(0)
    var seconds by mutableIntStateOf(0)
    var hints by mutableIntStateOf(0)
    var paused by mutableStateOf(false)
    var version by mutableIntStateOf(0)
        private set
    private val history = ArrayDeque<Pair<List<IntArray>, List<List<Set<Int>>>>>()

    /** Set by the screen: mistake limit (0 = none), auto-tidy, and the placement feedback hook. */
    var mistakeLimit = 3
    var tidy = true
    var onPlace: (correct: Boolean) -> Unit = {}

    val solved get() = run { version; (0 until 81).all { current[it / 9][it % 9] == puzzle.solution[it / 9][it % 9] } }
    val lost get() = mistakeLimit > 0 && mistakes >= mistakeLimit

    fun counts(): IntArray { version; return IntArray(10).also { n -> current.forEach { r -> r.forEach { n[it]++ } } } }

    fun tapCell(i: Int) {
        selected = i
        if (digitFirst) armed?.let { input(it) }
    }

    fun press(k: Any) {
        when (k) {
            "note" -> noteMode = !noteMode
            "mode" -> { digitFirst = !digitFirst; armed = null }
            is Int -> if (digitFirst && k != 0) armed = k.takeIf { it != armed } else input(k)
        }
    }

    private fun snapshot() = history.addLast(current.map { it.copyOf() } to notes.map { row -> row.map { it.toSet() } })

    fun input(n: Int) {
        val i = selected ?: return; val r = i / 9; val c = i % 9
        if (puzzle.givens[r][c] != 0 || solved || lost) return
        if (n != 0 && !noteMode && current[r][c] == n) return
        snapshot()
        when {
            n == 0 -> { current[r][c] = 0; notes[r][c].clear() }
            noteMode -> { current[r][c] = 0; if (!notes[r][c].remove(n)) notes[r][c].add(n) }
            else -> {
                val ok = n == puzzle.solution[r][c]
                if (!ok) mistakes++
                current[r][c] = n; notes[r][c].clear()
                if (tidy) peers(r, c).forEach { notes[it / 9][it % 9].remove(n) }
                onPlace(ok)
            }
        }
        version++
    }

    private fun peers(r: Int, c: Int) = (0 until 9).flatMap { listOf(r * 9 + it, it * 9 + c, (r / 3 * 3 + it / 3) * 9 + c / 3 * 3 + it % 3) }

    /** Digits not yet used in the cell's row, column or box. */
    fun candidates(r: Int, c: Int): Set<Int> {
        version
        return (1..9).toSet() - peers(r, c).map { current[it / 9][it % 9] }.toSet()
    }

    fun singles() = (0 until 81).filter { current[it / 9][it % 9] == 0 && candidates(it / 9, it % 9).size == 1 }

    /** "Breath of clarity": fill every cell that has exactly one candidate. */
    fun fillSingles() {
        val s = singles().ifEmpty { return }
        snapshot()
        s.forEach { i -> current[i / 9][i % 9] = candidates(i / 9, i % 9).single(); notes[i / 9][i % 9].clear() }
        version++
    }

    /** Auto-pencil: every empty cell's notes become all its legal candidates; auto-tidy keeps them current. */
    fun autoPencil() {
        if (solved || lost) return
        snapshot()
        (0 until 81).filter { current[it / 9][it % 9] == 0 }.forEach { i -> notes[i / 9][i % 9].run { clear(); addAll(candidates(i / 9, i % 9)) } }
        version++
    }

    /** Reveals the selected cell (or the most constrained empty one); the score is then marked hinted. */
    fun hint() {
        if (solved || lost) return
        val empty = (0 until 81).filter { current[it / 9][it % 9] != puzzle.solution[it / 9][it % 9] }
        val i = selected?.takeIf { it in empty && puzzle.givens[it / 9][it % 9] == 0 }
            ?: empty.minByOrNull { candidates(it / 9, it % 9).size } ?: return
        snapshot()
        current[i / 9][i % 9] = puzzle.solution[i / 9][i % 9]; notes[i / 9][i % 9].clear()
        selected = i; hints++; version++
    }

    fun undo() {
        val (cur, nts) = history.removeLastOrNull() ?: return
        cur.forEachIndexed { r, row -> row.copyInto(current[r]) }
        nts.forEachIndexed { r, row -> row.forEachIndexed { c, s -> notes[r][c].clear(); notes[r][c].addAll(s) } }
        version++
    }

    /** Resume record: seed|level|daily|81 digits|notes per cell|mistakes|seconds|hints. */
    fun encode() = listOf(
        puzzle.seed, puzzle.difficulty.name, daily, current.joinToString("") { it.joinToString("") },
        notes.flatten().joinToString(",") { it.joinToString("") }, mistakes, seconds, hints,
    ).joinToString("|")

    companion object {
        fun decode(s: String) = runCatching {
            val p = s.split("|")
            GameState(generate(p[0], Difficulty.valueOf(p[1])), p[2] == "true").apply {
                p[3].forEachIndexed { i, ch -> current[i / 9][i % 9] = ch - '0' }
                p[4].split(",").forEachIndexed { i, ns -> ns.forEach { notes[i / 9][i % 9].add(it - '0') } }
                mistakes = p[5].toInt(); seconds = p[6].toInt(); hints = p[7].toInt(); paused = true
            }
        }.getOrNull()
    }
}
