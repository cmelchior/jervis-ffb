package com.jervisffb.ui.animations

import androidx.compose.ui.geometry.Offset
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.ui.toRadians
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PassAnimation(val from: OnFieldLocation, val to: FieldCoordinate, val outOufBounds: Boolean) : JervisAnimation {

    val viewingDistance = 500f
    val angleRadians = toRadians(45.0)
    val g = AnimationFactory.GRAVITY

    // (x, y) in field coordinate system in pixels
    data class BallState(val x: Float, val y: Float, val scale: Float)

    /**
     * Returns a lamda that can be used to calculate the on-field (x, y) position
     * on the field as the ball is moving as being thrown.
     *
     * This means we have to map a 3D-experience to a 2D-experience with the
     * ball being watched from above.
     *
     * This is done by using the Projectile Motion Equation and Size-Distance
     * Scaling Equation while we map between "projectile"-space and "on-field"-
     * space.
     *
     * "projectile"-space is defined as a 2D-coordinate system where (0, 0)
     * is the square the ball starts from and (x, 0) is the distance to the
     * target square. The `y-parameter` in this space is used to determine
     * how the ball scales in "on-field"-space.
     *
     * "on-field"-space is the 2D coordinate system representing the field, i.e.
     * (0, 0) is the top-left corner of the field.
     */
    fun getStateCalculator(
        startOffset: Offset,
        endOffset: Offset,
    ): Pair<Duration, (Float) -> BallState> {

        // Start, Target and Angle is user defined, so we need to calculate how
        // hard the ball has to be thrown (velocity). This also partly
        // affects duration. So we need to somehow adjust this so a long pass
        // doesn't take forever (figure out how to do this)

        // Distance between start and target in pixels
        // We need to use the Composables as source-of-truth as they differ
        // slightly in size due to rounding errors.
        val startX = startOffset.x
        val startY = startOffset.y
        val endX = endOffset.x
        val endY = endOffset.y
        val onFieldDistancePx = sqrt((endX - startX).toDouble().pow(2) + (endY - startY).toDouble().pow(2))

        // Calculate all parameters for the Projectile motion
        val initialVelocity = calculateVelocity(onFieldDistancePx, angleRadians)
        val throwDurationSeconds = calculateDurationSeconds(initialVelocity, angleRadians)

        // Create mapper function that given a projectile distance (along the ground)
        // can map back to the field coordinate system
        val projectileCoordinateMapper = createCoordinateMapper(startOffset, endOffset)
        val timeToFieldCoordinatesMapper: (Float) -> BallState = { time ->
            // Calculate position in "projectile"-space
            val projectileX = initialVelocity * cos(angleRadians) * time
            val projectileY = (initialVelocity * sin(angleRadians) * time) - (0.5f * g * time.pow(2))

            // Apply the Size-Distance scaling to the ball.
            // At max throwing distance (corner to corner), the maximum height is ~475.
            // It looks a bit funny when throwing that distance, so we could choose to
            // adjust the reference distance at very long distances, but keep it
            // simple for now.
            val referenceDistance = viewingDistance // Distance of viewer in pixels
            val currentDistance = referenceDistance - projectileY
            val scale = 1.0 * referenceDistance / currentDistance

            // Convert to "on-field"-space
            val (onFieldX, onFieldY) = projectileCoordinateMapper(projectileX)
            BallState(onFieldX.toFloat(), onFieldY.toFloat(), scale.toFloat())
        }
        return Pair(throwDurationSeconds.seconds, timeToFieldCoordinatesMapper)
    }

    private fun createCoordinateMapper(startOffset: Offset, endOffset: Offset): (Double) -> Pair<Double, Double> {
        val startX = startOffset.x
        val startY = startOffset.y
        val endX = endOffset.x
        val endY = endOffset.y
        val totalFieldDistancePx = sqrt((endX - startX).toDouble().pow(2) + (endY - startY).toDouble().pow(2))
        return { projectileDistance: Double ->
            val d = projectileDistance / totalFieldDistancePx
            val pX = startX + d * (endX - startX)
            val pY = startY + d * (endY - startY)
            Pair(pX, pY)
        }
    }

    /**
     * Calculate initial velocity for the throw to hit the given target.
     */
    private fun calculateVelocity(distance: Double, angleRadian: Double): Double {
        // Calculate the initial velocity using the projectile motion equation
        // Create coordinates so we have a 2D-coordinate with (0, 0) = start and (dist, 0) = target
        // See https://www.youtube.com/watch?v=_KLfj84SOh8&ab_channel=ErikRosolowsky
        val y0 = 0f
        val x = distance
        val result =  sqrt((g * x.pow(2)) / (2 * cos(angleRadian) * cos(angleRadian) * (y0 + x * tan(angleRadian))))
        return result
    }

    /**
     * Calculate how long the throw will take using projectile motion.
     * Return value is in seconds.
     */
    private fun calculateDurationSeconds(velocity: Double, angle: Double): Double {
        val time = 2 * velocity * sin(angle) / g
        return time
    }
}
