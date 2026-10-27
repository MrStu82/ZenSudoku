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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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

@Composable
fun T(
    s: String, size: TextUnit, weight: Int = 500, color: Color = LocalZen.current.textPrimary,
    spacing: TextUnit = 0.sp, family: FontFamily = Quicksand, align: TextAlign = TextAlign.Start, modifier: Modifier = Modifier,
) = BasicText(
    s, modifier,
    style = TextStyle(color = color, fontSize = size, fontWeight = FontWeight(weight), letterSpacing = spacing, fontFamily = family, textAlign = align),
)

/** Ripple pattern: r40 and r10 circles on a 100px tile, 3% ink. */
@Composable
fun ZenBackground() = Canvas(Modifier.fillMaxSize()) {
    val tile = 100.dp.toPx(); val ink = Color.Black.copy(alpha = .03f); val st = Stroke(1.5.dp.toPx())
    var y = 0f
    while (y < size.height) {
        var x = 0f
        while (x < size.width) {
            val c = Offset(x + tile / 2, y + tile / 2)
            drawCircle(ink, 40.dp.toPx(), c, style = st); drawCircle(ink, 10.dp.toPx(), c, style = st)
            x += tile
        }
        y += tile
    }
}

@Composable
fun PlayerBar(track: String, playing: Boolean) {
    val z = LocalZen.current
    Row(
        Modifier.fillMaxWidth().background(z.glass)
            .drawBehind { drawLine(z.gridLine, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }
            .statusBarsPadding().height(60.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.width(90.dp)) {
            T(track.uppercase(), 9.sp, 700, spacing = .1.em, modifier = Modifier.alpha(.4f))
            T(if (playing) "Harmony Active" else "Paused", 8.sp, 700, modifier = Modifier.alpha(.6f))
        }
        Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.Black.copy(alpha = .05f)))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SvgIcon(Ic.SPEAKER, z.textPrimary, 20.dp, modifier = Modifier.padding(8.dp).alpha(.5f))
            SvgIcon(if (playing) Ic.PAUSE else Ic.PLAY, z.textPrimary, 22.dp, modifier = Modifier.padding(8.dp).alpha(.5f))
        }
    }
}

private val CardShape = RoundedCornerShape(32.dp)

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val z = LocalZen.current
    Column(
        modifier.shadow(14.dp, CardShape, ambientColor = Color.Black.copy(alpha = .1f), spotColor = Color.Black.copy(alpha = .1f))
            .background(z.glass, CardShape).border(1.dp, z.gridLine, CardShape),
        content = content,
    )
}

@Composable
fun ZenButton(label: String, icon: String, onClick: () -> Unit) {
    val z = LocalZen.current; val shape = RoundedCornerShape(24.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(z.boardBg, shape).border(1.dp, z.gridLine, shape)
            .clickable(onClick = onClick).padding(17.6.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
    ) {
        T(label, 17.sp, 700)
        SvgIcon(icon, z.textPrimary, 20.dp)
    }
}

/** Web: glass-card "Return" pill with no border/shadow. */
@Composable
fun ReturnButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier.fillMaxWidth().clip(shape).background(LocalZen.current.glass, shape).clickable(onClick = onClick).padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) { T("Return", 16.sp, 700) }
}

fun clock(s: Int) = "%02d:%02d".format(s / 60, s % 60)

// ---------- menu ----------

@Composable
fun MenuScreen(go: (Screen) -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 384.dp).fillMaxWidth()) {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    T("ZEN", 60.sp, 700, spacing = (-.05).em)
                    Spacer(Modifier.height(8.dp))
                    T("S U D O K U", 12.sp, 500, spacing = .4.em, modifier = Modifier.alpha(.6f))
                }
            }
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZenButton("Begin Journey", Ic.ARROW) { go(Screen.Game) }
                ZenButton("Recall Seed", Ic.ARROW) {}
                ZenButton("Rules", Ic.RULES) {}
                ZenButton("Records", Ic.STAR) { go(Screen.Records) }
                ZenButton("Options", Ic.OPTIONS) {}
            }
        }
    }
}

// ---------- game ----------

@Composable
fun GameScreen(g: GameState) {
    val z = LocalZen.current
    BoxWithConstraints(Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 24.dp)) {
        // Web: grid is 95vw capped at min(420px, 44vh).
        val side = minOf(maxWidth, 420.dp, (LocalConfiguration.current.screenHeightDp * .44f).dp)
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                SvgIcon(Ic.PAUSE, z.textPrimary, 24.dp, modifier = Modifier.padding(8.dp).alpha(.6f))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    T("MISTAKES: ${g.mistakes}/${g.mistakeLimit}", 9.sp, 700, spacing = .1.em, modifier = Modifier.alpha(.5f))
                    Spacer(Modifier.height(4.dp))
                    T(clock(g.seconds), 30.sp, 700, spacing = (-.05).em)
                }
                SvgIcon(Ic.UNDO, z.textPrimary, 24.dp, modifier = Modifier.clickable { g.undo() }.padding(8.dp).alpha(.6f))
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
    val z = LocalZen.current; val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier.size(side).shadow(20.dp, shape, ambientColor = Color.Black.copy(alpha = .1f), spotColor = Color.Black.copy(alpha = .2f))
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
    val bg = when {
        wrong -> Color(0x33D63031)
        sel -> z.selectionBg
        given -> z.prefilledBg
        else -> Color.Transparent
    }
    Box(modifier.background(bg).clickable { g.selected = r * 9 + c }, contentAlignment = Alignment.Center) {
        when {
            v != 0 -> T(
                "$v", 17.sp, 700, color = when { wrong -> Color(0xFFD63031); given -> z.textPrimary; else -> z.accent },
                family = if (given) Quicksand else Mono,
            )
            g.notes[r][c].isNotEmpty() -> Column(Modifier.fillMaxSize().padding(2.dp)) {
                for (nr in 0 until 3) Row(Modifier.weight(1f)) {
                    for (nc in 0 until 3) Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        val n = nr * 3 + nc + 1
                        if (n in g.notes[r][c]) T("$n", 8.sp, 600, z.textSecondary, family = Mono)
                    }
                }
            }
        }
    }
}

@Composable
fun Numpad(g: GameState, width: Dp) {
    val z = LocalZen.current; val counts = g.counts()
    val keys: List<Any> = (1..9).toList() + listOf("mode", "note", 0)
    Column(Modifier.width(width), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    val shape = RoundedCornerShape(12.dp)
                    val active = k == "note" && g.noteMode
                    val done = k is Int && k != 0 && counts[k] >= 9
                    Box(
                        Modifier.weight(1f).aspectRatio(1f).alpha(if (done) .3f else 1f).clip(shape)
                            .background(if (active) z.accent else z.glass, shape).border(1.dp, z.gridLine, shape)
                            .clickable { g.press(k) },
                        contentAlignment = Alignment.Center,
                    ) {
                        when (k) {
                            0 -> SvgIcon(Ic.ERASE, Color(0xFFF87171), 22.dp, stroke = 3f)
                            "note" -> SvgIcon(Ic.PENCIL, if (active) Color.White else Color(0xFF64748B), 22.dp)
                            "mode" -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                SvgIcon(Ic.MODE, Color(0xFF64748B), 20.dp, modifier = Modifier.alpha(.5f))
                                T("MODE", 7.sp, 700, Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp).alpha(.6f))
                            }
                            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                T("$k", 22.sp, 700)
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
    Box(Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.widthIn(max = 440.dp).fillMaxWidth()) {
            GlassCard(Modifier.fillMaxWidth().heightIn(max = (LocalConfiguration.current.screenHeightDp * .75f).dp)) {
                Column(Modifier.padding(32.dp)) {
                    T("Your Records", 30.sp, 700, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(24.dp))
                    val rate = if (stats.started > 0) Math.round(stats.won * 100f / stats.started) else 0
                    Row(Modifier.fillMaxWidth().alpha(.6f), horizontalArrangement = Arrangement.SpaceAround) {
                        T("Wins: ${stats.won}", 12.sp, 700); T("Streak: ${stats.streak}", 12.sp, 700); T("Win Rate: $rate%", 12.sp, 700)
                    }
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(scores.sortedBy { it.seconds }.take(30)) { i, s ->
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = .05f)).padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    T("#${i + 1} ${s.seed}", 12.sp, 700, modifier = Modifier.alpha(.3f))
                                    T(s.difficulty.name, 9.sp, 700, modifier = Modifier.alpha(.6f))
                                    T(s.date, 8.sp, 500, modifier = Modifier.alpha(.4f))
                                }
                                T("%d:%02d".format(s.seconds / 60, s.seconds % 60), 14.sp, 700, family = Mono)
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
