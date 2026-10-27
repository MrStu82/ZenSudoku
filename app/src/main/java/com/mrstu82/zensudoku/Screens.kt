package com.mrstu82.zensudoku

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// ---------- shared pieces (web: .zen-bg, .player-bar, .glass-card, .btn-zen) ----------

/** Web taps have no ripple or focus fill. */
fun Modifier.tap(onClick: () -> Unit) = clickable(interactionSource = null, indication = null, onClick = onClick)

@Composable
fun T(
    s: String, size: TextUnit, weight: Int = 500, color: Color = LocalZen.current.textPrimary,
    spacing: TextUnit = 0.sp, family: FontFamily = Mincho, align: TextAlign = TextAlign.Start, modifier: Modifier = Modifier,
) = BasicText(
    s, modifier,
    style = TextStyle(color = color, fontSize = size, fontWeight = FontWeight(weight), letterSpacing = spacing, fontFamily = family, textAlign = align),
)

/** Seigaiha (blue-sea-wave) pattern: overlapping fans of concentric arcs, 5% ink. */
@Composable
fun ZenBackground() {
    val z = LocalZen.current
    Canvas(Modifier.fillMaxSize()) {
        val r = 28.dp.toPx(); val ink = z.textPrimary.copy(alpha = .05f); val st = Stroke(1.dp.toPx())
        var row = 0; var y = 0f
        while (y < size.height + r) {
            var x = if (row % 2 == 0) 0f else r
            while (x < size.width + r) {
                val c = Offset(x, y)
                drawCircle(z.bgBase, r, c)
                for (k in 1..4) drawCircle(ink, r * k / 4.3f, c, style = st)
                drawCircle(ink, r, c, style = st)
                x += 2 * r
            }
            row++; y += r / 2
        }
        // Washi fibres and specks, fixed seed so the paper never "moves".
        val rnd = java.util.Random(7); val fibre = z.textPrimary.copy(alpha = .05f)
        repeat((size.width * size.height / 2500f).toInt()) {
            val p = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
            if (rnd.nextInt(3) == 0) drawCircle(fibre, (.4f + rnd.nextFloat()) * density, p)
            else {
                val a = rnd.nextFloat() * 6.283f; val l = (6 + rnd.nextFloat() * 18) * density
                drawLine(fibre, p, p + Offset(kotlin.math.cos(a) * l, kotlin.math.sin(a) * l), .6f * density)
            }
        }
    }
}

/** Brush ensō: one open stroke that thins and dries out towards its tail. */
@Composable
fun Enso(modifier: Modifier) {
    val ink = LocalZen.current.textPrimary
    Canvas(modifier) {
        val n = 140; val start = 115f; val sweep = 318f; val maxW = size.minDimension * .085f
        val inset = maxW; val box = Size(size.width - 2 * inset, size.height - 2 * inset)
        for (i in 0 until n) {
            val t = i / n.toFloat()
            val w = maxW * (1f - .7f * t) * (.92f + .08f * kotlin.math.sin(t * 23f))
            drawArc(
                ink.copy(alpha = .85f * (1f - .45f * t * t)), start + sweep * t, sweep / n + .8f, false,
                Offset(inset, inset), box, style = Stroke(w, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
fun PlayerBar(track: String, playing: Boolean) {
    val z = LocalZen.current
    // Unobtrusive: no surface, no divider, sits below the status bar at 45% ink.
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp).height(32.dp).padding(horizontal = 24.dp).alpha(.45f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        T(track, 12.sp, 500, spacing = .1.em)
        if (playing) T("  ·  playing", 10.sp, 400, z.textSecondary)
        Spacer(Modifier.weight(1f))
        SvgIcon(Ic.SPEAKER, z.textPrimary, 16.dp, stroke = 1.5f, modifier = Modifier.padding(6.dp))
        SvgIcon(if (playing) Ic.PAUSE else Ic.PLAY, z.textPrimary, 16.dp, stroke = 1.5f, modifier = Modifier.padding(6.dp))
    }
}

private val CardShape = RoundedCornerShape(6.dp)

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val z = LocalZen.current
    Column(
        modifier.shadow(6.dp, CardShape, ambientColor = Color.Black.copy(alpha = .06f), spotColor = Color.Black.copy(alpha = .08f))
            .background(z.glass, CardShape).border(1.dp, z.gridLine, CardShape),
        content = content,
    )
}

@Composable
fun ZenButton(label: String, icon: String, onClick: () -> Unit) {
    val z = LocalZen.current; val shape = RoundedCornerShape(4.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(z.glass, shape).border(1.dp, z.gridLine, shape)
            .tap(onClick).padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
    ) {
        T(label, 17.sp, 500)
        SvgIcon(icon, z.textSecondary, 18.dp, stroke = 1.5f)
    }
}

/** Web: glass-card "Return" pill with no border/shadow. */
@Composable
fun ReturnButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier.fillMaxWidth().clip(shape).background(LocalZen.current.glass, shape).tap(onClick).padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) { T("Return", 16.sp, 500) }
}

fun clock(s: Int) = "%02d:%02d".format(s / 60, s % 60)

// ---------- menu ----------

@Composable
fun MenuScreen(go: (Screen) -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 384.dp).fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                    Enso(Modifier.fillMaxSize())
                    T("禅", 64.sp, family = Brush)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    T("Zen Sudoku", 34.sp, family = Brush)
                    Spacer(Modifier.width(10.dp))
                    // Hanko seal, like a signature on a scroll.
                    Box(Modifier.size(26.dp).background(Shu, RoundedCornerShape(3.dp)), contentAlignment = Alignment.Center) {
                        T("数独", 9.sp, 700, Color(0xFFFAF6EC))
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            var level by remember { mutableStateOf(Difficulty.MEDIUM) }
            DifficultyPicker(level) { level = it }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ZenButton("Begin Journey", Ic.ARROW) { go(Screen.Game) }
                ZenButton("Daily Seed · ${dailySeed()}", Ic.SUN) { go(Screen.Game) }
                ZenButton("Recall Seed", Ic.ARROW) {}
                ZenButton("How to Play", Ic.RULES) {}
                ZenButton("Records", Ic.STAR) { go(Screen.Records) }
                ZenButton("Options", Ic.OPTIONS) {}
            }
        }
    }
}

/** Menu difficulty: labelled row of tiles; the chosen one is filled indigo with a vermilion mark. */
@Composable
fun DifficultyPicker(selected: Difficulty, pick: (Difficulty) -> Unit) {
    val z = LocalZen.current; val shape = RoundedCornerShape(4.dp)
    val plain = mapOf(Difficulty.EASY to "Easy", Difficulty.MEDIUM to "Medium", Difficulty.HARD to "Hard", Difficulty.IMPOSSIBLE to "Expert")
    Column {
        T("DIFFICULTY", 11.sp, 600, z.textSecondary, spacing = .3.em, modifier = Modifier.padding(start = 2.dp, bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Difficulty.entries.forEach { d ->
                val on = d == selected; val ink = if (on) z.boardBg else z.textPrimary
                Column(
                    Modifier.weight(1f).clip(shape).background(if (on) z.accent else z.glass, shape)
                        .border(if (on) 2.dp else 1.dp, if (on) z.accent else z.gridLine, shape)
                        .tap { pick(d) }.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    T(d.kanji, 22.sp, 500, ink)
                    T(d.label.substringBefore(' '), 11.sp, 500, ink.copy(alpha = .8f))
                    T(plain.getValue(d), 13.sp, 700, ink, modifier = Modifier.padding(top = 2.dp))
                    Box(Modifier.padding(top = 6.dp).size(5.dp).background(if (on) Shu else Color.Transparent, RoundedCornerShape(50)))
                }
            }
        }
    }
}

/** Segmented pill row (records filter). */
@Composable
fun <K> Chips(items: List<K>, selected: K, label: (K) -> String, pick: (K) -> Unit) {
    val z = LocalZen.current; val shape = RoundedCornerShape(4.dp)
    Row(Modifier.fillMaxWidth().clip(shape).background(z.glass, shape).border(1.dp, z.gridLine, shape).padding(4.dp)) {
        items.forEach { k ->
            val on = k == selected
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(3.dp)).background(if (on) z.accent else Color.Transparent)
                    .tap { pick(k) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) { T(label(k), 12.sp, 500, if (on) Color(0xFFFAF6EC) else z.textSecondary, align = TextAlign.Center) }
        }
    }
}

// ---------- game ----------

@Composable
fun GameScreen(g: GameState, best: Int?) {
    val z = LocalZen.current
    BoxWithConstraints(Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 24.dp)) {
        // Web: grid is 95vw capped at min(420px, 44vh).
        val side = minOf(maxWidth, 420.dp, (LocalConfiguration.current.screenHeightDp * .44f).dp)
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f)) { SvgIcon(Ic.PAUSE, z.textPrimary, 22.dp, stroke = 1.5f, modifier = Modifier.padding(8.dp).alpha(.6f)) }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    T("MISTAKES: ${g.mistakes}/${g.mistakeLimit}", 9.sp, 700, spacing = .1.em, modifier = Modifier.alpha(.5f))
                    Spacer(Modifier.height(4.dp))
                    T(clock(g.seconds), 28.sp, 500)
                    if (best != null) T("BEST ${clock(best)}", 9.sp, 700, spacing = .1.em, modifier = Modifier.alpha(.5f))
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    SvgIcon(Ic.BLOOM, Matcha, 24.dp, stroke = 1.6f, modifier = Modifier.tap { g.fillSingles() }.padding(8.dp).alpha(if (g.singles().isEmpty()) .25f else .9f))
                    SvgIcon(Ic.UNDO, z.textPrimary, 22.dp, stroke = 1.5f, modifier = Modifier.tap { g.undo() }.padding(8.dp).alpha(.6f))
                }
            }
            Spacer(Modifier.height(12.dp))
            Board(g, side)
            Spacer(Modifier.height(8.dp))
            Numpad(g, side)
            Spacer(Modifier.weight(1f))
            // Seed: faint, bottom-right, tap to copy.
            Row(
                Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(horizontal = 8.dp).alpha(.4f),
                horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically,
            ) {
                T("SEED: ${g.puzzle.seed} [${g.puzzle.difficulty.name}]", 9.sp, 700, spacing = .1.em)
                SvgIcon(Ic.COPY, z.textPrimary, 12.dp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun Board(g: GameState, side: Dp) {
    val z = LocalZen.current; val shape = RoundedCornerShape(3.dp)
    Box(
        Modifier.size(side).shadow(8.dp, shape, ambientColor = Color.Black.copy(alpha = .1f), spotColor = Color.Black.copy(alpha = .2f))
            .background(z.boardBg, shape).border(2.dp, z.gridMajor, shape).padding(2.dp)
            .drawBehind {
                val c = size.width / 9
                for (i in 1 until 9) {
                    val major = i % 3 == 0
                    val col = if (major) z.gridMajor else z.gridLine; val w = (if (major) 2 else 1).dp.toPx()
                    drawLine(col, Offset(i * c, 0f), Offset(i * c, size.height), w)
                    drawLine(col, Offset(0f, i * c), Offset(size.width, i * c), w)
                }
            },
    ) {
        Column {
            for (r in 0 until 9) Row(Modifier.weight(1f)) {
                for (c in 0 until 9) Cell(g, r, c, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun Cell(g: GameState, r: Int, c: Int, modifier: Modifier) {
    val z = LocalZen.current; g.version
    val v = g.current[r][c]; val given = g.puzzle.givens[r][c] != 0
    val sel = g.selected == r * 9 + c
    val wrong = v != 0 && !given && v != g.puzzle.solution[r][c]
    val s = g.selected; val sr = (s ?: 0) / 9; val sc = (s ?: 0) % 9
    val peer = s != null && (sr == r || sc == c || (sr / 3 == r / 3 && sc / 3 == c / 3))
    val same = s != null && v != 0 && v == g.current[sr][sc]
    val single = v == 0 && g.notes[r][c].isEmpty() && g.candidates(r, c).size == 1
    val bg = when {
        wrong -> Shu.copy(alpha = .16f)
        sel -> z.selectionBg
        same -> z.sameNumBg
        peer -> z.textPrimary.copy(alpha = .04f)
        given -> z.prefilledBg
        else -> Color.Transparent
    }
    // Single-candidate cells get a soft inner border ("inner clarity").
    val glow = if (single) Modifier.padding(2.dp).border(1.dp, Matcha.copy(alpha = .6f), RoundedCornerShape(3.dp)) else Modifier
    Box(modifier.background(bg).tap { g.selected = r * 9 + c }, contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().then(glow))
        when {
            v != 0 && given -> T("$v", 20.sp, 800, z.textPrimary)
            v != 0 -> T("$v", 20.sp, 400, if (wrong) Shu else z.accent)
            // Up to 4 pencil notes, one per quadrant, in entry order.
            g.notes[r][c].isNotEmpty() -> Column(Modifier.fillMaxSize().padding(3.dp)) {
                val ns = g.notes[r][c].toList()
                for (qr in 0 until 2) Row(Modifier.weight(1f)) {
                    for (qc in 0 until 2) Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        ns.getOrNull(qr * 2 + qc)?.let { T("$it", 10.sp, 500, z.textSecondary) }
                    }
                }
            }
        }
    }
}

@Composable
fun Numpad(g: GameState, width: Dp) {
    val z = LocalZen.current; val counts = g.counts()
    val sel = g.selected?.takeIf { g.current[it / 9][it % 9] == 0 }
    val allowed = sel?.let { g.candidates(it / 9, it % 9) }
    val keys: List<Any> = (1..9).toList() + listOf("mode", "note", 0)
    Column(Modifier.width(width), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    val shape = RoundedCornerShape(4.dp)
                    val active = k == "note" && g.noteMode
                    val done = k is Int && k != 0 && counts[k] >= 9
                    val blocked = k is Int && k != 0 && allowed != null && k !in allowed
                    Box(
                        Modifier.weight(1f).aspectRatio(1f).alpha(if (done || blocked) .3f else 1f).clip(shape)
                            .background(if (active) z.accent else z.glass, shape).border(1.dp, z.gridLine, shape)
                            .tap { g.press(k) },
                        contentAlignment = Alignment.Center,
                    ) {
                        when (k) {
                            0 -> SvgIcon(Ic.ERASE, Shu, 20.dp, stroke = 1.5f)
                            "note" -> SvgIcon(Ic.PENCIL, if (active) z.boardBg else z.textSecondary, 20.dp, stroke = 1.5f)
                            "mode" -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                SvgIcon(Ic.MODE, z.textSecondary, 20.dp, stroke = 1.5f, modifier = Modifier.alpha(.7f))
                                T("MODE", 7.sp, 600, z.textSecondary, modifier = Modifier.padding(top = 4.dp).alpha(.6f))
                            }
                            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                T("$k", 22.sp, 500)
                                T("${9 - counts[k as Int]}", 8.sp, 600, modifier = Modifier.alpha(.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- records ----------

class Score(val seed: String, val difficulty: Difficulty, val seconds: Int, val date: String)
class Stats(val started: Int, val won: Int, val streak: Int)

@Composable
fun RecordsScreen(scores: List<Score>, stats: Stats, back: () -> Unit) {
    val z = LocalZen.current
    Box(Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.widthIn(max = 440.dp).fillMaxWidth()) {
            GlassCard(Modifier.fillMaxWidth().heightIn(max = (LocalConfiguration.current.screenHeightDp * .9f).dp)) {
                Column(Modifier.padding(32.dp)) {
                    T("記録", 34.sp, family = Brush, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    T("RECORDS", 10.sp, 500, z.textSecondary, spacing = .4.em, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(24.dp))
                    val rate = if (stats.started > 0) Math.round(stats.won * 100f / stats.started) else 0
                    Row(Modifier.fillMaxWidth().alpha(.6f), horizontalArrangement = Arrangement.SpaceAround) {
                        T("Wins: ${stats.won}", 12.sp, 700); T("Streak: ${stats.streak}", 12.sp, 700); T("Win Rate: $rate%", 12.sp, 700)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Difficulty.entries.forEach { d ->
                            val best = scores.filter { it.difficulty == d }.minOfOrNull { it.seconds }
                            Column(
                                Modifier.weight(1f).clip(RoundedCornerShape(3.dp)).background(z.textPrimary.copy(alpha = .045f)).padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                T(d.label.substringBefore(' ').uppercase(), 8.sp, 700, spacing = .1.em, modifier = Modifier.alpha(.5f))
                                T(best?.let { "%d:%02d".format(it / 60, it % 60) } ?: "–", 13.sp, 600)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        itemsIndexed(scores.sortedBy { it.seconds }.take(30)) { i, s ->
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(3.dp)).background(z.textPrimary.copy(alpha = .045f)).padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    T("#${i + 1} ${s.seed}", 12.sp, 700, modifier = Modifier.alpha(.3f))
                                    T(s.difficulty.name, 9.sp, 700, modifier = Modifier.alpha(.6f))
                                    T(s.date, 8.sp, 500, modifier = Modifier.alpha(.4f))
                                }
                                T("%d:%02d".format(s.seconds / 60, s.seconds % 60), 14.sp, 600)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    ReturnButton(back)
                }
            }
        }
    }
}
