package dk.ilios.jervis.procedures.actions.move

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.AddGoal
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.TurnOver
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.model.isOnHomeTeam
import dk.ilios.jervis.reports.ReportGoal
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.BloodLust
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
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
            val isOnOpponentSize = if (player.isOnHomeTeam()) {
                player.location.isOnAwaySide(rules)
            } else {
                player.location.isOnHomeSide(rules)
            }
            return if (isInEndZone && isOnOpponentSize && player.hasBall()) {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
