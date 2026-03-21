package com.jervisffb.ui.game.view.field

import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.utils.safeTryEmit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * Enumerate which types can be registered through
 * [com.jervisffb.ui.game.view.field.jervisPointerEvent]
 */
enum class FieldPointerEventType {
    EnterSquare,
    ExitSquare,
    PrimaryClickSquare,
    SecondaryClickSquare,
}

data class SquarePressedEvent(
    val square: FieldCoordinate,
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
    fun onPress(square: FieldCoordinate, isPrimary: Boolean): Boolean
}

/**
 * Custom bus for propagating pointer events to the field to all field layers.
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
 * modifiers. Layers that need to intercept events before they reach field squares
 * should register a [PointerEventInterceptor] instead.
 */
class PointerEventBus {
    private val interceptors = mutableListOf<PointerEventInterceptor>()
    private val exitField = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val enterSquare = MutableSharedFlow<FieldCoordinate>(extraBufferCapacity = 1)
    private val exitSquare = MutableSharedFlow<FieldCoordinate>(extraBufferCapacity = 1)
    private val primaryClickSquare = MutableSharedFlow<FieldCoordinate?>(extraBufferCapacity = 1)
    private val secondaryClickSquare = MutableSharedFlow<FieldCoordinate?>(extraBufferCapacity = 1)
    private val currentHover = MutableSharedFlow<FieldCoordinate?>(extraBufferCapacity = 1)

    var lastSquarePressed: SquarePressedEvent? = null
    var lastMoveSquare: FieldCoordinate? = null

    fun addInterceptor(interceptor: PointerEventInterceptor) { interceptors.add(interceptor) }
    fun removeInterceptor(interceptor: PointerEventInterceptor) { interceptors.remove(interceptor) }

    fun notifyMove(eventSquare: FieldCoordinate) {
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

    fun notifyEnterField(eventSquare: FieldCoordinate) {
        notifyMove(eventSquare)
    }

    fun notifyExitField() {
        lastMoveSquare = null
        // Make sure that exiting and entering the field on the same square is reported correctly
        enterSquare.tryEmit(FieldCoordinate.UNKNOWN)
        exitField.tryEmit(Unit)
    }

    fun notifyPressSquare(eventSquare: FieldCoordinate, isPrimary: Boolean) {
        val consumed = interceptors.any { it.onPress(eventSquare, isPrimary) }
        if (!consumed) {
            lastSquarePressed = SquarePressedEvent(eventSquare, isPrimary)
        }
    }

    fun notifyReleaseSquare(eventSquare: FieldCoordinate?) {
        if (lastSquarePressed?.square == eventSquare && eventSquare != null) {
            if (lastSquarePressed?.isPrimary == true) {
                primaryClickSquare.safeTryEmit(eventSquare)
            } else {
                secondaryClickSquare.safeTryEmit(eventSquare)
            }
        }
        lastSquarePressed = null
    }


    fun enterSquare(square: FieldCoordinate): Flow<Boolean> {
        return enterSquare.map { it == square }.distinctUntilChanged()
    }

    fun exitSquare(square: FieldCoordinate): Flow<Boolean> {
        return exitSquare.map { it == square }.distinctUntilChanged()
    }
    fun hoverSquare(square: FieldCoordinate): Flow<Boolean> {
        return currentHover.map { it == square }.distinctUntilChanged()
    }
    fun primaryClickSquare(square: FieldCoordinate): Flow<Boolean> {
        return primaryClickSquare.map { it == square }.filter { it }
    }
    fun secondaryClickSquare(square: FieldCoordinate): Flow<Boolean> {
        return secondaryClickSquare.map { it == square }.filter { it }
    }

    fun exitField(): Flow<Unit> {
        return exitField
    }
}
