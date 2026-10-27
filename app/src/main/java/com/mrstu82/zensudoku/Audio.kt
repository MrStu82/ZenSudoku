package com.mrstu82.zensudoku

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** The godai elements; each maps to a bundled track in /audio. */
class Track(val name: String, val kanji: String, val file: String)

val Tracks = listOf(
    Track("Wind", "風", "wind.mp3"), Track("Water", "水", "water.mp3"), Track("Spirit", "魂", "spirit.mp3"),
    Track("Earth", "地", "earth.mp3"), Track("Ether", "空", "ether.mp3"),
)

/** Volume steps shared by music and sound effects. */
val VolumeSteps = listOf(0f, .3f, .6f, 1f)

class Audio(private val ctx: Context) {
    private var player: MediaPlayer? = null
    private var playing = -1
    private var volume = 0f
    private var sfx = 0f

    /** Applies settings; only restarts the player when the track itself changes. */
    fun apply(s: Settings, active: Boolean) {
        volume = VolumeSteps[s.music]; sfx = VolumeSteps[s.sfx]
        val want = if (active && volume > 0f) s.track else -1
        if (want != playing) {
            player?.release(); player = null; playing = want
            Tracks.getOrNull(want)?.let { t ->
                player = MediaPlayer().apply {
                    ctx.assets.openFd(t.file).use { setDataSource(it.fileDescriptor, it.startOffset, it.length) }
                    isLooping = true; prepare(); start()
                }
            }
        }
        player?.setVolume(volume * .6f, volume * .6f)
    }

    fun release() { player?.release(); player = null; playing = -1 }

    private val rate = 44100
    private val bellPcm by lazy { tone(2.6f, listOf(528f to 1f, 1457f to .35f, 2851f to .12f), 1.6f) }
    private val tickPcm by lazy { tone(.06f, listOf(1150f to 1f), 70f) }

    /** Soft temple-bowl bell for a finished puzzle. */
    fun bell() = play(bellPcm, .5f)

    /** A quiet wooden tick for a placed digit. */
    fun tick() = play(tickPcm, .18f)

    private fun play(pcm: ShortArray, gain: Float) {
        if (sfx <= 0f) return
        val t = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(pcm.size * 2).setTransferMode(AudioTrack.MODE_STATIC).build()
        t.write(pcm, 0, pcm.size); t.setVolume(sfx * gain)
        t.setNotificationMarkerPosition(pcm.size)
        t.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack) = track.release()
            override fun onPeriodicNotification(track: AudioTrack) {}
        })
        t.play()
    }

    /** Sum of decaying sine partials with a 5 ms attack, so nothing clicks or buzzes. */
    private fun tone(seconds: Float, partials: List<Pair<Float, Float>>, decay: Float) = ShortArray((seconds * rate).toInt()) { i ->
        val t = i / rate.toFloat()
        val env = minOf(1f, t / .005f) * exp(-decay * t)
        val v = partials.sumOf { (f, a) -> a * sin(2 * PI * f * t) } / partials.sumOf { it.second.toDouble() }
        (v * env * Short.MAX_VALUE * .8).toInt().toShort()
    }
}
