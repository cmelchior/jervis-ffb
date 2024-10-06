package com.jervisffb.engine.model.inducements.wizards

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.inducements.Spell
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.rules.bb2020.procedures.inducements.spells.FireBallProcedure
import com.jervisffb.engine.rules.bb2020.procedures.inducements.spells.ZapProcedure

// Fireball spell - See page 94 in the rulebook
class Fireball(override val wizard: Wizard) : Spell {
    override val name: String = "Fireball"
    override var used: Boolean = false
    override val triggers = listOf(
        Timing.START_OF_OPPONENT_TURN,
        Timing.END_OF_OPPONENT_TURN
    )
    override val procedure: Procedure = FireBallProcedure
}

// Zap! spell - See page 94 in the rulebook
class Zap(override val wizard: Wizard) : Spell {
    override val name: String = "Zap!"
    override var used: Boolean = false
    override val triggers = listOf(
        Timing.START_OF_OPPONENT_TURN,
        Timing.END_OF_OPPONENT_TURN
    )
    override val procedure: Procedure = ZapProcedure
}

