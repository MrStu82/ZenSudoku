package com.mrstu82.zensudoku

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
) {
    /** Row, column and box of the selected cell: a light wash of the theme accent. */
    val peerBg get() = accent.copy(alpha = .22f)
}

val Themes = mapOf(
    // Washi paper, sumi charcoal, ai (indigo) accent.
    "classic" to ZenColors(
        "Washi", Color(0xFFF1EBDD), Color(0xFF2F2E2C), Color(0xFF6F685E), Color(0xFF2E4A66),
        Color(0xFFFAF6EC), Color(0x332F2E2C), Color(0xFF2F2E2C), Color(0xE6FAF6EC),
        Color(0x80748A4A), Color(0x122F2E2C), Color(0x732E4A66), Color(0x57748A4A),
    ),
    // Yoru: night indigo, moonlit paper ink.
    "dark" to ZenColors(
        "Yoru", Color(0xFF1A2030), Color(0xFFECE6D6), Color(0xFFA39C8C), Color(0xFF8FA6C8),
        Color(0xFF222A3B), Color(0x33ECE6D6), Color(0xCCECE6D6), Color(0xE6222A3B),
        Color(0x80A9BC7A), Color(0x14ECE6D6), Color(0x738FA6C8), Color(0x57A9BC7A),
    ),
    // Sabi: aged paper, rust-brown kaki accent.
    "sepia" to ZenColors(
        "Sabi", Color(0xFFEFE6D2), Color(0xFF4A3F33), Color(0xFF7D705F), Color(0xFF7A5A2E),
        Color(0xFFF7F0E0), Color(0x334A3F33), Color(0xFF4A3F33), Color(0xE6F7F0E0),
        Color(0x80748A4A), Color(0x124A3F33), Color(0x737A5A2E), Color(0x57748A4A),
    ),
    // Koke: moss garden after rain.
    "forest" to ZenColors(
        "Koke", Color(0xFF2A3326), Color(0xFFE9E6D3), Color(0xFFA8AB95), Color(0xFFA9BC7A),
        Color(0xFF323D2D), Color(0x33E9E6D3), Color(0xCCE9E6D3), Color(0xE6323D2D),
        Color(0x80A9BC7A), Color(0x14E9E6D3), Color(0x73A9BC7A), Color(0x57A9BC7A),
    ),
)

/** Shu-iro vermilion (hanko seal, mistakes) and matcha (gentle guidance). */
val Shu = Color(0xFFB8432F)
val Matcha = Color(0xFF748A4A)

val ZenColors.dark get() = bgBase.luminance() < .5f

/** Theme with the optional seasonal accent (1 sakura, 2 momiji) laid over it. */
fun zen(s: Settings): ZenColors {
    val z = Themes.getValue(s.theme)
    val a = when (s.season) {
        1 -> if (z.dark) Color(0xFFE3A3B4) else Color(0xFFB8607A)
        2 -> if (z.dark) Color(0xFFE08A5A) else Color(0xFFB0502A)
        else -> return z
    }
    return z.copy(accent = a, selectionBg = a.copy(alpha = .45f))
}

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
