package com.jervisffb.ui.game.view.pitch

import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.utils.safeTryEmit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * Enumerate which types can be registered through
 * [com.jervisffb.ui.game.view.pitch.jervisPointerEvent]
 */
enum class SquarePointerEventType {
    EnterSquare,
    ExitSquare,
    PrimaryClickSquare,
    SecondaryClickSquare,
}

data class SquarePressedEvent(
    val square: PitchCoordinate,
    val isPrimary: Boolean,
)

/**
 * Interceptor that can be registered with [PointerEventBus] to intercept
 * pointer events before they are dispatched to regular listeners.
 *
 * For now, this interface contains only the minimal number of methods. More
 * can be added later if needed.
 */
interface PointerEventInterceptor {
    /**
     * Return `true` to consume the press, which will prevent the click from reaching other layers.
     */
    fun onPress(square: PitchCoordinate, isPrimary: Boolean): Boolean
}

/**
 * Custom bus for propagating pointer events to the Pitch to all pitch layers.
 *
 * Compose restrict hit testing to the first node that handles pointer events,
 * so in the case where we have multiple independent layers, only the first layer
 * with pointer handling will see them.
 *
 * This custom bus allows the wrapping box to intercept all pointer events and
 * convert them to "square"-evens that can be propagated to any layer that is
 * interested.
 *
 * This also means that layers should not be using `clickable` or `pointerInput`
 * modifiers. Layers that need to intercept events before they reach pitch squares
 * should register a [PointerEventInterceptor] instead.
 */
class PointerEventBus {
    private val interceptors = mutableListOf<PointerEventInterceptor>()
    private val exitPitch = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val enterSquare = MutableSharedFlow<PitchCoordinate>(extraBufferCapacity = 1)
    private val exitSquare = MutableSharedFlow<PitchCoordinate>(extraBufferCapacity = 1)
    private val primaryClickSquare = MutableSharedFlow<PitchCoordinate?>(extraBufferCapacity = 1)
    private val secondaryClickSquare = MutableSharedFlow<PitchCoordinate?>(extraBufferCapacity = 1)
    private val currentHover = MutableSharedFlow<PitchCoordinate?>(extraBufferCapacity = 1)

    var lastSquarePressed: SquarePressedEvent? = null
    var lastMoveSquare: PitchCoordinate? = null

    fun addInterceptor(interceptor: PointerEventInterceptor) { interceptors.add(interceptor) }
    fun removeInterceptor(interceptor: PointerEventInterceptor) { interceptors.remove(interceptor) }

    fun notifyMove(eventSquare: PitchCoordinate) {
        if (lastMoveSquare == eventSquare) return // Ignore mouse events within the same square
        lastMoveSquare?.let {
            if (it != eventSquare) {
                exitSquare.tryEmit(it)
                currentHover.tryEmit(it)
            }
        }
        enterSquare.tryEmit(eventSquare)
        currentHover.tryEmit(eventSquare)
        lastMoveSquare = eventSquare
    }

    fun notifyEnterField(eventSquare: PitchCoordinate) {
        notifyMove(eventSquare)
    }

    fun notifyExitPitch() {
        lastMoveSquare = null
        // Make sure that exiting and entering the field on the same square is reported correctly
        enterSquare.tryEmit(PitchCoordinate.UNKNOWN)
        exitPitch.tryEmit(Unit)
    }

    fun notifyPressSquare(eventSquare: PitchCoordinate, isPrimary: Boolean) {
        val consumed = interceptors.any { it.onPress(eventSquare, isPrimary) }
        if (!consumed) {
            lastSquarePressed = SquarePressedEvent(eventSquare, isPrimary)
        }
    }

    fun notifyReleaseSquare(eventSquare: PitchCoordinate?) {
        if (lastSquarePressed?.square == eventSquare && eventSquare != null) {
            if (lastSquarePressed?.isPrimary == true) {
                primaryClickSquare.safeTryEmit(eventSquare)
            } else {
                secondaryClickSquare.safeTryEmit(eventSquare)
            }
        }
        lastSquarePressed = null
    }


    fun enterSquare(square: PitchCoordinate): Flow<Boolean> {
        return enterSquare.map { it == square }.distinctUntilChanged()
    }

    fun exitSquare(square: PitchCoordinate): Flow<Boolean> {
        return exitSquare.map { it == square }.distinctUntilChanged()
    }
    fun hoverSquare(square: PitchCoordinate): Flow<Boolean> {
        return currentHover.map { it == square }.distinctUntilChanged()
    }
    fun primaryClickSquare(square: PitchCoordinate): Flow<Boolean> {
        return primaryClickSquare.map { it == square }.filter { it }
    }
    fun secondaryClickSquare(square: PitchCoordinate): Flow<Boolean> {
        return secondaryClickSquare.map { it == square }.filter { it }
    }

    fun exitPitch(): Flow<Unit> {
        return exitPitch
    }
}
