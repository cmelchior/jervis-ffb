package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.common.reports.ReportKickResult
import com.jervisffb.engine.rules.Rules

/**
 * FUMBBL deviates from the rulebook in the sense that it doesn't require you to nominate
 * a kicking player. Instead, it just allows you to use Kick (after the roll) if a player
 * with the skill is eligible as a kicking player.
 *
 * Practically, this doesn't make a difference as there is nothing that can remove a kicking
 * player from play between they being nominated and the scatter dice being rolled
 */
object TheFumbblKickOff : Procedure() {
    override val initialNode: Node = PlaceTheKick
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

//    object NominateKickingPlayer: ActionNode() {
//        data class PlayersAvailableForKicking(
//            var onLos: Int = 0,
//            var available: Int = 0,
//            val playersOnLoS: MutableList<Player> = mutableListOf(),
//            val playersAvailable: MutableList<Player> = mutableListOf()
//        )
//
//        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
//            // Nominate a player on the center field that should kick the ball
//            // If all players are on the line of scrimmage or in the wide zone, a player on the
//            // line of scrimmage must be selected.
//            val players = state.kickingTeam.fold(PlayersAvailableForKicking()) { acc, player ->
//                val onLoS = player.location.isOnLineOfScrimmage(rules)
//                val available = !(onLoS || player.location.isInWideZone(rules))
//                if (onLoS) {
//                    acc.onLos += 1
//                    acc.playersOnLoS.add(player)
//                }
//                if (available) {
//                    acc.available += 1
//                    acc.playersAvailable.add(player)
//                }
//                acc.available += if (available) 1 else 0
//                if (onLoS) {
//                    acc.playersOnLoS
//                }
//                acc
//            }
//
//            val eligiblePlayers: List<SelectPlayer> = if (players.available > 0) {
//                players.playersAvailable.map {
//                    SelectPlayer(it)
//                }
//            } else {
//                players.playersOnLoS.map {
//                    SelectPlayer(it)
//                }
//            }
//            if (eligiblePlayers.isEmpty()) {
//                INVALID_GAME_STATE("No player available for kicking")
//            }
//            return eligiblePlayers
//        }
//
//        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
//            return checkType<PlayerSelected>(action) {
//                compositeCommandOf(
//                    SetKickingPlayer(it.player),
//                    ReportKickingPlayer(it.player),
//                    GotoNode(PlaceTheKick)
//                )
//            }
//        }
//    }

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

    object TheKickDeviates : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D8, Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D8Result, D6Result>(action) { d8, d6 ->
                val direction = rules.direction(d8)
                val ball = state.currentBall()
                val newLocation = ball.coordinates.move(direction, d6.value)
                compositeCommandOf(
                    SetBallLocation(ball, newLocation),
                    ReportKickResult(state.kickingTeam, d8, d6, newLocation, rules),
                    ExitProcedure(),
                )
            }
        }
    }
}
