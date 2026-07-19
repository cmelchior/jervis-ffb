package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.modifiers.DiceModifier

/**
 * Generic command for adding a dice modifier to any list tracking them
 */
class AddDiceModifier<T: DiceModifier>(private val modifier: T, private val modifiers: MutableList<T>): Command {

    override fun execute(state: Game) {
        modifiers.add(modifier)
    }

    override fun undo(state: Game) {
        modifiers.remove(modifier)
    }
}
