package com.jervisffb.ui.game.view.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.jervisffb.ui.game.animations.ConfettiAnimation
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.DurationUnit

private data class Particle(
    val x0: Float,
    val y0: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val pixelSize: Float,
)

private data class CannonShot(
    val delaySeconds: Float,
    val particles: List<Particle>,
)

@Composable
fun TouchdownAnimation(vm: FieldViewModel, animation: ConfettiAnimation) {

    val squareSize = remember(animation) {
        vm.sharedFieldData.size.squareSize.width
    }

    val shots = remember(animation) {
        val rng = Random.Default
        val scale = squareSize / 30f // Adjust confetti size and its speed
        val angle = 65.0 // Angle of the confetti cannon (in degrees)
        val spreadDegrees = 50.0 // Spread of the cone in degrees
        val centerAngle = when (animation.homeTeamScored) {
            true -> (180.0 - angle) * PI / 180.0
            false -> angle * PI / 180.0
        }
        val halfCone = spreadDegrees * PI / 180.0
        val shotDelaySec = animation.shotDelay.inWholeMilliseconds / 1000f

        (0 until animation.shotCount).map { index ->
            // Trigger animation from bottom-right corner of the given coordinates
            val (cx, cy) = animation.getShotCoords(index, vm)
            val particles = List(animation.particleCount) {
                val colorArgb = animation.colors.random()
                val pixelSize = (4f + rng.nextFloat() * 2f) * scale
                // Initial angle of the particle
                val a = centerAngle + (rng.nextFloat() - 0.5f) * halfCone
                // Speed of confetti particles. Is adjusted with a big enough range that you can see the entire "cone".
                // But also avoid having particles just drop down immediately.
                val speed = 200f + (rng.nextFloat() * 400f) * scale
                Particle(
                    x0 = cx,
                    y0 = cy,
                    vx = (cos(a) * speed).toFloat(),
                    vy = (-sin(a) * speed).toFloat(),
                    color = Color(colorArgb.toInt()),
                    pixelSize = pixelSize,
                )
            }
            CannonShot(delaySeconds = index * shotDelaySec, particles = particles)
        }
    }

    var timeMs by remember(animation) { mutableStateOf(0) }
    LaunchedEffect(animation) {
        timeMs = 0
        try {
            animate(
                initialValue = 0f,
                targetValue = animation.duration.toInt(DurationUnit.MILLISECONDS).toFloat(),
                animationSpec = tween(
                    durationMillis = animation.duration.toInt(DurationUnit.MILLISECONDS),
                    easing = LinearEasing,
                ),
            ) { value, _ -> timeMs = value.roundToInt() }
        } finally {
            vm.notifyAnimationFinished()
        }
    }

    // Alpha based on per-shot burst duration (each shot fades independently)
    val alphaAnim = remember(animation) {
        TargetBasedAnimation(
            animationSpec = tween(
                durationMillis = animation.burstDuration.toInt(DurationUnit.MILLISECONDS),
                // Only make the confetti translucent towards the very end
                easing = CubicBezierEasing(0.0f, 0.0f, 0.85f, 1.0f),
            ),
            typeConverter = Float.VectorConverter,
            initialValue = 1f,
            targetValue = 0f,
        )
    }

    // Control how the particles move through the air
    val gravity = 200f * (squareSize / 40f)
    val drag = 1.5f
    val invDrag = 1f / drag

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = timeMs / 1000f
        shots.forEach { shot ->
            val tShot = t - shot.delaySeconds
            if (tShot < 0f) return@forEach
            val alpha = alphaAnim.getValueFromNanos((tShot * 1000).toLong() * 1_000_000L)
            val expDecay = exp(-drag * tShot)
            val oneMinusExpDecay = 1f - expDecay
            shot.particles.forEach { p ->
                val px = p.x0 + p.vx * invDrag * oneMinusExpDecay
                val py = p.y0 + p.vy * invDrag * oneMinusExpDecay + gravity * invDrag * (tShot - invDrag * oneMinusExpDecay)
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(px, py),
                    size = Size(p.pixelSize, p.pixelSize),
                )
            }
        }
    }
}
