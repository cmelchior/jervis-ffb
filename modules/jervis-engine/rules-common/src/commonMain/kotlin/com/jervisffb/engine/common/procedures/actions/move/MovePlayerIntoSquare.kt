package com.jervisffb.engine.common.procedures.actions.move

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.commands.SetCurrentBall
import com.jervisffb.engine.common.commands.SetPlayerIntermediateState
import com.jervisffb.engine.common.commands.SetTurnOver
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.procedures.Bounce
import com.jervisffb.engine.common.procedures.dicerolls.createFinalAtLeastObservation
import com.jervisffb.engine.common.procedures.getResetChompedStateCommands
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.common.procedures.tables.prayers.TreacherousTrapdoor
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.common.reports.ReportGameProgress
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerIntermediateState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.MovePlayerIntoSquareContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler


/**
 * Procedure controlling a player entering a square using one of their
 * normal movement options or landing there after being thrown.
 *
 * Normally it just means moving the player into that square, but if
 * Treacherous Trapdoors have been rolled on Prayers to Nuffle, it
 * might result in the player being removed from play immediately.
 *
 * This procedure should not be called until after all rolls for entering the
 * square have been resolved, i.e., Tentacles, Rush, Dodge, Jump, Leap, and
 * Landing. This is covered under Picking Up The Ball on page 46 in the rulebook.
 *
 * This procedure does NOT check for touchdowns nor pickups. That is left up
 * to the parent procedure.
 *
 * NOTE: Pushbacks roughly follow the same logic but with different timings,
 * so moving players into squares during a push is handled in those procedures.
 *
 * NOTE: This procedure only supports a player landing successfully after a
 * throw. Falling Over after a throw is handled in XXX.
 *
 * TODO This logic here is wrong and needs to be reworked. See rule-discussions.md
 * TODO Not sure the above is still relevant
 */
object MovePlayerIntoSquare : Procedure(), ChanceObservationHandler {
    override val initialNode: Node = MoveIntoSquare
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<MovePlayerIntoSquareContext>()
    }

    // Move the player into the target square
    object MoveIntoSquare: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MovePlayerIntoSquareContext>()
            return compositeCommandOf(
                SetPlayerLocation(context.player, context.target, isThrown = false),
                getResetChompedStateCommands(context.player, context.target),
                GotoNode(CheckForBouncingBall),
            )
        }
    }

    // If the player was already holding a ball and moves into a square with a Ball Clone,
    // the ball on the ground will bounce before anything else happens.
    object CheckForBouncingBall: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MovePlayerIntoSquareContext>()
            val playerIsHoldingBall = (context.player.ball?.carriedBy == context.player)
            val ballOnTheGround = (
                state.balls.size > 1 &&
                    state.pitch[context.target].balls.count { it.state == BallState.ON_GROUND } > 0
            )
            return if (playerIsHoldingBall && ballOnTheGround) {
                GotoNode(ResolveBouncingBall)
            } else {
                GotoNode(CheckForTrapdoor)
            }
        }

    }

    object ResolveBouncingBall: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MovePlayerIntoSquareContext>()
            val ball = state.pitch[context.target].balls.first { it.state == BallState.ON_GROUND }
            return compositeCommandOf(
                SetBallState.bouncing(ball),
                SetCurrentBall(ball)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                GotoNode(CheckForTrapdoor)
            )
        }
    }

    object CheckForTrapdoor: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MovePlayerIntoSquareContext>()
            val hasTrapdoor = state.pitch[context.target].hasTrapdoor
            val isTreacherous = state.homeTeam.activePrayersToNuffle
                .plus(state.awayTeam.activePrayersToNuffle)
                .any {
                    // Work-around for `common` not knowing version-specific prayer results.
                    it.procedure == TreacherousTrapdoor
                }
            return if (hasTrapdoor && isTreacherous) {
                GotoNode(RollForTrapdoor)
            } else {
                ExitProcedure()
            }
        }
    }

    object RollForTrapdoor: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MovePlayerIntoSquareContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6, type = DiceRollType.TREACHEROUS_TRAPDOOR))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<MovePlayerIntoSquareContext>()
                val chanceObservation = createFinalAtLeastObservation(
                    state = state,
                    player = context.player,
                    rollType = DiceRollType.TREACHEROUS_TRAPDOOR,
                    die = d6,
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.TREACHEROUS_TRAPDOOR, d6),
                    chanceObservation?.let(::AddChanceObservation),
                    if (d6.value != 1) ReportGameProgress("${context.player.name} narrowly avoided the trapdoor") else null,
                    if (d6.value == 1) GotoNode(ResolveFallingThroughTrapdoor) else ExitProcedure()
                )
            }
        }

    }

    object ResolveFallingThroughTrapdoor : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MovePlayerIntoSquareContext>()
            return compositeCommandOf(
                SetPlayerLocation(context.player, Dogout),
                SetPlayerIntermediateState(context.player, PlayerIntermediateState.KNOCKED_DOWN),
                AddContext(
                    RiskingInjuryContext(
                        player = context.player,
                        mode = RiskingInjuryMode.PUSHED_INTO_CROWD
                    )
                ),
                ReportGameProgress("${context.player.name} fell through a trapdoor at ${context.target.toLogString()}")
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MovePlayerIntoSquareContext>()
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                if (context.player.hasBall()) {
                    // TODO Should also bounce the ball
                    SetTurnOver(TurnOver.STANDARD)
                } else null,
                ExitProcedure()
            )
        }
    }
}
