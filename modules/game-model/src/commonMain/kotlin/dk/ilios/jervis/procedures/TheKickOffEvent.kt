package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportKickOffEventRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.TableResult

/**
 * Run the Kick-Off Event as well as the results of the ball coming back to the field.
 *
 * - See page 41 in the rulebook
 */
object TheKickOffEvent: Procedure() {
    override val initialNode: Node = ResolveKickOffEvent
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object ResolveKickOffEvent: ActionNode() {
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
        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
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

    object WhatGoesUpMustComeDown: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(ContinueWhenReady)
        }

        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
            return compositeCommandOf(
                GotoNode(TouchBacks)
            )
        }
    }

    object TouchBacks: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }

    object GiveBallToPlayer: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            TODO("Not yet implemented")
        }

        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }

    }
}