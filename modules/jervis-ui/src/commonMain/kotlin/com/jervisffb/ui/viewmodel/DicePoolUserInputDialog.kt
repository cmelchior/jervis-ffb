package com.jervisffb.ui.viewmodel

import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DicePool
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.model.Team

/**
 * Class wrapping the intent to show a dialog for selecting a result from a dice pool
 */
class DicePoolUserInputDialog(
    val icon: Any? = null, // TODO Replacement for Icon?
    val dialogTitle: String,
    val message: String,
    val poolTitles: List<String>,
    val dice: List<Pair<Dice, DicePool<*, *>>>,
    override var owner: Team? = null,
) : UserInputDialog {
    override val actions: List<GameAction> = emptyList()

    companion object {
        fun createSelectBlockDie(result: SelectDicePoolResult): UserInputDialog {
            if (result.pools.size != 1) throw IllegalStateException("Unexpected number of pools: ${result.pools.size}")
            return DicePoolUserInputDialog(
                dialogTitle = "Select Block Result",
                message = "Select die to apply",
                poolTitles = emptyList(),
                dice = result.pools.map { Pair(Dice.BLOCK, it)},
            )
        }
    }
}
