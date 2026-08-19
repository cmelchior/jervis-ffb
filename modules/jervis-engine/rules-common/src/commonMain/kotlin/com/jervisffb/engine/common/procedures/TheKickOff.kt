package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.SetKickingPlayer
import com.jervisffb.engine.common.context.DeviateRollContext
import com.jervisffb.engine.common.reports.ReportKickResult
import com.jervisffb.engine.common.reports.ReportKickSkillResult
import com.jervisffb.engine.common.reports.ReportKickingPlayer
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Do the Kick-Off.
 *
 * - See page 40 in the BB2020 rulebook.
 * - See Designer's Commentary - May 2023, page 2.
 * - See page 47 in the BB2025 rulebook.
 */
object TheKickOff : Procedure() {
    override val initialNode: Node = NominateKickingPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object NominateKickingPlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam

        // Used as accumulator in `fold`
        data class PlayersAvailableForKicking(
            var onLos: Int = 0,
            var inCenterField: Int = 0,
            val playersOnLoS: MutableList<Player> = mutableListOf(),
            val playersInCenterField: MutableList<Player> = mutableListOf(),
        )

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Nominate a player on the center field that should kick the ball
            // If all players are on the line of scrimmage or in the wide zone, a player on the
            // center line of scrimmage must be selected.
            val players =
                state.kickingTeam.fold(PlayersAvailableForKicking()) { acc, player ->
                    val inDogout = !player.location.isOnPitch(rules)
                    val onLoS = player.location.isOnLineOfScrimmage(rules)
                    val inWideZone = player.location.isInWideZone(rules)
                    val available = !(onLoS || inWideZone || inDogout)
                    if (onLoS && !inWideZone) {
                        acc.onLos += 1
                        acc.playersOnLoS.add(player)
                    }
                    if (available && !inDogout) {
                        acc.inCenterField += 1
                        acc.playersInCenterField.add(player)
                    }
                    acc.inCenterField += if (available) 1 else 0
                    if (onLoS) {
                        acc.playersOnLoS
                    }
                    acc
                }

            val eligiblePlayers: List<Player> = if (players.inCenterField > 0) {
                players.playersInCenterField
            } else {
                players.playersOnLoS
            }
            return if (eligiblePlayers.isEmpty()) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectPlayer.fromPlayers(eligiblePlayers))
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> {
                    compositeCommandOf(
                        SetKickingPlayer(null),
                        ReportKickingPlayer(null),
                        GotoNode(PlaceTheKick),
                    )
                }
                is PlayerSelected -> {
                    compositeCommandOf(
                        SetKickingPlayer(action.getPlayer(state)),
                        ReportKickingPlayer(action.getPlayer(state)),
                        GotoNode(PlaceTheKick),
                    )
                }
                else -> INVALID_GAME_STATE("Unsupported action: $action")
            }
        }
    }

    object PlaceTheKick : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Place the ball anywhere on the opposing teams side
            return state.pitch
                .filter { rules.canPlaceBallForKickoff(state.kickingTeam, it) }
                .map { TargetSquare.kick(it.coordinates) }
                .let { listOf(SelectPitchLocation(it)) }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<PitchSquareSelected>(action) {
                val ball = state.balls.single()
                compositeCommandOf(
                    SetBallState.inAir(ball),
                    SetBallLocation(ball, PitchCoordinate(it.x, it.y)),
                    GotoNode(TheKickDeviates),
                )
            }
        }
    }

    object TheKickDeviates : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return AddContext(DeviateRollContext(from = state.currentBall().coordinates))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.kickOffDeviateRollStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ChooseToUseKick)
        }
    }

    object ChooseToUseKick: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return when (state.kickingPlayer?.isSkillAvailable(SkillType.KICK) == true) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false ->listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val useKick = (action is Confirm)
            val context = state.getContext<DeviateRollContext>()
            val d8 = context.deviateRoll.first() as D8Result
            val d6 = context.minD6
            if (!useKick) {
                val newLocation = context.landsAt ?: error("Missing landing coordinate: $context")
                val ball = state.currentBall()
                return compositeCommandOf(
                    RemoveContext<DeviateRollContext>(),
                    if (context.outOfBoundsAt != null) SetBallState.outOfBounds(ball, context.outOfBoundsAt) else SetBallState.deviating(ball),
                    SetBallLocation(ball, newLocation),
                    ReportKickResult(state.kickingTeam, d8, d6, newLocation, rules),
                    ExitProcedure(),
                )
            } else {
                // Move the ball one at a time and check for out of bounds at every move
                val ball = state.currentBall()
                var newLocation = context.from
                var outOfBoundsAt: PitchCoordinate? = null
                val direction = rules.direction(d8)
                val distance = d6.toD3().value
                for (i in 1..distance) {
                    val start = newLocation
                    newLocation = newLocation.move(direction, 1)
                    if (newLocation.isOutOfBounds(rules)) {
                        outOfBoundsAt = start
                        break
                    }
                }
                return compositeCommandOf(
                    RemoveContext<DeviateRollContext>(),
                    ReportKickSkillResult(state.kickingPlayer!!, d6, d6.toD3()),
                    if (outOfBoundsAt != null) SetBallState.outOfBounds(ball, outOfBoundsAt) else SetBallState.deviating(ball),
                    SetBallLocation(ball, newLocation),
                    ReportKickResult(state.kickingTeam, d8, d6, newLocation, rules),
                    ExitProcedure(),
                )
            }
        }
    }
}
