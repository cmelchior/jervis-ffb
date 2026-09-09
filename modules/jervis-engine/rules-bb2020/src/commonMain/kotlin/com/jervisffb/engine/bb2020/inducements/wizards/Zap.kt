package com.jervisffb.engine.bb2020.inducements.wizards

import com.jervisffb.engine.common.procedures.inducements.spells.ZapProcedure
import com.jervisffb.engine.common.procedures.inducements.spells.ZapSpell
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.InducementEffectId
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.WizardId
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.rules.common.roster.Position
import kotlinx.serialization.Serializable

// Zap! spell - See page 94 in the BB2020 rulebook
@Serializable
class Zap(val wizard: WizardId) : ZapSpell() {
    override val id: InducementEffectId = InducementEffectId("${wizard.value}-zap")
    override val name: String = "Zap!"
    override var used: Boolean = false
    override val triggers = listOf(
        Timing.START_OF_OPPONENT_TURN,
        Timing.END_OF_OPPONENT_TURN
    )
    override val procedure: Procedure = ZapProcedure

    // Zap! is not currently supported in BB2020, so the target is left alone.
    override fun getFrogPositionData(player: Player): Position = TODO("Not supported yet")
}
