package com.jervisffb.engine.rules.common.rerolls

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.skills.RerollSource
import kotlinx.serialization.Serializable

/**
 * Class representing a reroll option for dice rolls.
 *
 * For dice pools with multiple dice, like blocks, selecting rerolls is done
 * in two steps:
 *  1. Select the reroll source.
 *  2. Select the dice to reroll.
 *
 * This means that [dice] might be `null`.
 *
 * For dice pools with a single die, like dodging, the dice list can be
 * populated directly.
 */
@Serializable
data class DiceRerollOption(
    val rerollId: RerollSourceId,
    val dice: List<DieRoll<*>>? = null,
) {
    constructor(rerollId: RerollSourceId, dieRoll: DieRoll<*>): this(rerollId, listOf(dieRoll))
    constructor(source: RerollSource, dieRoll: DieRoll<*>): this(source.id, listOf(dieRoll))

    fun getRerollSource(game: Game): RerollSource {
        return game.getRerollSourceById(rerollId)
    }
}
