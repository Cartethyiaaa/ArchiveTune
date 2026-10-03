/*
 * ArchiveTune (2026)
 * Liquid-glass surface kit built on Haze (already a project dependency).
 *
 * Usage:
 *   1. Create one HazeState near the root:  val glassHazeState = rememberHazeState()
 *   2. Mark the content that should show through the glass:  Modifier.hazeSource(glassHazeState)
 *   3. Put the glass above it:  Modifier.glassPanel(glassHazeState, shape, tint)
 * Without a HazeState the panel falls back to a denser translucent fill (still readable).
 */

package moe.rukamori.archivetune.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur

/** Glass material: blur of whatever sits behind + tinted fill + specular highlight + rim light. */
@Composable
fun Modifier.glassPanel(
    hazeState: HazeState?,
    shape: Shape,
    tint: Color,
    blurRadius: Dp = 26.dp,
    tintAlpha: Float = 0.42f,
): Modifier {
    val blurStyle =
        remember(blurRadius) {
            HazeBlurStyle {
                blurEnabled(true)
                blurRadius(blurRadius)
                noiseFactor(0.03f)
                backgroundColor(Color.Transparent)
            }
        }
    // Without a blur source the glass has nothing to show, so make the fill denser.
    val baseAlpha = if (hazeState != null) tintAlpha else (tintAlpha + 0.35f).coerceAtMost(0.92f)
    val fill =
        remember(tint, baseAlpha) {
            Brush.linearGradient(
                listOf(
                    tint.copy(alpha = (baseAlpha + 0.10f).coerceAtMost(1f)),
                    tint.copy(alpha = (baseAlpha - 0.10f).coerceAtLeast(0f)),
                ),
            )
        }
    val rim =
        remember {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.55f),
                    Color.White.copy(alpha = 0.04f),
                    Color.White.copy(alpha = 0.26f),
                ),
            )
        }

    val blurred =
        if (hazeState != null) {
            Modifier.hazeBlur(
                input = HazeInput.Sources(hazeState),
                style = blurStyle,
                performanceMode = HazePerformanceMode.Adaptive,
                expandLayerBounds = false,
            )
        } else {
            Modifier
        }

    return this
        .clip(shape)
        .then(blurred)
        .background(fill)
        .drawBehind {
            // specular highlight, top-left
            drawRect(
                Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.75f,
                ),
            )
        }.border(1.dp, rim, shape)
}
