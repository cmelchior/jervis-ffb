package dk.ilios.jervis.procedures.actions.move

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetCurrentBall
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.TurnOver
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.locations.DogOut
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.procedures.Bounce
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryContext
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryMode
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryRoll
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.tables.PrayerToNuffle
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
