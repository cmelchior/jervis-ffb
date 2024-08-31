package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import kotlin.reflect.KMutableProperty1

/**
 * Set any state on the [Game] object using property access.
 * Currently only used for `temporary` state.
 */
class SetTemporaryState<T>(private val property: KMutableProperty1<Game, T>, private val value: T) : Command {
    var originalValue: T? = null

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalValue = property.get(state)
        property.set(state, value)
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        try {
            property.set(state, originalValue!!)
        } catch (ex: Exception) {
            println(ex)
        }
    }
}

class SetOldContext<T>(private val property: KMutableProperty1<Game, T?>, private val value: T) : Command {
    var originalValue: T? = null

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalValue = property.get(state)
        property.set(state, value)
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        try {
            property.set(state, originalValue)
        } catch (ex: Exception) {
            println(ex)
        }
    }
}
