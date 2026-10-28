package com.mrstu82.zensudoku

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import androidx.compose.ui.graphics.drawscope.rotate
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
fun ZenBackground(season: Int = 0) {
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
        // Season: a few drifting petals or leaves in the accent colour.
        if (season > 0) repeat(14) {
            val p = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height); val l = (5 + rnd.nextFloat() * 5) * density
            rotate(rnd.nextFloat() * 360f, p) { drawOval(z.accent.copy(alpha = .16f), p - Offset(l, l / 2), Size(2 * l, l)) }
        }
    }
}

/** Brush ensō: one open stroke that thins and dries out towards its tail; [progress] draws it in. */
@Composable
fun Enso(modifier: Modifier, progress: Float = 1f) {
    val ink = LocalZen.current.textPrimary
    Canvas(modifier) {
        val n = 140; val start = 115f; val sweep = 318f; val maxW = size.minDimension * .085f
        val inset = maxW; val box = Size(size.width - 2 * inset, size.height - 2 * inset)
        for (i in 0 until (n * progress).toInt()) {
            val t = i / n.toFloat()
            val w = maxW * (1f - .7f * t) * (.92f + .08f * kotlin.math.sin(t * 23f))
            drawArc(
                ink.copy(alpha = .85f * (1f - .45f * t * t)), start + sweep * t, sweep / n + .8f, false,
                Offset(inset, inset), box, style = Stroke(w, cap = StrokeCap.Round),
            )
        }
    }
}

/** Track name and speaker cycle the five elements (then silence); play/pause stops the music. */
@Composable
fun PlayerBar(app: ZenApp) {
    val z = LocalZen.current; val t = Tracks.getOrNull(app.settings.track)
    val playing = t != null && app.musicOn && app.settings.music > 0
    // Unobtrusive: no surface, no divider, sits below the status bar at 45% ink.
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp).height(32.dp).padding(horizontal = 24.dp).alpha(.45f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.tap { app.nextTrack() }, verticalAlignment = Alignment.CenterVertically) {
            T(t?.let { "${it.name} ${it.kanji}" } ?: "Silence 無", 12.sp, 500, spacing = .1.em)
            if (playing) T("  ·  playing", 10.sp, 400, z.textSecondary)
        }
        Spacer(Modifier.weight(1f))
        SvgIcon(Ic.SPEAKER, z.textPrimary, 16.dp, stroke = 1.5f, modifier = Modifier.tap { app.nextTrack() }.padding(6.dp))
        SvgIcon(if (playing) Ic.PAUSE else Ic.PLAY, z.textPrimary, 16.dp, stroke = 1.5f, modifier = Modifier.tap { app.toggleMusic() }.padding(6.dp))
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


/** Scrolling glass card with a brush title, used by Options, How to Play and Records. */
@Composable
fun Page(kanji: String, title: String, back: () -> Unit, scroll: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    val z = LocalZen.current
    Box(Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp), contentAlignment = Alignment.Center) {
        GlassCard(Modifier.widthIn(max = 440.dp).fillMaxWidth().heightIn(max = (LocalConfiguration.current.screenHeightDp * .9f).dp)) {
            Column(Modifier.padding(horizontal = 28.dp, vertical = 28.dp)) {
                T(kanji, 34.sp, family = Brush, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                T(title, 10.sp, 500, z.textSecondary, spacing = .4.em, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(20.dp))
                Column(Modifier.weight(1f, fill = false).then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier), content = content)
                Spacer(Modifier.height(20.dp))
                ReturnButton(back)
            }
        }
    }
}

// ---------- menu ----------

@Composable
fun MenuScreen(app: ZenApp) {
    var recall by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(Modifier.widthIn(max = 384.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
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
                val level = app.settings.level
                DifficultyPicker(level) { app.update(app.settings.copy(level = it)) }
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    app.resumable?.let { g -> ZenButton("Resume · ${g.puzzle.difficulty.kanji} ${clock(g.seconds)}", Ic.PLAY) { app.screen = Screen.Game } }
                    ZenButton("Begin Journey", Ic.ARROW) { app.start(generate(randomSeed(), level)) }
                    val streak = app.streak.takeIf { it > 0 }?.let { " · $it 日" } ?: ""
                    ZenButton("Daily Seed 日$streak", Ic.SUN) { app.start(generate(dailySeed(), Difficulty.MEDIUM), daily = true) }
                    ZenButton("Recall Seed", Ic.ARROW) { recall = true }
                    ZenButton("How to Play", Ic.RULES) { app.open(Screen.HowTo) }
                    ZenButton("Records", Ic.STAR) { app.screen = Screen.Records }
                    ZenButton("Options", Ic.OPTIONS) { app.open(Screen.Options) }
                }
            }
        }
        if (recall) RecallSeed(app) { recall = false }
    }
}

/** Type a friend's 8-character seed to play the same board at the chosen level. */
@Composable
private fun RecallSeed(app: ZenApp, close: () -> Unit) {
    val z = LocalZen.current; var seed by remember { mutableStateOf("") }
    BackHandler(onBack = close)
    Veil("呼", "RECALL SEED") {
        val shape = RoundedCornerShape(4.dp)
        BasicTextField(
            seed, { t -> seed = t.uppercase().filter { it in SEED_CHARS }.take(8) },
            Modifier.fillMaxWidth().clip(shape).background(z.glass, shape).border(1.dp, z.gridLine, shape).padding(16.dp),
            textStyle = TextStyle(z.textPrimary, 22.sp, FontWeight.W700, fontFamily = Mono, letterSpacing = .2.em, textAlign = TextAlign.Center),
            singleLine = true, cursorBrush = SolidColor(z.accent),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        )
        T("Level: ${app.settings.level.label}", 12.sp, 500, z.textSecondary, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Box(Modifier.alpha(if (seed.length == 8) 1f else .35f)) {
            ZenButton("Begin", Ic.ARROW) { if (seed.length == 8) { close(); app.start(generate(seed, app.settings.level)) } }
        }
        ReturnButton(close)
    }
}

/** Menu difficulty: labelled row of tiles; the chosen one is filled indigo with a vermilion mark. */
@Composable
fun DifficultyPicker(selected: Difficulty, pick: (Difficulty) -> Unit) {
    val z = LocalZen.current; val shape = RoundedCornerShape(4.dp)
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
                    T(d.plain, 13.sp, 700, ink, modifier = Modifier.padding(top = 2.dp))
                    Box(Modifier.padding(top = 6.dp).size(5.dp).background(if (on) Shu else Color.Transparent, RoundedCornerShape(50)))
                }
            }
        }
    }
}

/** Segmented pill row. */
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
            ) { T(label(k), 12.sp, 500, if (on) z.boardBg else z.textSecondary, align = TextAlign.Center) }
        }
    }
}

// ---------- options ----------

@Composable
private fun Section(title: String) =
    T(title, 10.sp, 600, LocalZen.current.textSecondary, spacing = .3.em, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))

/** A quiet on/off row: a small ink dot fills when on. */
@Composable
private fun Toggle(label: String, note: String, on: Boolean, set: (Boolean) -> Unit) {
    val z = LocalZen.current
    Row(Modifier.fillMaxWidth().tap { set(!on) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            T(label, 15.sp, 500)
            T(note, 11.sp, 400, z.textSecondary)
        }
        Box(
            Modifier.size(22.dp).border(1.5.dp, if (on) z.accent else z.gridLine, RoundedCornerShape(50)).padding(5.dp)
                .background(if (on) z.accent else Color.Transparent, RoundedCornerShape(50)),
        )
    }
}

@Composable
fun OptionsScreen(app: ZenApp) {
    val s = app.settings; fun set(n: Settings) = app.update(n)
    val vol = listOf("Off", "Low", "Mid", "High")
    Page("設定", "OPTIONS", { app.screen = app.back }) {
        Section("THEME")
        Chips(Themes.keys.toList(), s.theme, { Themes.getValue(it).label }) { set(s.copy(theme = it)) }
        Section("SEASON")
        Chips(listOf(0, 1, 2), s.season, { listOf("None", "Sakura 桜", "Momiji 紅葉")[it] }) { set(s.copy(season = it)) }
        Section("TRACK")
        Chips((0 until Tracks.size).toList() + -1, s.track, { Tracks.getOrNull(it)?.kanji ?: "無" }) { set(s.copy(track = it)) }
        Section("MUSIC")
        Chips((0..3).toList(), s.music, { vol[it] }) { set(s.copy(music = it)) }
        Section("SOUND EFFECTS")
        Chips((0..3).toList(), s.sfx, { vol[it] }) { set(s.copy(sfx = it)) }
        Section("MISTAKE LIMIT")
        Chips(listOf(3, 5, 0), s.mistakeLimit, { if (it == 0) "None" else "$it" }) { set(s.copy(mistakeLimit = it)) }
        Section("GUIDE")
        Toggle("Possibility dots", "Pips show how many digits still fit", s.dots) { set(s.copy(dots = it)) }
        Toggle("Clarity border", "Outline cells with a single answer", s.clarity) { set(s.copy(clarity = it)) }
        Toggle("Bloom", "Button that fills every single", s.bloom) { set(s.copy(bloom = it)) }
        Toggle("Auto-tidy notes", "A placed digit clears it from nearby notes", s.tidy) { set(s.copy(tidy = it)) }
        Toggle("Calm mode", "Hide timer and mistakes; scores saved untimed", s.calm) { set(s.copy(calm = it)) }
        Section("HANDS")
        Toggle("Soft haptics", "A light tick on placement", s.haptics) { set(s.copy(haptics = it)) }
        Toggle("Larger digits", "Bigger numbers on board and pad", s.large) { set(s.copy(large = it)) }
        Toggle("Left-handed pad", "Mode, notes and erase on the left", s.left) { set(s.copy(left = it)) }
    }
}

// ---------- how to play ----------

/** A 3x3 slice of board: digits ('.' empty), with optional highlighted cells and a matcha-outlined cell. */
@Composable
private fun Mini(cells: String, lit: Set<Int> = emptySet(), clear: Int = -1, notes: Map<Int, String> = emptyMap()) {
    val z = LocalZen.current; val shape = RoundedCornerShape(3.dp)
    Column(Modifier.size(96.dp).background(z.boardBg, shape).border(1.5.dp, z.gridMajor, shape).padding(1.5.dp)) {
        for (r in 0 until 3) Row(Modifier.weight(1f)) {
            for (c in 0 until 3) {
                val i = r * 3 + c; val ch = cells[i]
                Box(
                    Modifier.weight(1f).fillMaxHeight().background(if (i in lit) z.selectionBg else Color.Transparent).border(.5.dp, z.gridLine)
                        .then(if (i == clear) Modifier.padding(2.dp).border(1.dp, Matcha.copy(alpha = .7f), RoundedCornerShape(3.dp)) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        ch != '.' -> T("$ch", 15.sp, 700)
                        i in notes -> T(notes.getValue(i), 8.sp, 500, z.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun Lesson(title: String, body: String, art: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        art()
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            T(title, 15.sp, 700)
            T(body, 12.sp, 400, LocalZen.current.textSecondary)
        }
    }
}

@Composable
fun HowToScreen(back: () -> Unit) {
    Page("道", "HOW TO PLAY", back) {
        Lesson("One of each", "Fill every row, column and 3×3 box with 1 to 9, each digit once.") { Mini("53.6.98.7", lit = setOf(2, 4)) }
        Lesson("Choose a cell, then a digit", "Or switch MODE to pick a digit first and tap cells to place it.") { Mini("1.3.5.789", lit = setOf(1)) }
        Lesson("Notes", "The pencil key writes small notes in a cell; the top pencil fills every empty cell with its candidates.") { Mini("12..5.7.9", notes = mapOf(2 to "3 6", 3 to "4 6", 5 to "3 8")) }
        Lesson("Clarity", "A soft green border marks a cell with only one possible digit; Bloom fills them all.") { Mini("12345.789", clear = 5) }
        Lesson("Gentle help", "The lantern reveals one cell. Mistakes blush and fade; three ends the game unless you change the limit.") {
            Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) { SvgIcon(Ic.HINT, LocalZen.current.textPrimary, 48.dp, stroke = .75f) }
        }
        Lesson("Seeds", "Every board has an 8-character seed. Share it, or recall one, to play the same puzzle.") {
            Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) { T("K7Q2\nM9XA", 16.sp, 700, family = Mono, align = TextAlign.Center) }
        }
    }
}

// ---------- records ----------

@Composable
fun RecordsScreen(app: ZenApp) {
    val z = LocalZen.current; val stats = app.stats
    var level by remember { mutableStateOf<Difficulty?>(null) }
    val fmt = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.UK) }
    Page("記録", "RECORDS", { app.screen = Screen.Menu }, scroll = false) {
        Row(Modifier.fillMaxWidth().alpha(.6f), horizontalArrangement = Arrangement.SpaceAround) {
            T("Played: ${stats.started}", 12.sp, 700); T("Won: ${stats.won}", 12.sp, 700)
            T("Streak: ${app.streak}", 12.sp, 700); T("Longest: ${stats.longest}", 12.sp, 700)
        }
        Spacer(Modifier.height(16.dp))
        // Best (clean runs only) and average (all timed runs) per level.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Difficulty.entries.forEach { d ->
                val timed = app.scores.filter { it.difficulty == d && !it.untimed }
                val best = timed.filter { it.clean }.minOfOrNull { it.seconds }
                val avg = timed.takeIf { it.isNotEmpty() }?.map { it.seconds }?.average()?.toInt()
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(3.dp)).background(z.textPrimary.copy(alpha = if (d == level) .1f else .045f)).padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    T(d.plain.uppercase(), 8.sp, 700, spacing = .1.em, modifier = Modifier.alpha(.5f))
                    T(best?.let { clock(it) } ?: "–", 13.sp, 600)
                    T("avg ${avg?.let { clock(it) } ?: "–"}", 9.sp, 500, modifier = Modifier.alpha(.5f))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Chips(listOf(null) + Difficulty.entries, level, { it?.kanji ?: "All" }) { level = it }
        Spacer(Modifier.height(12.dp))
        val list = app.scores.filter { level == null || it.difficulty == level }.sortedWith(compareBy({ it.untimed }, { it.seconds })).take(50)
        if (list.isEmpty()) T("No journeys finished yet.", 13.sp, 400, z.textSecondary, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(16.dp))
        LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(list) { i, s ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(3.dp)).background(z.textPrimary.copy(alpha = .045f)).padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        T("#${i + 1} ${s.seed}", 12.sp, 700, modifier = Modifier.alpha(.3f))
                        val tags = listOfNotNull(s.difficulty.plain.uppercase(), "日 DAILY".takeIf { s.daily }, "HINTED".takeIf { s.hinted }, "UNTIMED".takeIf { s.untimed })
                        T(tags.joinToString(" · "), 9.sp, 700, modifier = Modifier.alpha(.6f))
                        T(fmt.format(java.util.Date(s.at)), 8.sp, 500, modifier = Modifier.alpha(.4f))
                    }
                    T(clock(s.seconds), 14.sp, 600, modifier = Modifier.alpha(if (s.clean) 1f else .45f))
                }
            }
        }
    }
}
