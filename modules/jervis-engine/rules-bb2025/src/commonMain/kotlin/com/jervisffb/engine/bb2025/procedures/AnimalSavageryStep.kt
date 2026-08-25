package com.jervisffb.engine.bb2025.procedures

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.bb2025.modifiers.distracted
import com.jervisffb.engine.commands.AddPlayerStatusEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.SetHasTackleZones
import com.jervisffb.engine.common.context.ActivatePlayerContext
import com.jervisffb.engine.common.context.AnimalSavageryContext
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.procedures.AnimalSavageryRoll
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.rules.Rules

/**
 * Procedure controlling rolling for the Animal Savagery negatrait in BB2025.
 */
object AnimalSavageryStep : Procedure() {
    override val initialNode: Node = RollForAnimalSavagery
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<ActivatePlayerContext>()
        return AddContext(AnimalSavageryContext(player = context.player))
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command =
        RemoveContext<AnimalSavageryContext>()

    object RollForAnimalSavagery : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = AnimalSavageryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val activateContext = state.getContext<ActivatePlayerContext>()
            val context = state.getContext<AnimalSavageryContext>()
            val isAdjacentTeamPlayer = context.player.coordinates
                .getSurroundingCoordinates(rules)
                .mapNotNull { state.pitch[it].player }
                .filter { it.team == context.player.team }
                .any { rules.isStanding(it) }
            return when {
                context.isSuccess -> compositeCommandOf(
                    UpdateContext(activateContext.copy(rolledForNegaTrait = true, markActionAsUsed = true)),
                    ExitProcedure(),
                )
                isAdjacentTeamPlayer -> compositeCommandOf(
                    UpdateContext(activateContext.copy(rolledForNegaTrait = true, markActionAsUsed = true)),
                    GotoNode(SelectAdjacentPlayer),
                )
                else -> compositeCommandOf(
                    AddPlayerStatusEffect(context.player, PlayerStatusEffect.distracted()),
                    SetHasTackleZones(context.player, hasTackleZones = false),
                    UpdateContext(
                        activateContext.copy(
                            rolledForNegaTrait = true,
                            activationEndsImmediately = true,
                            markActionAsUsed = true,
                        ),
                    ),
                    ExitProcedure(),
                )
            }
        }
    }

    object SelectAdjacentPlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<AnimalSavageryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<AnimalSavageryContext>()
            val eligiblePlayers = context.player.coordinates
                .getSurroundingCoordinates(rules)
                .mapNotNull { state.pitch[it].player }
                .filter { it.team == context.player.team }
            return listOf(SelectPlayer.fromPlayers(eligiblePlayers))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val player = action.safeCast<PlayerSelected>().getPlayer(state)
            val context = state.getContext<AnimalSavageryContext>()
            return compositeCommandOf(
                UpdateContext(context.copy(selectedAdjacentPlayer = player)),
                GotoNode(HitAdjacentPlayer),
            )
        }
    }

    object HitAdjacentPlayer : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<AnimalSavageryContext>()
            return AddContext(
                RiskingInjuryContext(
                    player = context.selectedAdjacentPlayer!!,
                    causedBy = context.player,
                    mode = RiskingInjuryMode.KNOCKED_DOWN,
                ),
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.knockedDownStep
        override fun onExitNode(state: Game, rules: Rules): Command = compositeCommandOf(
            RemoveContext<RiskingInjuryContext>(),
            ExitProcedure(),
        )
    }
}
