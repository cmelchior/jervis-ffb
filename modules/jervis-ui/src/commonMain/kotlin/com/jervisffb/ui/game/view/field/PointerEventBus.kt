package com.jervisffb.ui.game.view.field

import com.jervisffb.engine.model.locations.FieldCoordinate
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
    ClickSquare
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
 * modifiers.
 */
class PointerEventBus {
    private val exitField = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val enterSquare = MutableSharedFlow<FieldCoordinate>(extraBufferCapacity = 1)
    private val exitSquare = MutableSharedFlow<FieldCoordinate>(extraBufferCapacity = 1)
    private val clickSquare = MutableSharedFlow<FieldCoordinate?>(extraBufferCapacity = 1)
    private val currentHover = MutableSharedFlow<FieldCoordinate?>(extraBufferCapacity = 1)

    var lastSquarePressed: FieldCoordinate? = null
    var lastMoveSquare: FieldCoordinate? = null

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

    fun notifyPressSquare(eventSquare: FieldCoordinate) {
        lastSquarePressed = eventSquare
    }

    fun notifyReleaseSquare(eventSquare: FieldCoordinate?) {
        if (lastSquarePressed == eventSquare && eventSquare != null) {
            clickSquare.tryEmit(eventSquare)
        }
        lastSquarePressed == null
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
    fun clickSquare(square: FieldCoordinate): Flow<Boolean> {
        return clickSquare.map { it == square }.filter { it }
    }

    fun exitField(): Flow<Unit> {
        return exitField
    }
}
