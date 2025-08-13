@file:Suppress("FunctionName")

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.test.Test

class IsoDemoFile() {
    @Test
    fun main() {
        application {
            Window(onCloseRequest = ::exitApplication, title = "Iso Demo") {
                MaterialTheme { IsoDemo() }
            }
        }
    }
}


// --- Iso math ---
data class Iso(val x: Float, val y: Float)

fun gridToIso(ix: Int, iy: Int, tileW: Float, tileH: Float): Iso {
    val hw = tileW / 2f
    val hh = tileH / 2f
    val sx = (ix - iy) * hw
    val sy = (ix + iy) * hh
    return Iso(sx, sy)
}

fun isoToGrid(px: Float, py: Float, tileW: Float, tileH: Float): Pair<Float, Float> {
    val hw = tileW / 2f
    val hh = tileH / 2f
    val gx = (px / hw + py / hh) / 2f
    val gy = (py / hh - px / hw) / 2f
    return gx to gy
}

// Diamond hit-test (within one tile centered at origin)
private fun inDiamond(local: Offset, tileW: Float, tileH: Float): Boolean {
    val hw = tileW / 2f
    val hh = tileH / 2f
    // |x|/hw + |y|/hh <= 1
    return (abs(local.x) / hw + abs(local.y) / hh) <= 1f
}

@Composable
fun IsoDemo(
    cols: Int = 12,
    rows: Int = 12,
    tileW: Float = 128f,
    tileH: Float = 64f
) {
    // camera state
    var origin by remember { mutableStateOf(Offset(600f, 100f)) }
    var scale by remember { mutableStateOf(1.0f) }

    // selection + a single actor
    var selected by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val actor = remember { mutableStateOf(Offset(5f, 5f)) } // grid coords (x,y)

    val groundA = Color(0xFF3E7F4C)
    val groundB = Color(0xFF2F6B3D)
    val gridLine = Color(0xFF1D3B28)
    val selectColor = Color(0xFFFFD54F)
    val actorColor = Color(0xFF2196F3)

    val diamond = remember(tileW, tileH) {
        Path().apply {
            moveTo(0f, -tileH / 2f)
            lineTo(tileW / 2f, 0f)
            lineTo(0f, tileH / 2f)
            lineTo(-tileW / 2f, 0f)
            close()
        }
    }

    // Gesture: pan + zoom
    val transformMod = Modifier.pointerInput(Unit) {
        detectTransformGestures(
            onGesture = { centroid, pan, zoom, _ ->
                // Zoom around the gesture centroid in screen space
                val newScale = (scale * zoom).coerceIn(0.5f, 3.0f)
                val scaleChange = newScale / scale
                // Recompute origin so the centroid stays under the fingers
                origin = (origin - centroid) * scaleChange + centroid + pan
                scale = newScale
            }
        )
    }

    // Gesture: tap to select tile; double-tap moves actor
    val tapMod = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onTap = { pos ->
                // Screen -> world
                val world = (pos - origin) / scale
                val (gx, gy) = isoToGrid(world.x, world.y, tileW, tileH)
                val cx = floor(gx).toInt()
                val cy = floor(gy).toInt()

                // refine hit inside the diamond
                val center = gridToIso(cx, cy, tileW, tileH)
                val local = Offset(world.x - center.x, world.y - center.y)
                if (inDiamond(local, tileW, tileH) && cx in 0 until cols && cy in 0 until rows) {
                    selected = cx to cy
                } else {
                    selected = null
                }
            },
            onDoubleTap = { pos ->
                val world = (pos - origin) / scale
                val (gx, gy) = isoToGrid(world.x, world.y, tileW, tileH)
                val cx = floor(gx).toInt()
                val cy = floor(gy).toInt()
                val center = gridToIso(cx, cy, tileW, tileH)
                val local = Offset(world.x - center.x, world.y - center.y)
                if (inDiamond(local, tileW, tileH) && cx in 0 until cols && cy in 0 until rows) {
                    actor.value = Offset(cx.toFloat(), cy.toFloat())
                    selected = cx to cy
                }
            }
        )
    }

    Canvas(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .then(transformMod)
            .then(tapMod)
    ) {
        withTransform({
            translate(origin.x, origin.y)
            scale(scale, scale)
        }) {
            // Draw ground (back-to-front by x+y so actors occlude correctly)
            for (y in 0 until rows) {
                for (x in 0 until cols) {
                    val p = gridToIso(x, y, tileW, tileH)
                    withTransform({
                        translate(p.x, p.y)
                    }) {
                        val col = if ((x + y) % 2 == 0) groundA else groundB
                        drawPath(diamond, color = col)
                        // optional grid line
                        drawPath(diamond, color = gridLine.copy(alpha = 0.35f))
                    }
                }
            }

            // Highlight selection
            selected?.let { (sx, sy) ->
                val p = gridToIso(sx, sy, tileW, tileH)
                withTransform({
                    translate(p.x, p.y)
                }) {
                    drawPath(diamond, color = selectColor.copy(alpha = 0.35f))
                }
            }

            // Draw a simple "actor" (placed bottom-center on tile)
            run {
                val ax = actor.value.x
                val ay = actor.value.y
                val p = gridToIso(ax.toInt(), ay.toInt(), tileW, tileH)
                // circle with its bottom touching the tile center
                val r = min(tileW, tileH) * 0.35f
                val center = Offset(p.x, p.y - r)
                drawCircle(color = actorColor, radius = r, center = center)
                // tiny shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.25f),
                    radius = r * 0.5f,
                    center = Offset(p.x, p.y + 2f)
                )
            }
        }
    }
}
