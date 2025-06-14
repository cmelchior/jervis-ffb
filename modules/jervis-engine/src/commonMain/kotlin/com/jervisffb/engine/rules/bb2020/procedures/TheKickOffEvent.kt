package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportTouchback
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.TableResult
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class KickOffEventContext(
    val roll: DiceRollResults,
    val result: TableResult,
    val scatterBallBeforeLanding: Boolean = false // If Changing Weather rolled Perfect Conditions
): ProcedureContext

/**
 * Run the Kick-Off Event as well as the results of the ball coming back to the field.
 *
 * See page 41 in the rulebook.
 */
object TheKickOffEvent : Procedure() {
    override val initialNode: Node = RollForKickOffEvent
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        state.abortIfBallOutOfBounds = false
        return null
    }

    object RollForKickOffEvent : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6, Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result, D6Result>(action) { firstD6, secondD6 ->
                val result: TableResult = rules.kickOffEventTable.roll(firstD6, secondD6)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.KICK_OFF_TABLE, listOf(firstD6, secondD6)),
                    SetContext(KickOffEventContext(roll = DiceRollResults(firstD6, secondD6), result = result)),
                    GotoNode(ResolveKickOffTableEvent),
                )
            }
        }
    }

    object ResolveKickOffTableEvent : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.getContext<KickOffEventContext>().result.procedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<KickOffEventContext>()
            return if (context.scatterBallBeforeLanding) {
                compositeCommandOf(
                    SetBallState.scattered(state.singleBall()),
                    RemoveContext<KickOffEventContext>(),
                    GotoNode(ScatterBallBeforeLanding)
                )
            } else {
                compositeCommandOf(
                    RemoveContext<KickOffEventContext>(),
                    GotoNode(WhatGoesUpMustComeDown)
                )
            }
        }
    }

    /**
     * The ball scatters further while high in the air, before coming down.
     * Should only happen if Changing Weather (on the Kick-off Event Table) rolled Perfect
     * Conditions.
     */
    object ScatterBallBeforeLanding : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.singleBall()
            return SetContext(
                ScatterRollContext(
                    ball = ball,
                    from = ball.location,
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScatterRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ScatterRollContext>(),
                GotoNode(WhatGoesUpMustComeDown)
            )
        }
    }

    /**
     * Resolve the "What goes up, must come down" step.
     * See page 41 in the rulebook.
     */
    object WhatGoesUpMustComeDown : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // If out-of-bounds, award touch back
            // If on an empty square, bounce
            // if landing on a player, they must/can(?) attempt to catch it
            val ball = state.singleBall()
            val ballLocation: FieldCoordinate = ball.location

            // According to Designer's Errata May 2024, it is only a touchback if a ball
            // goes beyond the kicking teams Line of Scrimmage. This rule also generalizes
            // to Standard board setups. In particular, the ball is allowed to land in any
            // configured No Man's Land.
            state.abortIfBallOutOfBounds = true // TODO Is there a better way to handle this?
            val outOfBounds =
                !ball.location.isOnField(rules)
                    || (state.kickingTeam.isHomeTeam() && ballLocation.x <= rules.lineOfScrimmageHome)
                    || (state.kickingTeam.isAwayTeam() && ballLocation.x >= rules.lineOfScrimmageAway)
            return if (outOfBounds) {
                GotoNode(TouchBack)
            } else {
                GotoNode(ResolveBallLanding)
            }
        }
    }

    // Move this logic to its own procedure. It will be needed when blocking, throwing and otherwise.
    object ResolveBallLanding : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val ballLocation = state.singleBall().location
            val canCatch = state.field[ballLocation].player?.let { rules.canCatch(state, it) } ?: false
            return if (!canCatch) {
                SetBallState.bouncing(state.singleBall())
            } else {
                null
            }
        }

        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return if (state.singleBall().state != BallState.BOUNCING) Catch else Bounce
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            return if (state.singleBall().state == BallState.OUT_OF_BOUNDS) {
                GotoNode(TouchBack)
            } else {
                ExitProcedure()
            }
        }
    }

    object TouchBack : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Prone / Stunned players can only be selected if no standing players are available.
            // In that case, it will bounce from their square.
            val standingPlayers = mutableListOf<Player>()
            val pronePlayers = mutableListOf<Player>()
            state.receivingTeam.forEach {
                if (it.location.isOnField(rules)) {
                    when (it.state) {
                        PlayerState.PRONE, PlayerState.STUNNED -> {
                            pronePlayers.add(it)
                        }
                        PlayerState.STANDING -> {
                            standingPlayers.add(it)
                        }
                        else -> INVALID_GAME_STATE("Unsupported state: ${it.state}")
                    }
                }
            }
            return if (standingPlayers.isEmpty()) {
                listOf(SelectPlayer.fromPlayers(pronePlayers))
            } else {
                listOf(SelectPlayer.fromPlayers(standingPlayers))
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<PlayerSelected>(action) {
                val player = it.getPlayer(state)
                if (player.state == PlayerState.STANDING) {
                    return compositeCommandOf(
                        SetBallState.carried(state.singleBall(), player),
                        ReportTouchback(player, pronePlayer = false),
                        ExitProcedure(),
                    )
                } else {
                    compositeCommandOf(
                        // TODO Giant Support
                        SetBallState.carried(state.singleBall(), player),
                        SetBallLocation(state.singleBall(), player.coordinates),
                        SetBallState.bouncing(state.singleBall()),
                        ReportTouchback(player, pronePlayer = false),
                        GotoNode(BounceFromPronePlayer)
                    )
                }
            }
        }
    }

    object BounceFromPronePlayer: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            return super.onEnterNode(state, rules)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If ball went out of bounds, another touchback is awarded.
            // "out-of-bounds" also covers crossing back over the LoS
            return if (state.singleBall().state == BallState.OUT_OF_BOUNDS) {
                GotoNode(TouchBack)
            } else {
                compositeCommandOf(
                    ExitProcedure(),
                )
            }
        }
    }
}
