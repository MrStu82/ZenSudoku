package com.mrstu82.zensudoku

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

/** The web version's CSS custom properties, one set per theme. */
data class ZenColors(
    val label: String,
    val bgBase: Color, val textPrimary: Color, val textSecondary: Color, val accent: Color,
    val boardBg: Color, val gridLine: Color, val gridMajor: Color, val glass: Color,
    val hintGlow: Color, val prefilledBg: Color, val selectionBg: Color, val sameNumBg: Color,
)

val Themes = mapOf(
    "classic" to ZenColors(
        "Daylight", Color(0xFFF4F1EA), Color(0xFF2D3436), Color(0xFF636E72), Color(0xFF0984E3),
        Color.White, Color.Black.copy(alpha = .25f), Color(0xFF2D3436), Color.White.copy(alpha = .95f),
        Color(0x660984E3), Color.Black.copy(alpha = .06f), Color(0x400984E3), Color(0x260984E3),
    ),
    "dark" to ZenColors(
        "Midnight", Color(0xFF121820), Color(0xFFF5F6FA), Color(0xFFA4B0BE), Color(0xFF45AAF2),
        Color(0xFF1E272E), Color.White.copy(alpha = .15f), Color(0xFFF5F6FA), Color(0xFA1E272E),
        Color(0x8045AAF2), Color.White.copy(alpha = .08f), Color(0x4D45AAF2), Color(0x2645AAF2),
    ),
    "sepia" to ZenColors(
        "Ancient", Color(0xFFFDF6E3), Color(0xFF586E75), Color(0xFF839496), Color(0xFFB58900),
        Color(0xFFEEE8D5), Color(0x4D586E75), Color(0xFF586E75), Color(0xF2FDF6E3),
        Color(0x80B58900), Color(0x1AB58900), Color(0x33B58900), Color(0x1AB58900),
    ),
    "forest" to ZenColors(
        "Forest", Color(0xFF064E3B), Color(0xFFECFDF5), Color(0xFFA7F3D0), Color(0xFF34D399),
        Color(0xFF065F46), Color(0x40ECFDF5), Color(0xFFECFDF5), Color(0xF2064E3B),
        Color(0x8034D399), Color(0x26ECFDF5), Color(0x4D34D399), Color(0x2634D399),
    ),
)

val LocalZen = staticCompositionLocalOf { Themes.getValue("classic") }

private fun weights(res: Int, vararg w: Int) =
    FontFamily(w.map { Font(res, FontWeight(it), variationSettings = FontVariation.Settings(FontVariation.weight(it))) })

val Quicksand = weights(R.font.quicksand, 400, 500, 600, 700)
val Mono = weights(R.font.jetbrains_mono, 500, 700)
