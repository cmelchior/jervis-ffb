package com.jervisffb.engine.rules.bb2025.procedures.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.UseShadowingSkill
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.ShadowingRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.Pickup
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Class wrapping using the Shadowing skill in BB2025.
 */
object UseShadowingStep: Procedure() {
    override val initialNode: Node = CheckIfShadowingIsAvailable
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object CheckIfShadowingIsAvailable: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MoveContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MoveContext>()
            // This can happen if a Diving Tackle player went prone here. This will prevent Shadowing from being used.
            val isStartingSquareEmpty = !state.pitch[context.startingSquare].isOccupied()
            val eligiblePlayers = when (isStartingSquareEmpty) {
                true -> {
                    context.startingSquare.getSurroundingCoordinates(rules)
                        .filter { coord ->
                            state.pitch[coord].player
                                ?.let { player -> player.team != context.player.team }
                                ?: false
                        }
                        .mapNotNull { state.pitch[it].player }
                        .filter { it.isSkillAvailable(SkillType.SHADOWING) }
                        .filterNot { it.hasStatusEffect(PlayerStatusEffectType.ROOTED) }
                }
                false -> emptyList()
            }
            return if (eligiblePlayers.isEmpty()) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectPlayer.fromPlayers(eligiblePlayers), CancelWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    val shadowingContext = ShadowingRollContext(player = player)
                    compositeCommandOf(
                        ReportSkillUsed(player, SkillType.SHADOWING),
                        UseShadowingSkill(player),
                        AddContext(shadowingContext),
                        GotoNode(RollShadowingDie),
                    )
                }
                is Cancel,
                is Continue -> {
                    ExitProcedure()
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RollShadowingDie: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ShadowingRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            val context = state.getContext<ShadowingRollContext>()
            return if (context.isSuccess) {
                val isBallInLocation = state.pitch[moveContext.startingSquare].balls.isNotEmpty()
                val nextNode = when (isBallInLocation) {
                    true -> GotoNode(BounceBall)
                    false -> ExitProcedure()
                }
                compositeCommandOf(
                    SetPlayerLocation(context.player, moveContext.startingSquare),
                    RemoveContext<ShadowingRollContext>(),
                    nextNode
                )
            } else {
                compositeCommandOf(
                    RemoveContext<ShadowingRollContext>(),
                    ExitProcedure()
                )
            }
        }
    }

    // A Shadowing Player cannot attempt to pickup the ball (as they are not on the active team),
    // but that logic is inside `Pickup`, so just delegate to that.
    object BounceBall: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val moveContext = state.getContext<MoveContext>()
            val ball = state.pitch[moveContext.startingSquare].balls.singleOrNull() ?: INVALID_GAME_STATE("Too many balls in square: ${moveContext.startingSquare}")
            return SetCurrentBall(ball)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Pickup
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                ExitProcedure(),
            )
        }
    }
}
