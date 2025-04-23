package com.jervisffb.engine.rules.bb2020.procedures.actions.move

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.AddGoal
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.reports.ReportGoal
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.BloodLust
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

data class ScoringATouchDownContext(
    val player: Player,
    val isTouchdownScored: Boolean = false
): ProcedureContext

/**
 * Procedure responsible for checking if a touchdown was scored as per page 64
 * in the rulebook. This procedure should be called every time a player with the
 * ball moves or a player receives a ball (and doesn't fall over).
 *
 * Moving into the End Zone would normally result in an immediate touchdown, but
 * some things can impact it:
 *
 * - Ball Clone: The ball disappears between your hands
 * - Blood Lust: Need to bite a thrall for turnover to count
 *
 * For Ball Clone, we are using the following semantics:
 * - We roll for Ball Clone before any other effect
 * - Pro is not allowed
 * - Team rerolls are not allowed
 * - If the roll fails and the ball disappeared, we let the player continue
 *   their turn as is nothing has happened
 *
 * The reason for this is this phrase "A touchdown is scored.....No touchdown
 *  * is scored". But the exact timing is under-documented, so a valid argument
 *  could probably be made that the player's turn ends as well. So for now,
 *  the choice is somewhat arbitrary.
 *
 * If Ball Clone succeeds, other effects will be taken into account, like
 * Blood Lust.
 */
@Serializable
object ScoringATouchdown : Procedure() {
    override val initialNode: Node = CheckForTouchdown
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<ScoringATouchDownContext>()
        return if (context.isTouchdownScored) {
            val turnover = if (context.player.team == state.activeTeam) {
                TurnOver.ACTIVE_TEAM_TOUCHDOWN
            } else {
                TurnOver.INACTIVE_TEAM_TOUCHDOWN
            }
            compositeCommandOf(
                AddGoal(context.player.team, 1),
                ReportGoal(state, context),
                SetTurnOver(turnover),
            )
        } else {
            RemoveContext<ScoringATouchDownContext>()
        }

    }
    override fun isValid(state: Game, rules: Rules) {
        val player = state.getContext<ScoringATouchDownContext>().player
        if (player.hasBall() && player.state == PlayerState.STANDING) {
            INVALID_GAME_STATE("Player is in invalid state: $player")
        }
    }

    object CheckForTouchdown: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ScoringATouchDownContext>()
            val player = context.player
            val isInEndZone = player.location.isInEndZone(rules)
            val isOnOpponentSide = if (player.isOnHomeTeam()) {
                player.location.isOnAwaySide(rules)
            } else {
                player.location.isOnHomeSide(rules)
            }
            return if (isInEndZone && isOnOpponentSide && player.hasBall()) {
                compositeCommandOf(
                    SetContext(context.copy(isTouchdownScored = true)),
                    GotoNode(RollForBallClone)
                )
            } else {
                ExitProcedure()
            }
        }
    }

    object RollForBallClone: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = state.getContext<ScoringATouchDownContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(ContinueWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<ScoringATouchDownContext>()
            return when (action) {
                Continue -> {
                    if (context.player.hasSkill<BloodLust>()) GotoNode(CheckBloodLust) else ExitProcedure()
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object CheckBloodLust: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            TODO("Not yet implemented")
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }
}
