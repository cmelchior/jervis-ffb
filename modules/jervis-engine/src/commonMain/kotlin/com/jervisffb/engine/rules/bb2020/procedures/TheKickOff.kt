package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetKickingPlayer
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportKickResult
import com.jervisffb.engine.reports.ReportKickingPlayer
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Do the Kick-Off.
 *
 * - See page 40 in the rulebook
 * - See Designer's Commentary - May 2023, page 2.
 */
object TheKickOff : Procedure() {
    override val initialNode: Node = NominateKickingPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object NominateKickingPlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam

        data class PlayersAvailableForKicking(
            var onLos: Int = 0,
            var available: Int = 0,
            val playersOnLoS: MutableList<Player> = mutableListOf(),
            val playersAvailable: MutableList<Player> = mutableListOf(),
        )

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Nominate a player on the center field that should kick the ball
            // If all players are on the line of scrimmage or in the wide zone, a player on the
            // line of scrimmage must be selected.
            val players =
                state.kickingTeam.fold(PlayersAvailableForKicking()) { acc, player ->
                    val onLoS = player.location.isOnLineOfScrimmage(rules)
                    val available = !(onLoS || player.location.isInWideZone(rules))
                    if (onLoS) {
                        acc.onLos += 1
                        acc.playersOnLoS.add(player)
                    }
                    if (available) {
                        acc.available += 1
                        acc.playersAvailable.add(player)
                    }
                    acc.available += if (available) 1 else 0
                    if (onLoS) {
                        acc.playersOnLoS
                    }
                    acc
                }

            val eligiblePlayers: List<PlayerId> =
                if (players.available > 0) {
                    players.playersAvailable.map { it.id }
                } else {
                    players.playersOnLoS.map { it.id }
                }
            if (eligiblePlayers.isEmpty()) {
                INVALID_GAME_STATE("No player available for kicking")
            }
            return listOf(SelectPlayer(eligiblePlayers))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<PlayerSelected>(action) {
                compositeCommandOf(
                    SetKickingPlayer(it.getPlayer(state)),
                    ReportKickingPlayer(it.getPlayer(state)),
                    GotoNode(PlaceTheKick),
                )
            }
        }
    }

    object PlaceTheKick : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Place the ball anywhere on the opposing teams side
            return state.field
                .filter { rules.canPlaceBallForKickoff(state.kickingTeam, it) }
                .map { TargetSquare.kick(it.coordinates) }
                .let { listOf(SelectFieldLocation(it)) }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<FieldSquareSelected>(action) {
                val ball = state.balls.single()
                compositeCommandOf(
                    SetBallState.inAir(ball),
                    SetBallLocation(ball, FieldCoordinate(it.x, it.y)),
                    GotoNode(TheKickDeviates),
                )
            }
        }
    }

    object TheKickDeviates : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return SetContext(DeviateRollContext(from = state.currentBall().location))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = DeviateRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<DeviateRollContext>()
            val newLocation = context.landsAt ?: FieldCoordinate.OUT_OF_BOUNDS
            val ball = state.currentBall()
            return compositeCommandOf(
                if (context.outOfBoundsAt != null) SetBallState.outOfBounds(ball, context.outOfBoundsAt) else SetBallState.deviating(ball),
                SetBallLocation(ball, newLocation),
                ReportKickResult(state.kickingTeam, context.deviateRoll.first() as D8Result, context.deviateRoll.last() as D6Result, newLocation, rules),
                ExitProcedure(),
            )
        }
    }
}

/**
 * FUMBBL deviates from the rulebook in the sense that it doesn't require you to nominate
 * a kicking player. Instead, it just allows you to use Kick (after the roll) if a player
 * with the skill is eligible as a kicking player.
 *
 * Practically, this doesn't make a difference as there is nothing that can remove a kicking
 * player from play between they being nominated and the scatter dice being rolled
 */
object TheFUMBBLKickOff : Procedure() {
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
            return state.field
                .filter { rules.canPlaceBallForKickoff(state.kickingTeam, it) }
                .map { TargetSquare.kick(it.coordinates) }
                .let { listOf(SelectFieldLocation(it)) }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<FieldSquareSelected>(action) {
                val ball = state.balls.single()
                compositeCommandOf(
                    SetBallState.inAir(ball),
                    SetBallLocation(ball, FieldCoordinate(it.x, it.y)),
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
            return checkDiceRoll<D8Result, D6Result>(action) { d8, d6 ->
                val direction = rules.direction(d8)
                val ball = state.currentBall()
                val newLocation = ball.location.move(direction, d6.value)
                compositeCommandOf(
                    SetBallLocation(ball, newLocation),
                    ReportKickResult(state.kickingTeam, d8, d6, newLocation, rules),
                    ExitProcedure(),
                )
            }
        }
    }
}
