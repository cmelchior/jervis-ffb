package com.jervisffb.engine.commands.context

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Helper command, making it easier to modify complex
 * [com.jervisffb.engine.model.context.ProcedureContext] objects that themselves
 * contain lists of items.
 *
 * This command can replace an item in those lists, so we do not have to
 * copy the entire list to modify it. The default is the last item.
 *
 * @see com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushContext
 * @see com.jervisffb.engine.model.context.DodgeRollContext
 */
class ReplaceContextListItem<T: Any>(
    private val list: MutableList<T>,
    private val item: T,
    private val index: Int = list.lastIndex
): Command {

    private lateinit var originalItem: T

    override fun execute(state: Game) {
        originalItem = list.lastOrNull() ?: INVALID_GAME_STATE("List is empty")
        list[index] = item
    }
    
    override fun undo(state: Game) {
        list[index] = originalItem
    }
}
