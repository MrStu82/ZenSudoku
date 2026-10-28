package com.mrstu82.zensudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTest {
    private fun game() = GameState(generate("K7Q2M9XA", Difficulty.MEDIUM))
    private fun GameState.empty() = (0 until 81).filter { puzzle.givens[it / 9][it % 9] == 0 }

    @Test fun resumeRoundTrip() {
        val g = game(); val e = g.empty()
        g.selected = e[0]; g.input(g.puzzle.solution[e[0] / 9][e[0] % 9])
        g.noteMode = true; g.selected = e[1]; g.input(3); g.input(7)
        g.seconds = 99; g.hint()
        val back = GameState.decode(g.encode())!!
        assertEquals(g.encode(), back.encode())
        assertTrue(back.paused)
    }

    @Test fun autoTidyClearsPeerNotes() {
        val g = game(); val e = g.empty(); val i = e[0]; val n = g.puzzle.solution[i / 9][i % 9]
        val peer = e.first { it != i && it / 9 == i / 9 }
        g.noteMode = true; g.selected = peer; g.input(n)
        g.noteMode = false; g.selected = i; g.input(n)
        assertFalse(n in g.notes[peer / 9][peer % 9])
    }

    @Test fun autoPencilFillsCandidatesAndTidies() {
        val g = game(); val e = g.empty()
        g.autoPencil()
        e.forEach { assertEquals(g.candidates(it / 9, it % 9), g.notes[it / 9][it % 9].toSet()) }
        val i = e[0]; val n = g.puzzle.solution[i / 9][i % 9]
        g.selected = i; g.input(n)
        assertTrue(e.filter { it != i && it / 9 == i / 9 }.none { n in g.notes[it / 9][it % 9] })
        val j = e.first { g.notes[it / 9][it % 9].size > 1 }; val m = g.notes[j / 9][j % 9].first()
        g.noteMode = true; g.selected = j; g.input(m)
        assertFalse(m in g.notes[j / 9][j % 9])
    }

    @Test fun digitFirstPlacesArmedDigit() {
        val g = game(); val i = g.empty()[0]; val n = g.puzzle.solution[i / 9][i % 9]
        g.press("mode"); g.press(n); g.tapCell(i)
        assertEquals(n, g.current[i / 9][i % 9])
    }
}
