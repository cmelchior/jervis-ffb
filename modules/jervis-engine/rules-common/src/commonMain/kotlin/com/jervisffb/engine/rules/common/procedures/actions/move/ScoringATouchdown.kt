package com.jervisffb.engine.rules.common.procedures.actions.move

import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.AddTouchdown
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ScoringATouchDownContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.reports.ReportTouchdown
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure responsible for checking if a touchdown was scored as per page 64
 * in the rulebook. This procedure should be called every time a player with the
 * ball moves, or a player receives a ball (and doesn't fall over).
 *
 * It is up to the caller of this procedure to delete [ScoringATouchDownContext].
 *
 * Moving into the End Zone would normally result in an immediate touchdown, but
 * some things can impact it:
 *
 * - Ball Clone: The ball disappears between your hands.
 * - Blood Lust: Need to bite a thrall for turnover to count
 * - Touchdown already happened: During a push, multiple players might be end up
 *   in a scoring position. In that case, we only treat the first player as
 *   having scored and ignore the rest.
 *
 * For Ball Clone, we are using the following semantics:
 * - We roll for Ball Clone before any other effect.
 * - Pro is not allowed.
 * - Team rerolls are not allowed.
 * - If the roll fails and the ball disappeared, we let the player continue
 *   their turn as if nothing has happened.
 *
 * The reason for this is this phrase "A touchdown is scored.....No touchdown
 *  is scored". But the exact timing is under-documented, so a valid argument
 *  could be made that the player's turn ends as well. So for now, the choice is
 *  somewhat arbitrary.
 *
 * If Ball Clone succeeds, other effects will be taken into account, like
 * Blood Lust.
 */
object ScoringATouchdown : Procedure() {
    override val initialNode: Node = CheckForTouchdown
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val player = state.getContext<ScoringATouchDownContext>().player
        if (!player.hasBall() || player.state != PlayerPitchState.STANDING) {
            INVALID_GAME_STATE("Player needs to have the ball and be standing: $player")
        }
    }

    object CheckForTouchdown: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // In some cases (like when resolving push chains), it is possible to
            // score multiple touchdowns (as multiple balls exists). In that case,
            // we ignore any other cases that could have been a touchdown
            val context = state.getContext<ScoringATouchDownContext>()
            val player = context.player
            val isInEndZone = player.location.isInEndZone(rules)
            val isOnOpponentSide = if (player.isOnHomeTeam()) {
                player.location.isOnAwaySide(rules)
            } else {
                player.location.isOnHomeSide(rules)
            }
            val touchdownAlreadyHappened = state.turnOver == TurnOver.ACTIVE_TEAM_TOUCHDOWN || state.turnOver == TurnOver.INACTIVE_TEAM_TOUCHDOWN
            return if (isInEndZone && isOnOpponentSide && player.hasBall() && !touchdownAlreadyHappened) {
                compositeCommandOf(
                    UpdateContext(context.copy(isTouchdownScored = true)),
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
                    GotoNode(UpdateScore)
                    // if (context.player.hasSkill(SkillType.BLOOD_LUST)) GotoNode(CheckBloodLust) else ExitProcedure()
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

    // We update the score before triggering the "Confirm" action. This is
    // mostly to improve the UI experience, so the Touchdown Counter increases
    // at the top of the screen increases as soon as the touchdown is scored
    // and is visible while any touchdown animations are running.
    object UpdateScore: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ScoringATouchDownContext>()
            if (!context.isTouchdownScored) INVALID_GAME_STATE("Touchdown was not scored: $context")
            val turnover = if (context.player.team == state.activeTeamOrThrow()) {
                TurnOver.ACTIVE_TEAM_TOUCHDOWN
            } else {
                TurnOver.INACTIVE_TEAM_TOUCHDOWN
            }
            return compositeCommandOf(
                // Technically, if you score during the opponent's turn, the score isn't
                // increased until your next real turn, but this has some problematic
                // side effects for the end of the half. So we do it immediately instead.
                // See rules-faq.md (page 64) for a discussion on this.
                AddTouchdown(context.player.team, 1),
                ReportTouchdown(state, context),
                SetTurnOver(turnover),
                GotoNode(InformOfTouchdown)
            )
        }

    }

    // Mostly relevant to give the UI a hook to show "Touchdown" messages
    object InformOfTouchdown : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ScoringATouchDownContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(ConfirmWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
