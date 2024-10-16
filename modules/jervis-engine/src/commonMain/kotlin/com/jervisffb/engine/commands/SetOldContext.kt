package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import kotlin.reflect.KMutableProperty1

class SetOldContext<T>(private val property: KMutableProperty1<Game, T?>, private val value: T) : Command {
    var originalValue: T? = null

    override fun execute(
        state: Game,
    ) {
        originalValue = property.get(state)
        property.set(state, value)
    }

    override fun undo(
        state: Game,
    ) {
        try {
            property.set(state, originalValue)
        } catch (ex: Exception) {
            println(ex)
        }
    }
}
