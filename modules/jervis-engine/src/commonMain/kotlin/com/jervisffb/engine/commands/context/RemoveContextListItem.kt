package com.jervisffb.engine.commands.context

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game

/**
 * Helper command, making it easier to modify complex
 * [com.jervisffb.engine.model.context.ProcedureContext] objects that themselves
 * contain lists of items.
 *
 * This command can remove items from those lists, so we do not have to copy the
 * entire list to modify it.
 *
 * @see com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushContext
 * @see com.jervisffb.engine.model.context.DodgeRollContext
 */
class RemoveContextListItem<T>(private val list: MutableList<T>, private val item: T): Command {
    var index: Int = 0
    override fun execute(state: Game) {
        index = list.indexOf(item)
        list.remove(item)
    }

    override fun undo(state: Game) {
        list.add(index, item)
    }
}


