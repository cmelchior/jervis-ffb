package dk.ilios.jervis.model.inducements.wizards

import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.inducements.InducementEffect
import dk.ilios.jervis.model.inducements.Spell
import dk.ilios.jervis.model.inducements.Timing
import dk.ilios.jervis.procedures.inducements.spells.FireBallProcedure
import dk.ilios.jervis.procedures.inducements.spells.ZapProcedure

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

