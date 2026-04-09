package com.jervisffb.engine.rules.bb2020.procedures.actions.throwteammate

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.QualityModifier
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowPlayerResult
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.sum
import kotlinx.collections.immutable.toPersistentList

/**
 * Implement the Quality Roll as described on page 53 in the rulebook.
 *
 * The result is stored in [com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext] and it is up
 * to the caller to determine what to do with the result.
 */
object QualityRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.QUALITY
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<ThrowTeamMateContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<ThrowTeamMateContext>().thrower.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            return updateThrowTeamMateContext(state, rules, d6, false)
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<ThrowTeamMateContext>()
            return RerollData(context.thrower, context.qualityRoll!!, null)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            return updateThrowTeamMateContext(state, rules, d6, true)
        }
    }

    // HELPER METHODS

    private fun updateThrowTeamMateContext(state: Game, rules: Rules, d6: D6Result, reroll: Boolean): ThrowTeamMateContext {
        val context = state.getContext<ThrowTeamMateContext>()
        val modifiers = mutableListOf<DiceModifier>()

        // Range modifier
        when (context.range) {
            Range.QUICK_PASS -> null
            Range.SHORT_PASS -> QualityModifier.SHORT_PASS
            else -> INVALID_GAME_STATE("Unsupported range: ${context.range}")
        }?.let { modifiers.add(it) }

        // Marked modifiers for thrower
        rules.addMarkedModifiers(
            state,
            context.thrower.team,
            context.thrower.coordinates,
            modifiers,
            QualityModifier.MARKED
        )

        // Weather
        if (state.weather == Weather.VERY_SUNNY) {
            modifiers.add(QualityModifier.VERY_SUNNY)
        }

        // Are there other quality roll modifiers? (Like disturbing presence)
        // TODO

        // Calculate throw result.
        val passingStat = context.thrower.passing ?: Int.MAX_VALUE
        val modifierTotal = modifiers.sum()
        val result = when {
            context.thrower.passing == null -> ThrowPlayerResult.FUMBLED
            d6.value == 1 -> ThrowPlayerResult.FUMBLED
            d6.value == 6 -> ThrowPlayerResult.SUPERB
            d6.value + modifierTotal == 1 -> ThrowPlayerResult.TERRIBLE
            d6.value + modifierTotal >= passingStat -> ThrowPlayerResult.SUPERB
            d6.value + modifierTotal < passingStat -> ThrowPlayerResult.SUCCESSFUL
            else -> INVALID_GAME_STATE("Unsupported result: ${d6.value}, target: $passingStat, modifierTotal: $modifierTotal")
        }

        return if (reroll) {
            context.copy(
                qualityRoll = context.qualityRoll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6
                ),
                qualityRollModifiers = modifiers.toPersistentList(),
                qualityRollResult = result
            )
        } else {
            context.copy(
                qualityRoll = D6DieRoll.create(state, d6),
                qualityRollModifiers = modifiers.toPersistentList(),
                qualityRollResult = result
            )
        }
    }
}
