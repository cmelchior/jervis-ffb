package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.SetApothecaryUsed
import com.jervisffb.engine.common.commands.SetMortuaryAssistantUsed
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.procedures.dicerolls.D6WithPlayerRerollProcedure
import com.jervisffb.engine.common.procedures.dicerolls.RerollData
import com.jervisffb.engine.common.reports.ReportApothecaryUsed
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.common.reports.ReportMortuaryAssistantUsed
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Procedure for rolling for Regeneration. See page 135 in the BB2025 rulebook.
 *
 * The procedure rolls a D6 and stores the result on [RiskingInjuryContext].
 * The roll is a success on a 4+ and the caller is responsible for reacting to
 * [RiskingInjuryContext.regenerationSuccess].
 *
 * Rerolls are supported through three sources: Team rerolls (only available
 * during the player's team's own turn — see NAF interpretation), a Mortuary
 * Assistant, or a Plague Doctor inducement.
 */
object RegenerationRoll: D6WithPlayerRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.REGENERATION
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun getActionOwner(state: Game): Player = state.getContext<RiskingInjuryContext>().player

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<RiskingInjuryContext>()
            return context.copy(
                regenerationRoll = D6DieRoll.create(d6),
                regenerationSuccess = calculateSuccess(d6),
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource(
        // If the standard reroll sources are exhausted or declined, fall back
        // to the inducement-based re-roll prompts. Only the Mortuary Assistant
        // and Plague Doctor can re-roll a Regeneration roll outside the team
        // reroll path.
        exitWithoutRerollCommand = { GotoNode(ChooseToUseMortuaryAssistant) },
    ) {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<RiskingInjuryContext>()
            return RerollData(context.player, context.regenerationRoll!!, context.regenerationSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<RiskingInjuryContext>()
            return context.copy(
                regenerationRoll = context.regenerationRoll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                regenerationSuccess = calculateSuccess(d6),
            )
        }
    }

    /**
     * If the team has an unused Mortuary Assistant, they can be used to
     * re-roll a failed Regeneration roll. See page 145 in the BB2025 rulebook.
     */
    object ChooseToUseMortuaryAssistant: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val regenerationSucceeded = context.regenerationSuccess
            val isAvailable = context.player.team.mortuaryAssistants.any { !it.used }
            return when (isAvailable && !regenerationSucceeded) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Confirm -> {
                    val context = state.getContext<RiskingInjuryContext>()
                    val team = context.player.team
                    val assistant = team.mortuaryAssistants.first { !it.used }
                    compositeCommandOf(
                        SetMortuaryAssistantUsed(team, assistant, true),
                        ReportMortuaryAssistantUsed(team, assistant),
                        UpdateContext(context.copy(regenerationMortuaryAssistantUsed = assistant)),
                        GotoNode(RerollUsingInducement),
                    )
                }
                Cancel,
                Continue -> GotoNode(ChooseToUsePlagueDoctor)
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * If the team has an unused Plague Doctor, they can be used to re-roll a
     * failed Regeneration roll. See page 145 in the BB2025 rulebook.
     */
    object ChooseToUsePlagueDoctor: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            if (context.regenerationSuccess) return listOf(ContinueWhenReady)
            val isAvailable = context.player.team.plagueDoctors.any { !it.used }
            return when (isAvailable) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Confirm -> {
                    val context = state.getContext<RiskingInjuryContext>()
                    val team = context.player.team
                    val apothecary = team.plagueDoctors.first { !it.used }
                    compositeCommandOf(
                        SetApothecaryUsed(team, apothecary, true),
                        ReportApothecaryUsed(team, apothecary),
                        UpdateContext(context.copy(regenerationApothecaryUsed = apothecary)),
                        GotoNode(RerollUsingInducement),
                    )
                }
                Cancel,
                Continue -> ExitProcedure()
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Re-roll a failed Regeneration roll using a Mortuary Assistant or Plague
     * Doctor. The reroll source is tracked separately on
     * [RiskingInjuryContext.regenerationApothecaryUsed], not on the
     * [D6DieRoll], since inducements are not modeled as [RerollSource].
     */
    object RerollUsingInducement: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6, type = DiceRollType.REGENERATION))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<RiskingInjuryContext>()
                val updatedContext = context.copy(
                    regenerationRoll = context.regenerationRoll!!.copyReroll(
                        rerollSource = null,
                        rerolledResult = d6,
                    ),
                    regenerationSuccess = calculateSuccess(d6),
                )
                return compositeCommandOf(
                    UpdateContext(updatedContext),
                    ReportDiceRoll(DiceRollType.REGENERATION, d6),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun calculateSuccess(d6: D6Result): Boolean {
        val target = 4
        return d6.value >= target
    }
}
