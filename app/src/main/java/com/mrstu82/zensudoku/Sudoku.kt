package com.mrstu82.zensudoku

enum class Difficulty(val label: String, val kanji: String, val holes: Int, val plain: String) {
    EASY("Pebble (Easy)", "石", 34, "Easy"), MEDIUM("Bamboo (Medium)", "竹", 46, "Medium"),
    HARD("Stone (Hard)", "岩", 56, "Hard"), IMPOSSIBLE("Mountain (Impossible)", "山", 62, "Expert"),
}

class Puzzle(val seed: String, val difficulty: Difficulty, val givens: List<IntArray>, val solution: List<IntArray>)

/** 8-char level seed in the web version's alphabet (base-36, upper case). */
const val SEED_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

fun randomSeed(): String = (1..8).map { SEED_CHARS.random() }.joinToString("")

/** Same seed for everyone on a given day (java.util.Random is specified, so stable across devices). */
fun dailySeed(day: java.time.LocalDate = java.time.LocalDate.now()): String =
    java.util.Random(day.toEpochDay()).let { r -> (1..8).map { SEED_CHARS[r.nextInt(36)] }.joinToString("") }

/**
 * Port of the web version's Zen.generate(): same string hash, PRNG and shuffle order,
 * so a seed + difficulty gives the identical board on web and Android.
 */
fun generate(seedIn: String, difficulty: Difficulty): Puzzle {
    val seed = seedIn.uppercase()
    var rs = seed.fold(0) { h, c -> 31 * h + c.code }
    fun rnd(): Double {
        rs += 0x9e3779b9.toInt()
        var t = rs xor (rs ushr 16)
        t *= 0x21f0aaad
        t = t xor (t ushr 15)
        t *= 0x735a2d97
        t = t xor (t ushr 15)
        return (t.toLong() and 0xffffffffL) / 4294967296.0
    }
    fun <T> shuf(a: MutableList<T>) {
        for (i in a.size - 1 downTo 1) {
            val j = (rnd() * (i + 1)).toInt()
            a[i] = a[j].also { a[j] = a[i] }
        }
    }

    val base = listOf(
        intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9), intArrayOf(4, 5, 6, 7, 8, 9, 1, 2, 3), intArrayOf(7, 8, 9, 1, 2, 3, 4, 5, 6),
        intArrayOf(2, 3, 1, 5, 6, 4, 8, 9, 7), intArrayOf(5, 6, 4, 8, 9, 7, 2, 3, 1), intArrayOf(8, 9, 7, 2, 3, 1, 5, 6, 4),
        intArrayOf(3, 1, 2, 6, 4, 5, 9, 7, 8), intArrayOf(6, 4, 5, 9, 7, 8, 3, 1, 2), intArrayOf(9, 7, 8, 3, 1, 2, 6, 4, 5),
    ).toMutableList()
    for (i in 0 until 3) {
        val idx = mutableListOf(i * 3, i * 3 + 1, i * 3 + 2); shuf(idx)
        val rows = idx.map { base[it] }
        for (k in 0 until 3) base[i * 3 + k] = rows[k]
    }
    val map = (1..9).toMutableList(); shuf(map)
    val solution = base.map { r -> IntArray(9) { map[r[it] - 1] } }

    val givens = solution.map { it.copyOf() }
    val cells = (0 until 81).toMutableList(); shuf(cells)
    for (i in 0 until difficulty.holes) givens[cells[i] / 9][cells[i] % 9] = 0
    return Puzzle(seed, difficulty, givens, solution)
}
