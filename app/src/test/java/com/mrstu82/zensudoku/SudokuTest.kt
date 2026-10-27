package com.mrstu82.zensudoku

import org.junit.Assert.assertEquals
import org.junit.Test

/** Expected strings come from running the web version's Zen.generate() in node. */
class SudokuTest {
    private fun flat(b: List<IntArray>) = b.joinToString("") { it.joinToString("") }

    @Test fun matchesWebGenerator() {
        val m = generate("K7Q2M9XA", Difficulty.MEDIUM)
        assertEquals("295800000607095031001000005406052000308000952002008070064009180083700000500000704", flat(m.givens))
        assertEquals("295831647647295831831647295476952318318476952952318476764529183183764529529183764", flat(m.solution))
        val i = generate("d2x8fj7y", Difficulty.IMPOSSIBLE)
        assertEquals("000000804000050900000010372000100000000040000009003008007000000600000000080600207", flat(i.givens))
        assertEquals("916372854372854916854916372548169723723548169169723548237485691691237485485691237", flat(i.solution))
    }
}
