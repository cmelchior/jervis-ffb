package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
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
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.KickOffEventContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.BB2020TheKickOffEvent
import com.jervisffb.engine.rules.bb2025.procedures.BB2025TheKickOffEvent
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.tables.TableResult

/**
 * Run the Kick-Off Event as well as the results of the ball coming back to the
 * field.
 *
 * See page 41 in the BB2020 rulebook.
 * See page 47 in the BB2025 rulebook.
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
            return castDiceRoll<D6Result, D6Result>(action) { firstD6, secondD6 ->
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
                    GotoNode(WhatGoesUp)
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
                    from = ball.location,
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScatterRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ScatterRollContext>(),
                GotoNode(WhatGoesUp)
            )
        }
    }

    /**
     * Resolve the "What goes up, must come down" (BB2020) or "What goes up..."
     * (BB2025) step.
     */
    object WhatGoesUp : ComputationNode() {
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
                GotoNode(SelectTouchBack)
            } else {
                GotoNode(ResolveBallLanding)
            }
        }
    }

    object ResolveBallLanding : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val ballLocation = state.singleBall().location
            val canCatch = state.field[ballLocation].player?.let { rules.canCatch(it) } ?: false
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
                GotoNode(SelectTouchBack)
            } else {
                ExitProcedure()
            }
        }
    }

    // BB2020 and BB2025 version differs slightly for the touchback, so delegate to the
    // appropriate version.
    object SelectTouchBack: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return when (rules.baseVersion) {
                GameVersion.BB2020 -> GotoNode(BB2020TheKickOffEvent.TouchBack)
                GameVersion.BB2025 -> GotoNode(BB2025TheKickOffEvent.TouchBack)
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
                GotoNode(SelectTouchBack)
            } else {
                compositeCommandOf(
                    ExitProcedure(),
                )
            }
        }
    }
}
