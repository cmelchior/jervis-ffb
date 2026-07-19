package com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.AccuracyModifier
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.DisturbingPresenceModifier
import com.jervisffb.engine.model.modifiers.QualityModifier
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowPlayerResult
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.sum
import kotlinx.collections.immutable.toPersistentList

/**
 * Implement the Accuracy Roll for Throw Teammate as described on page 77 in the
 * BB2025 rulebook.
 *
 * The result is stored in [ThrowTeamMateContext] and it is up to the caller to
 * determine what to do with the result.
 */
object ThrowTeammateAccuracyRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.ACCURACY
    override val initialNode: Node = ChooseToUseStrongArm
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<ThrowTeamMateContext>()
        return UpdateContext(addInitialModifiersToContext(context, state, rules))
    }
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<ThrowTeamMateContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<ThrowTeamMateContext>().thrower

    object ChooseToUseStrongArm: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state).team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<ThrowTeamMateContext>()
            val isStrongArmAvailable = context.thrower.isSkillAvailable(SkillType.STRONG_ARM)
            return if (isStrongArmAvailable) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()

            val useStrongArm = (action == Confirm)
            return if (useStrongArm) {
                compositeCommandOf(
                    ReportSkillUsed(context.thrower, SkillType.STRONG_ARM),
                    UpdateContext(context.copy(qualityRollModifiers = context.qualityRollModifiers.add(QualityModifier.STRONG_ARM))),
                    GotoNode(RollDie)
                )
            } else {
                GotoNode(RollDie)
            }
        }
    }

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            return updateThrowTeamMateContext(state, d6, reroll = false)
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
            return updateThrowTeamMateContext(state, d6, reroll = true)
        }
    }

    // HELPER METHODS

    private fun addInitialModifiersToContext(context: ThrowTeamMateContext, state: Game, rules: Rules): ThrowTeamMateContext {
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

        // Disturbing Presence
        val thrower = context.thrower
        val playersWithDisturbingPresence = thrower.coordinates
            .getSurroundingCoordinates(rules, distance = 3, includeOutOfBounds = false)
            .mapNotNull { state.pitch[it].player }
            .filter { it.team != thrower.team }
            .count { it.isSkillAvailable(SkillType.DISTURBING_PRESENCE) }
        modifiers.add(DisturbingPresenceModifier(playersWithDisturbingPresence, AccuracyModifier.DISTURBING_PRESENCE))

        return context.copy(
            qualityRollModifiers = modifiers.toPersistentList()
        )
    }

    private fun updateThrowTeamMateContext(state: Game, d6: D6Result, reroll: Boolean): ThrowTeamMateContext {
        val context = state.getContext<ThrowTeamMateContext>()

        // Calculate throw result.
        val passingStat = context.thrower.passing ?: Int.MAX_VALUE
        val modifierTotal = context.qualityRollModifiers.sum()
        val result = when {
            // The value can be modified below 1. Players with PA 1+ will fumble
            // on a modified 0 and below.
            context.thrower.passing == null -> ThrowPlayerResult.FUMBLED
            d6.value == 6 -> ThrowPlayerResult.SUPERB
            d6.value == 1 && passingStat != 1 -> ThrowPlayerResult.FUMBLED
            d6.value + modifierTotal <= 1 && passingStat > 1 -> ThrowPlayerResult.FUMBLED
            d6.value + modifierTotal <= 0 && passingStat == 1 -> ThrowPlayerResult.FUMBLED
            d6.value + modifierTotal >= passingStat -> ThrowPlayerResult.SUPERB
            d6.value + modifierTotal < passingStat -> ThrowPlayerResult.SUBPAR
            else -> INVALID_GAME_STATE("Unsupported result: ${d6.value}, target: $passingStat, modifierTotal: $modifierTotal")
        }

        return if (reroll) {
            context.copy(
                qualityRoll = context.qualityRoll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6
                ),
                qualityRollResult = result
            )
        } else {
            context.copy(
                qualityRoll = D6DieRoll.create(state, d6),
                qualityRollResult = result
            )
        }
    }
}
