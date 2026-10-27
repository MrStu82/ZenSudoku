package com.mrstu82.zensudoku

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The web version's inline 24x24 stroke SVGs (circles/rects/polygons rewritten as paths). */
object Ic {
    const val ARROW = "M5 12h14M12 5l7 7-7 7"
    const val RULES = "M2 12a10 10 0 1 0 20 0a10 10 0 1 0-20 0M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"
    const val STAR = "M12 15l-2 5l-2-5l-5-2l5-2l2-5l2 5l5 2z"
    const val OPTIONS = "M12 12m-3 0a3 3 0 1 0 6 0a3 3 0 1 0-6 0"
    const val SPEAKER = "M11 5L6 9H2v6h4l5 4V5zM19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"
    const val PLAY = "M5 3L19 12L5 21Z"
    const val PAUSE = "M6 4h4v16H6zM14 4h4v16h-4z"
    const val UNDO = "M3 12a9 9 0 1 0 9-9a9.75 9.75 0 0 0-6.74 2.74L3 8M3 3v5h5"
    const val MODE = "M7 4L3 8l4 4M3 8h14M17 12l4 4-4 4M21 16H7"
    const val PENCIL = "M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5L2 22l1.5-5.5L17 3z"
    const val ERASE = "M18 6L6 18M6 6l12 12"
    const val BLOOM = "M12 3c2 3 2 6 0 9c-2-3-2-6 0-9zM3 12c3-2 6-2 9 0c-3 2-6 2-9 0zM21 12c-3-2-6-2-9 0c3 2 6 2 9 0zM12 21c2-3 2-6 0-9c-2 3-2 6 0 9z"
    const val SUN = "M12 8a4 4 0 1 0 0 8a4 4 0 1 0 0-8M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"
    const val HINT = "M12 5c4 0 6 3 6 7s-2 7-6 7s-6-3-6-7s2-7 6-7zM6 12h12M7 8.5h10M7 15.5h10M9 3h6M9 21h6"
    const val COPY = "M10 0h10a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H10a2 2 0 0 1-2-2V2a2 2 0 0 1 2-2zM4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"
}

@Composable
fun SvgIcon(d: String, color: Color, size: Dp, stroke: Float = 2.5f, modifier: Modifier = Modifier) {
    val v = remember(d, color, stroke) {
        ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(
                pathData = PathParser().parsePathString(d).toNodes(), stroke = SolidColor(color), strokeLineWidth = stroke,
                strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            ).build()
    }
    Image(rememberVectorPainter(v), contentDescription = null, modifier = modifier.size(size))
}
