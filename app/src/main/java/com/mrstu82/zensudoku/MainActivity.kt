package com.mrstu82.zensudoku

import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import java.time.LocalDate

enum class Screen { Menu, Game, Records, Options, HowTo }

/** App-wide state: settings, saved records and the game in progress, all backed by [Repo]. */
class ZenApp(private val repo: Repo, val audio: Audio) {
    private val saved = repo.load()
    var settings by mutableStateOf(saved.settings); private set
    var scores by mutableStateOf(saved.scores); private set
    var stats by mutableStateOf(saved.stats); private set
    var game by mutableStateOf(saved.resume?.let { GameState.decode(it) }); private set
    var screen by mutableStateOf(Screen.Menu)
    /** Where Options and How to Play return to. */
    var back = Screen.Menu
    /** Player-bar pause; per session only. */
    var musicOn by mutableStateOf(true); private set
    private var recorded: GameState? = null

    fun today() = LocalDate.now().toEpochDay()

    /** The daily streak only counts while unbroken (last daily solved today or yesterday). */
    val streak get() = if (stats.lastDaily >= today() - 1) stats.streak else 0

    val resumable get() = game?.takeIf { !it.solved && !it.lost }

    fun update(s: Settings) { settings = s; repo.saveSettings(s); audio.apply(s, musicOn) }
    fun toggleMusic() { musicOn = !musicOn; audio.apply(settings, musicOn) }
    fun nextTrack() = update(settings.copy(track = if (settings.track + 1 >= Tracks.size) -1 else settings.track + 1))

    fun open(s: Screen) { if (s == Screen.Options || s == Screen.HowTo) back = screen; screen = s }

    fun start(p: Puzzle, daily: Boolean = false) {
        game = GameState(p, daily); repo.started(); repo.saveResume(null); refresh(); screen = Screen.Game
    }

    fun quit() { game = null; repo.saveResume(null); screen = Screen.Menu }

    fun win(g: GameState) {
        if (recorded === g) return
        recorded = g
        repo.won(Score(g.puzzle.seed, g.puzzle.difficulty, g.seconds, System.currentTimeMillis(), g.hints > 0, settings.calm, g.daily), today())
        repo.saveResume(null); refresh()
    }

    /** Fastest clean time for this exact seed and level. */
    fun best(p: Puzzle) = scores.filter { it.seed == p.seed && it.difficulty == p.difficulty && it.clean }.minOfOrNull { it.seconds }

    fun stop() {
        game?.let { it.paused = true }
        repo.saveResume(resumable?.encode()); audio.release()
    }

    private fun refresh() = repo.load().let { scores = it.scores; stats = it.stats }
}

class MainActivity : ComponentActivity() {
    private lateinit var app: ZenApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Settings load synchronously, so the saved theme paints the very first frame.
        app = ZenApp(Repo(this), Audio(this))
        paint(zen(app.settings))
        setContent {
            val z = zen(app.settings)
            LaunchedEffect(z) { paint(z) }
            BackHandler(app.screen != Screen.Menu) {
                when (app.screen) {
                    Screen.Game -> app.game?.let { if (it.paused || it.solved || it.lost) app.screen = Screen.Menu else it.paused = true }
                    Screen.Options, Screen.HowTo -> app.screen = app.back
                    else -> app.screen = Screen.Menu
                }
            }
            CompositionLocalProvider(LocalZen provides z) {
                Box(Modifier.fillMaxSize().background(z.bgBase)) {
                    ZenBackground(app.settings.season)
                    Column(Modifier.fillMaxSize()) {
                        PlayerBar(app)
                        when (app.screen) {
                            Screen.Menu -> MenuScreen(app)
                            Screen.Game -> app.game?.let { GameScreen(app, it) } ?: MenuScreen(app)
                            Screen.Records -> RecordsScreen(app)
                            Screen.Options -> OptionsScreen(app)
                            Screen.HowTo -> HowToScreen { app.screen = app.back }
                        }
                    }
                }
            }
        }
    }

    /** Window and system bars in the theme's paper colour. */
    private fun paint(z: ZenColors) {
        window.decorView.setBackgroundColor(z.bgBase.toArgb())
        val bars = if (z.dark) SystemBarStyle.dark(AColor.TRANSPARENT) else SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT)
        enableEdgeToEdge(bars, bars)
    }

    override fun onStart() { super.onStart(); app.audio.apply(app.settings, app.musicOn) }

    override fun onStop() { app.stop(); super.onStop() }
}
