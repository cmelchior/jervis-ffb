package com.jervisffb.engine.rules.bb2020.procedures.actions.move

import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.rules.bb2020.tables.PrayerToNuffle
import com.jervisffb.engine.commands.compositeCommandOf
import kotlinx.serialization.Serializable

data class MovePlayerIntoSquareContext(
    val player: Player,
    val target: FieldCoordinate
) : ProcedureContext

/**
 * Procedure controlling a player entering a square using one of their
 * normal movement options or by being pushed into it.
 *
 * Normally it just means moving the player into that square, but if
 * Treacherous Trapdoors have been rolled on Prayers to Nuffle, it
 * might result in the player being removed from play immediately.
 */
@Serializable
object MovePlayerIntoSquare : Procedure() {
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<MovePlayerIntoSquareContext>()
    }
    override val initialNode: Node = MoveIntoSquare
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<MovePlayerIntoSquareContext>()
    }

    // Move the player into the target square
    object MoveIntoSquare: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MovePlayerIntoSquareContext>()
            return compositeCommandOf(
                SetPlayerLocation(context.player, context.target),
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
                state.field[context.target].balls.count { it.state == BallState.ON_GROUND } > 0
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
            val ball = state.field[context.target].balls.first { it.state == BallState.ON_GROUND }
            return SetCurrentBall(ball)
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
            val hasTrapdoor = state.field[context.target].hasTrapdoor
            val isTreacherous = (
                state.homeTeam.hasPrayer(PrayerToNuffle.TREACHEROUS_TRAPDOOR) ||
                state.awayTeam.hasPrayer(PrayerToNuffle.TREACHEROUS_TRAPDOOR)
            )
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
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<MovePlayerIntoSquareContext>()
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.TREACHEROUS_TRAPDOOR, d6),
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
                SetPlayerLocation(context.player, DogOut),
                SetPlayerState(context.player, PlayerState.KNOCKED_DOWN, hasTackleZones = false),
                SetContext(
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
                if (context.player.hasBall()) {
                    // TODO Should also bounce the ball
                    SetTurnOver(TurnOver.STANDARD)
                } else null,
                ExitProcedure()
            )
        }
    }
}
