package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.reports.ReportKickOffEventRoll
import dk.ilios.jervis.reports.ReportTouchback
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.TableResult

/**
 * Run the Kick-Off Event as well as the results of the ball coming back to the field.
 *
 * See page 41 in the rulebook.
 */
object TheKickOffEvent: Procedure() {
    override val initialNode: Node = RollForKickOffEvent
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollForKickOffEvent: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6, Dice.D6))
        }

        // For High Kick:
        // Following the strict ordering of the rules, the Kick-Off Event is resolved
        // before "What Goes Up, Must Come Down". This means that the touchback rule cannot
        // yet be applied when High Kick is resolved. Also, no-where is it stated that
        // the high kick player cannot enter the opponents field. So in theory it would be
        // allowed to move a player into the opponents field, resolve the ball coming down,
        // which would result in a touchback. And then automatically give it to the player
        // who was moved onto the opponents field.
        //
        // However, this seems against the spirits of the rules and are probably an oversight,
        // So disallowing it for now unless someone can surface an official reference that
        // contradicts this.
        //
        // Another node: If it is just rules that are unclear, and the touchback is awarded as soon as
        // the ball leaves the kicking teams half, then this also impacts things like Blitz,
        // where you
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result, D6Result>(action) { firstD6, secondD6 ->
                val result: TableResult = rules.kickOffEventTable.roll(firstD6, secondD6)
                compositeCommandOf(
                    ReportKickOffEventRoll(firstD6, secondD6, result),
                    GotoNode(ResolveKickOffTableEvent(result.procedure))
                )
            }
        }
    }

    class ResolveKickOffTableEvent(private val eventProcedure: Procedure) : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = eventProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(WhatGoesUpMustComeDown)
        }
    }

    object WhatGoesUpMustComeDown: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // If out-of-bounds, award touch back
            // If on an empty square, bounce
            // if landing on a player, they must/can(?) attempt to catch it
            // TODO Ball lands again, and can either be caught, will bounce or result in a
            //  touchback
            val ballLocation: FieldCoordinate = state.ball.location
            val outOfBounds = ballLocation.isOutOfBounds(rules)
                    || (state.kickingTeam.isHomeTeam() && ballLocation.isOnHomeSide(rules))
                    || (state.kickingTeam.isAwayTeam() && ballLocation.isOnAwaySide(rules))
            return if (outOfBounds) {
                GotoNode(TouchBack)
            } else {
                compositeCommandOf(
                    SetBallState.deviating(),
                    GotoNode(ResolveBallLanding(ballLocation))
                )
            }
        }
    }

    // Move this logic to its own procedure. It will be needed when blocking, throwing and otherwise.
    class ResolveBallLanding(private val location: FieldCoordinate): ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            state.abortIfBallOutOfBounds = true // TODO Wrong way to do this
            return null
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.field[location].player?.let { player: Player ->
                if (player.hasTackleZones && player.state == PlayerState.STANDING) {
                    Catch
                } else {
                    Bounce// Player is not able to catch the ball
                }
            } ?: Bounce // Field is empty
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            state.abortIfBallOutOfBounds = false
            return if (state.ball.state == BallState.OUT_OF_BOUNDS) {
                GotoNode(TouchBack)
            } else {
                ExitProcedure()
            }
        }
    }

    object TouchBack: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return state.receivingTeam.filter {
                it.hasTackleZones && it.state == PlayerState.STANDING && it.location.isOnField(rules)
            }.map {
                SelectPlayer(it)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<PlayerSelected>(action) {
                return compositeCommandOf(
                    SetBallState.carried(it.player),
                    ReportTouchback(it.player),
                    ExitProcedure()
                )
            }
        }
    }
}