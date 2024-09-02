package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.procedures.BlockDieRoll
import dk.ilios.jervis.rules.skills.DiceRollType

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
