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
    const val MODE = "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zM8 12h8M12 8v8"
    const val PENCIL = "M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5L2 22l1.5-5.5L17 3z"
    const val ERASE = "M18 6L6 18M6 6l12 12"
    const val HINT = "M9 18h6M10 22h4M12 2a7 7 0 0 0-4 12.7V17h8v-2.3A7 7 0 0 0 12 2z"
    const val SUN = "M12 8a4 4 0 1 0 0 8a4 4 0 1 0 0-8M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"
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
