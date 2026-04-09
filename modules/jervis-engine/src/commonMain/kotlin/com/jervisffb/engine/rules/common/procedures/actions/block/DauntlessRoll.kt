package com.jervisffb.engine.rules.common.procedures.actions.block

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DauntlessStrengthModifier
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.reports.ReportDauntlessResult
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType

data class DauntlessRollContext(
    val attacker: Player,
    val defender: Player,
    val roll: D6DieRoll? = null,
    val modifier: StatModifier? = null
): ProcedureContext {
    val isSuccess = (modifier != null)
}

/**
 * Implement the Dauntless Roll as described on page 127 in the BB2025 rulebook.
 *
 * The result is stored in [DauntlessRollContext] and it is up to the caller to
 * determine what to do with the result.
 */
object DauntlessRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.DAUNTLESS
    override val initialNode: Node get() = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<DauntlessRollContext>()
        return ReportSkillUsed(context.attacker, SkillType.DAUNTLESS)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<DauntlessRollContext>()
        return ReportDauntlessResult(context)
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<DauntlessRollContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<DauntlessRollContext>().attacker.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<DauntlessRollContext>()
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                modifier = calculateModifier(context.attacker, context.defender, d6),
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<DauntlessRollContext>()
            return RerollData(context.attacker, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<DauntlessRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6,
                ),
                modifier = calculateModifier(context.attacker, context.defender, d6),
            )
        }
    }

    // -- HELPER FUNCTIONS --
    fun calculateModifier(attacker: Player, defender: Player, d6: D6Result): StatModifier? {
        // Dauntless is based on "unmodified strength", which we interpret as strength without
        // modifiers that does not have a PERMANENT duration.
        val defenderStrength = defender.strengthModifiers.filter { it.expiresAt == Duration.PERMANENT }.sumOf { it.modifier } + defender.baseStrength
        val attackerStrength = attacker.strengthModifiers.filter { it.expiresAt == Duration.PERMANENT }.sumOf { it.modifier } + attacker.baseStrength
        val isDefenderWeaker = attackerStrength >= defenderStrength
        // We should never encounter this, but better safe than sorry
        if (isDefenderWeaker) {
            return null
        }
        val diff = defenderStrength - attackerStrength
        return if (d6.value > diff) {
            DauntlessStrengthModifier(modifier = diff)
        } else {
            null
        }
    }
}
