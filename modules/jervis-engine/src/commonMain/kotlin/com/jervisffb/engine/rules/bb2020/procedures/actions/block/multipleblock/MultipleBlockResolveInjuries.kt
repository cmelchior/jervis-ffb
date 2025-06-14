package com.jervisffb.engine.rules.bb2020.procedures.actions.block.multipleblock

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.MultipleBlockContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.PatchUpPlayer
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure responsible for resolving the "Injury Pool" for a Multiple Block.
 *
 * Reading the rules, it is unclear if resolving injuries is resolved "simultaneous".
 * The answer is probably yes, and would affect things like choosing which failed
 * regeneration roll to use a Mortuary Assistant on.
 *
 * For now, we instead just choose which player to fully resolve.
 * To make a proper flow, we need to split [PatchUpPlayer] into steps that can be run
 * in parallel, similar to [StandardBlocKStep]
 */
object MultipleBlockResolveInjuries: Procedure() {
    override val initialNode: Node = SelectDefender
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    // TODO It isn't guaranteded that it is the blocker team that selects the dice.
    // We might need to have both attacker and defender choose dice
    object SelectDefender : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MultipleBlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MultipleBlockContext>()
            val defender1Injury = context.defender1InjuryContext
            val defender2Injury = context.defender2InjuryContext

            val injuredPlayers = buildList {
                if (defender1Injury != null) add(context.defender1!!.id)
                if (defender2Injury != null) add(context.defender2!!.id)
            }
            return if (injuredPlayers.isNotEmpty())
                listOf(SelectPlayer(injuredPlayers))
            else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return when (action) {
                Continue -> {
                    if (context.attackerInjuryContext != null) GotoNode(PatchUpAttacker) else ExitProcedure()
                }
                else -> {
                    checkTypeAndValue<PlayerSelected>(state, action) { playerSelected ->
                        val context = state.getContext<MultipleBlockContext>()
                        val selectedPlayer = playerSelected.getPlayer(state)
                        val index = when (selectedPlayer) {
                            context.defender1 -> 0
                            context.defender2 -> 1
                            else -> INVALID_ACTION(playerSelected)
                        }
                        compositeCommandOf(
                            SetContext(context.copy(activeDefender = index)),
                            GotoNode(PatchUpSelectedDefender)
                        )
                    }
                }
            }
        }
    }

    object PatchUpSelectedDefender: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            val injuryContext = when (context.activeDefender) {
                0 -> context.defender1InjuryContext!!
                1 -> context.defender2InjuryContext!!
                else -> INVALID_GAME_STATE("Invalid active defender: ${context.activeDefender}")
            }
            return SetContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PatchUpPlayer
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            val updatedContext = when (context.activeDefender) {
                0 -> context.copy(defender1InjuryContext = null, activeDefender = null)
                1 -> context.copy(defender2InjuryContext = null, activeDefender = null)
                else -> INVALID_GAME_STATE("Invalid active defender: ${context.activeDefender}")
            }
            return compositeCommandOf(
                SetContext(updatedContext),
                GotoNode(SelectDefender)
            )
        }
    }

    object PatchUpAttacker: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return SetContext(context.attackerInjuryContext!!)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PatchUpPlayer
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
