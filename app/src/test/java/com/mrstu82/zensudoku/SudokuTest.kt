package com.mrstu82.zensudoku

import org.junit.Assert.assertEquals
import org.junit.Test

/** Expected solutions come from running the web version's Zen.generate() in node. */
class SudokuTest {
    private fun flat(b: List<IntArray>) = b.joinToString("") { it.joinToString("") }

    @Test fun solutionMatchesWebGenerator() {
        assertEquals("295831647647295831831647295476952318318476952952318476764529183183764529529183764", flat(generate("K7Q2M9XA", Difficulty.MEDIUM).solution))
        assertEquals("916372854372854916854916372548169723723548169169723548237485691691237485485691237", flat(generate("d2x8fj7y", Difficulty.IMPOSSIBLE).solution))
    }

    @Test fun puzzlesHaveExactlyOneSolution() {
        assertEquals(1, countSolutions(generate("S3V6A0BD", Difficulty.MEDIUM).givens))
        val rnd = java.util.Random(9982)
        for (d in Difficulty.values()) {
            val holes = (1..25).map {
                val p = generate((1..8).map { SEED_CHARS[rnd.nextInt(36)] }.joinToString(""), d)
                assertEquals(1, countSolutions(p.givens))
                p.givens.sumOf { r -> r.count { it == 0 } }
            }
            println("$d target ${d.holes} holes min ${holes.min()} avg ${holes.average()} max ${holes.max()}")
        }
    }
}
