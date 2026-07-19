package com.jervisffb.engine.rules.common.rerolls

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.procedures.UseStandardSkillReroll
import com.jervisffb.engine.rules.common.skills.RerollSource

interface D6StandardSkillReroll : RerollSource {
    override val rerollProcedure: Procedure
        get() =  UseStandardSkillReroll

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

