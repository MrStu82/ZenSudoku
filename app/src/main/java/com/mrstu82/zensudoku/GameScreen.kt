package com.mrstu82.zensudoku

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun GameScreen(app: ZenApp, g: GameState) {
    val z = LocalZen.current; val s = app.settings; val view = LocalView.current
    SideEffect {
        g.mistakeLimit = if (s.calm) 0 else s.mistakeLimit; g.tidy = s.tidy
        g.onPlace = { ok ->
            if (ok) {
                app.audio.tick()
                if (s.haptics) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    }
    val solved = g.solved; val lost = g.lost
    LaunchedEffect(g, g.paused, solved, lost) {
        while (!g.paused && !solved && !lost) { delay(1000); g.seconds++ }
    }
    LaunchedEffect(g, solved) { if (solved) { app.win(g); app.audio.bell() } }
    val best = app.best(g.puzzle)

    Box(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp)) {
            // Web: grid is 95vw capped at min(420px, 44vh).
            val side = minOf(maxWidth, 420.dp, (LocalConfiguration.current.screenHeightDp * .44f).dp)
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f)) {
                        SvgIcon(Ic.PAUSE, z.textPrimary, 22.dp, stroke = 1.5f, modifier = Modifier.tap { g.paused = true }.padding(8.dp).alpha(.6f))
                        SvgIcon(Ic.HINT, z.textPrimary, 22.dp, stroke = 1.5f, modifier = Modifier.tap { g.hint() }.padding(8.dp).alpha(.6f))
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (s.calm) T("静", 28.sp, family = Brush, modifier = Modifier.alpha(.5f))
                        else {
                            val limit = if (g.mistakeLimit > 0) "/${g.mistakeLimit}" else ""
                            T("MISTAKES: ${g.mistakes}$limit", 9.sp, 700, spacing = .1.em, modifier = Modifier.alpha(.5f))
                            Spacer(Modifier.height(4.dp))
                            T(clock(g.seconds), 28.sp, 500)
                            if (best != null) T("BEST ${clock(best)}", 9.sp, 700, spacing = .1.em, modifier = Modifier.alpha(.5f))
                        }
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        if (s.bloom) SvgIcon(Ic.BLOOM, Matcha, 24.dp, stroke = 1.6f, modifier = Modifier.tap { g.fillSingles() }.padding(8.dp).alpha(if (g.singles().isEmpty()) .25f else .9f))
                        SvgIcon(Ic.UNDO, z.textPrimary, 22.dp, stroke = 1.5f, modifier = Modifier.tap { g.undo() }.padding(8.dp).alpha(.6f))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Board(g, s, side)
                Spacer(Modifier.height(8.dp))
                Numpad(g, s, side)
                Spacer(Modifier.weight(1f))
                SeedLine(g.puzzle)
            }
        }
        when {
            solved -> Completion(app, g)
            lost -> Veil("終", "THE INK HAS RUN") {
                ZenButton("Try again (same seed)", Ic.UNDO) { app.start(generate(g.puzzle.seed, g.puzzle.difficulty), g.daily) }
                ZenButton("Main menu", Ic.ARROW) { app.quit() }
            }
            g.paused -> Veil("休", "PAUSED") {
                ZenButton("Resume", Ic.PLAY) { g.paused = false }
                ZenButton("How to Play", Ic.RULES) { app.open(Screen.HowTo) }
                ZenButton("Options", Ic.OPTIONS) { app.open(Screen.Options) }
                ZenButton("Main menu", Ic.ARROW) { app.screen = Screen.Menu }
            }
        }
    }
}

/** Seed: faint, bottom-right, tap to copy. */
@Composable
private fun SeedLine(p: Puzzle) {
    val clip = LocalClipboardManager.current; var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) { if (copied) { delay(1500); copied = false } }
    Row(
        Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(horizontal = 8.dp).alpha(.4f)
            .tap { clip.setText(AnnotatedString(p.seed)); copied = true },
        horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically,
    ) {
        T(if (copied) "COPIED" else "SEED: ${p.seed} [${p.difficulty.name}]", 9.sp, 700, spacing = .1.em)
        SvgIcon(Ic.COPY, LocalZen.current.textPrimary, 12.dp, modifier = Modifier.padding(8.dp))
    }
}

/** Mist over the whole screen; the grid is hidden while it shows. */
@Composable
fun Veil(kanji: String, caption: String, content: @Composable ColumnScope.() -> Unit) {
    val z = LocalZen.current
    Box(Modifier.fillMaxSize().background(z.bgBase.copy(alpha = .97f)).tap {}.padding(32.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 360.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            T(kanji, 56.sp, family = Brush, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            T(caption, 10.sp, 500, z.textSecondary, spacing = .4.em, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp))
            content()
        }
    }
}

/** Ensō is brushed over the board, then the time is stamped like a hanko. */
@Composable
private fun Completion(app: ZenApp, g: GameState) {
    val z = LocalZen.current
    val brush = remember(g) { Animatable(0f) }; val stamp = remember(g) { Animatable(0f) }
    LaunchedEffect(g) { brush.animateTo(1f, tween(1600)); stamp.animateTo(1f, tween(260)) }
    Box(Modifier.fillMaxSize().background(z.bgBase.copy(alpha = .9f * brush.value)).tap {}.padding(32.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 360.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                Enso(Modifier.fillMaxSize(), brush.value)
                if (stamp.value > 0f) Box(
                    Modifier.align(Alignment.BottomEnd).scale(1.6f - .6f * stamp.value).alpha(stamp.value).rotate(-6f)
                        .background(Shu, RoundedCornerShape(4.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val tag = when { app.settings.calm -> "静"; g.hints > 0 -> "灯"; else -> clock(g.seconds) }
                    T(tag, 16.sp, 700, Color(0xFFFAF6EC))
                }
            }
            Spacer(Modifier.height(20.dp))
            T("完", 34.sp, family = Brush)
            T(g.puzzle.difficulty.label.uppercase(), 10.sp, 500, z.textSecondary, spacing = .3.em, modifier = Modifier.padding(bottom = 24.dp))
            Column(Modifier.alpha(stamp.value), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ZenButton("New journey", Ic.ARROW) { app.start(generate(randomSeed(), g.puzzle.difficulty)) }
                ZenButton("Records", Ic.STAR) { app.screen = Screen.Records }
                ZenButton("Main menu", Ic.ARROW) { app.quit() }
            }
        }
    }
}

@Composable
fun Board(g: GameState, s: Settings, side: Dp) {
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
                for (c in 0 until 9) Cell(g, s, r, c, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

/** Dice-pip layouts on a 3x3 lattice for 1..9 candidates. */
private val Pips = listOf(
    listOf(4), listOf(0, 8), listOf(0, 4, 8), listOf(0, 2, 6, 8), listOf(0, 2, 4, 6, 8),
    listOf(0, 2, 3, 5, 6, 8), listOf(0, 2, 3, 4, 5, 6, 8), listOf(0, 1, 2, 3, 5, 6, 7, 8), (0..8).toList(),
)

@Composable
private fun Cell(g: GameState, s: Settings, r: Int, c: Int, modifier: Modifier) {
    val z = LocalZen.current; g.version
    val v = g.current[r][c]; val given = g.puzzle.givens[r][c] != 0
    val sel = g.selected == r * 9 + c
    val wrong = v != 0 && !given && v != g.puzzle.solution[r][c]
    val sx = g.selected; val sr = (sx ?: 0) / 9; val sc = (sx ?: 0) % 9
    val peer = sx != null && (sr == r || sc == c || (sr / 3 == r / 3 && sc / 3 == c / 3))
    val focus = if (g.digitFirst) g.armed else sx?.let { g.current[sr][sc] }.takeIf { it != 0 }
    val same = v != 0 && v == focus
    val cands = if (v == 0 && g.notes[r][c].isEmpty()) g.candidates(r, c) else emptySet()
    // Ink bleeds in on placement; a mistake blushes vermilion, then settles.
    val ink = remember(v) { Animatable(if (v != 0 && !given) 0f else 1f) }
    LaunchedEffect(v) { ink.animateTo(1f, tween(if (wrong) 900 else 380)) }
    val bg = when {
        wrong -> Shu.copy(alpha = .1f + .25f * (1f - ink.value))
        sel -> z.selectionBg
        same -> z.sameNumBg
        peer -> z.peerBg
        given -> z.prefilledBg
        else -> Color.Transparent
    }
    // Single-candidate cells get a soft inner border ("inner clarity").
    val glow = if (s.clarity && cands.size == 1) Modifier.padding(2.dp).border(1.dp, Matcha.copy(alpha = .6f), RoundedCornerShape(3.dp)) else Modifier
    val digit = if (s.large) 24.sp else 20.sp
    val ring = if (sel) Modifier.border(2.dp, z.accent) else Modifier
    Box(modifier.background(bg).then(ring).tap { g.tapCell(r * 9 + c) }, contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().then(glow))
        when {
            v != 0 && given -> T("$v", digit, 800, z.textPrimary)
            v != 0 -> T("$v", digit, if (same) 700 else 400, if (wrong) Shu else z.accent, modifier = Modifier.alpha(ink.value).scale(1.15f - .15f * ink.value))
            // Up to 4 pencil notes, one per quadrant, in entry order.
            g.notes[r][c].isNotEmpty() -> Column(Modifier.fillMaxSize().padding(3.dp)) {
                val ns = g.notes[r][c].toList()
                for (qr in 0 until 2) Row(Modifier.weight(1f)) {
                    for (qc in 0 until 2) Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        ns.getOrNull(qr * 2 + qc)?.let { T("$it", if (s.large) 12.sp else 10.sp, 500, z.textSecondary) }
                    }
                }
            }
            s.dots && cands.isNotEmpty() -> Canvas(Modifier.fillMaxSize(.5f)) {
                val step = size.width / 3; val pip = z.textSecondary.copy(alpha = .35f)
                Pips[cands.size - 1].forEach { drawCircle(pip, step * .16f, Offset(step * (it % 3 + .5f), step * (it / 3 + .5f))) }
            }
        }
    }
}

@Composable
fun Numpad(g: GameState, s: Settings, width: Dp) {
    val z = LocalZen.current; val counts = g.counts()
    val sel = g.selected?.takeIf { g.current[it / 9][it % 9] == 0 && !g.digitFirst }
    val allowed = sel?.let { g.candidates(it / 9, it % 9) }
    val controls = listOf("mode", "note", 0)
    val keys: List<Any> = (1..6).toList() + if (s.left) controls + (7..9) else (7..9) + controls
    Column(Modifier.width(width), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    val shape = RoundedCornerShape(4.dp)
                    val active = (k == "note" && g.noteMode) || (k == "mode" && g.digitFirst) || (k == g.armed)
                    val on = if (active) z.boardBg else z.textSecondary
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
                            "note" -> SvgIcon(Ic.PENCIL, on, 20.dp, stroke = 1.5f)
                            "mode" -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                SvgIcon(Ic.MODE, on, 20.dp, stroke = 1.5f, modifier = Modifier.alpha(.7f))
                                T(if (g.digitFirst) "DIGIT" else "CELL", 7.sp, 600, on, modifier = Modifier.padding(top = 4.dp).alpha(.8f))
                            }
                            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val ink = if (active) z.boardBg else z.textPrimary
                                T("$k", if (s.large) 26.sp else 22.sp, 500, ink)
                                T("${9 - counts[k as Int]}", 8.sp, 600, ink, modifier = Modifier.alpha(.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}
