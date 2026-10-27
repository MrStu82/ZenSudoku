package com.mrstu82.zensudoku

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
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
    // Washi paper, sumi charcoal, ai (indigo) accent.
    "classic" to ZenColors(
        "Washi", Color(0xFFF1EBDD), Color(0xFF2F2E2C), Color(0xFF6F685E), Color(0xFF2E4A66),
        Color(0xFFFAF6EC), Color(0x332F2E2C), Color(0xFF2F2E2C), Color(0xE6FAF6EC),
        Color(0x80748A4A), Color(0x122F2E2C), Color(0x332E4A66), Color(0x1F2E4A66),
    ),
    "dark" to ZenColors(
        "Yoru", Color(0xFF121820), Color(0xFFF5F6FA), Color(0xFFA4B0BE), Color(0xFF45AAF2),
        Color(0xFF1E272E), Color.White.copy(alpha = .15f), Color(0xFFF5F6FA), Color(0xFA1E272E),
        Color(0x8045AAF2), Color.White.copy(alpha = .08f), Color(0x4D45AAF2), Color(0x2645AAF2),
    ),
    "sepia" to ZenColors(
        "Sabi", Color(0xFFFDF6E3), Color(0xFF586E75), Color(0xFF839496), Color(0xFFB58900),
        Color(0xFFEEE8D5), Color(0x4D586E75), Color(0xFF586E75), Color(0xF2FDF6E3),
        Color(0x80B58900), Color(0x1AB58900), Color(0x33B58900), Color(0x1AB58900),
    ),
    "forest" to ZenColors(
        "Koke", Color(0xFF064E3B), Color(0xFFECFDF5), Color(0xFFA7F3D0), Color(0xFF34D399),
        Color(0xFF065F46), Color(0x40ECFDF5), Color(0xFFECFDF5), Color(0xF2064E3B),
        Color(0x8034D399), Color(0x26ECFDF5), Color(0x4D34D399), Color(0x2634D399),
    ),
)

/** Shu-iro vermilion (hanko seal, mistakes) and matcha (gentle guidance). */
val Shu = Color(0xFFB8432F)
val Matcha = Color(0xFF748A4A)

val LocalZen = staticCompositionLocalOf { Themes.getValue("classic") }

@OptIn(ExperimentalTextApi::class)
private fun weights(res: Int, vararg w: Int) =
    FontFamily(w.map { Font(res, FontWeight(it), variationSettings = FontVariation.Settings(FontVariation.weight(it))) })

/** Shippori Mincho (subset): one step lighter than asked, for a calmer page. */
val Mincho = FontFamily(
    Font(R.font.mincho_regular, FontWeight.W400), Font(R.font.mincho_regular, FontWeight.W500),
    Font(R.font.mincho_semibold, FontWeight.W600), Font(R.font.mincho_semibold, FontWeight.W700),
    Font(R.font.mincho_bold, FontWeight.W800),
)

/** Yuji Syuku brush face (subset), for titles only. */
val Brush = FontFamily(Font(R.font.yuji_syuku))
val Mono = weights(R.font.jetbrains_mono, 500, 700)
