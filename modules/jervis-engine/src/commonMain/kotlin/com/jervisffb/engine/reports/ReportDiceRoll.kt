package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.rules.bb2020.procedures.BlockDieRoll
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType

class ReportDiceRoll(
    private val type: DiceRollType,
    private val dice: List<DieResult>,
    private val showDiceType: Boolean = false) : LogEntry() {
    constructor(type: DiceRollType, die: DieResult): this(type, listOf(die))
    constructor(roll: List<BlockDieRoll>) : this(DiceRollType.BLOCK, roll.map { it.result })

    override val category: LogCategory = LogCategory.DICE_ROLL
    override val message: String
        get() {
            val dice = dice.joinToString(" ") { it ->
                // For now, just do the easy thing
                val diceType = if (showDiceType) {
                    "d${it.max}="
                } else {
                    ""
                }
                when (it) {
                    is DBlockResult -> "[$diceType${it.blockResult.name}]"
                    else -> "[$diceType${it.value}]"
                }
            }
            return "${type.name} Roll $dice"
        }
}
