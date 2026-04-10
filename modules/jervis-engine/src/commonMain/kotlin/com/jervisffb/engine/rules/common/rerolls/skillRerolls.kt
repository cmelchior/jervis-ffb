package com.jervisffb.engine.rules.common.rerolls

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.procedures.UseStandardSkillReroll
import com.jervisffb.engine.rules.common.skills.RerollSource
import kotlinx.serialization.Serializable

interface D6StandardSkillReroll : RerollSource {
    override val rerollProcedure: Procedure
        get() = UseStandardSkillReroll

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): List<DiceRerollOption> {
        // For standard skills
        if (value.size != 1) error("Unsupported number of dice: ${value.joinToString()}")
        return listOf(DiceRerollOption(this.id, value))
    }
}

@Serializable
data class DiceRerollOption(
    val rerollId: RerollSourceId,
    val dice: List<DieRoll<*>>,
) {
    constructor(rerollId: RerollSourceId, dieRoll: DieRoll<*>): this(rerollId, listOf(dieRoll))
    constructor(source: RerollSource, dieRoll: DieRoll<*>): this(source.id, listOf(dieRoll))

    fun getRerollSource(game: Game): RerollSource {
        return game.getRerollSourceById(rerollId)
    }
}
