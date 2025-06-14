package com.jervisffb.engine.commands.context

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Helper command, making it easier to modify complex
 * [com.jervisffb.engine.model.context.ProcedureContext] objects that themselves
 * contain lists of items.
 *
 * This command can add items to those lists, so we do not have to copy the
 * entire list in order to modify it..
 *
 * @see com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushContext
 * @see com.jervisffb.engine.model.context.DodgeRollContext
 */
class AddContextListItem<T>(private val list: MutableList<T>, private val items: List<T>): Command {
    constructor(list: MutableList<T>, item: T): this(list, listOf(item))

    override fun execute(state: Game) {
        list.addAll(items)
    }

    override fun undo(state: Game) {
        items.reversed().forEach {
            if (!list.remove(it)) {
                INVALID_GAME_STATE("Could not remove item from list: $it")
            }
        }
    }
}


