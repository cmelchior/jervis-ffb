package dk.ilios.jervis.procedures.actions.move

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.DogOut
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.injury.RiskingInjuryContext
import dk.ilios.jervis.procedures.injury.RiskingInjuryMode
import dk.ilios.jervis.procedures.injury.RiskingInjuryRoll
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
 * Procedure controlling a player entering a square.
 * Normally it just means moving the player into that square, but if
 * Treacherous Trapdoors have been rolled on Prayers to Nuffle, it
 * might result in the player being removed from play immediately.
 */
@Serializable
object MovePlayerIntoSquare : Procedure() {
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<MovePlayerIntoSquareContext>()
    }
    override val initialNode: Node = RollForTrapdoorIfNeeded
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<MovePlayerIntoSquareContext>()
    }

    object RollForTrapdoorIfNeeded : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<MovePlayerIntoSquareContext>()
            val hasTrapdoor = state.field[context.target].hasTrapdoor
            val isTreacherous = state.homeTeam.hasPrayer(PrayerToNuffle.TREACHEROUS_TRAPDOOR) ||
                state.awayTeam.hasPrayer(PrayerToNuffle.TREACHEROUS_TRAPDOOR)
            return if (hasTrapdoor && isTreacherous) {
                listOf(RollDice(Dice.D6))
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MovePlayerIntoSquareContext>()
            return when (action) {
                is Continue -> {
                    compositeCommandOf(
                        SetPlayerLocation(context.player, context.target),
                        ExitProcedure()
                    )
                }
                else -> {
                    checkDiceRoll<D6Result>(action) { d6 ->
                        compositeCommandOf(
                            SetPlayerLocation(context.player, context.target),
                            ReportDiceRoll(DiceRollType.TREACHEROUS_TRAPDOOR, d6),
                            if (d6.value != 1) ReportGameProgress("${context.player.name} narrowly avoided the trapdoor") else null,
                            if (d6.value == 1) GotoNode(ResolveFallingThroughTrapdoor) else ExitProcedure()
                        )
                    }
                }
            }
        }
    }

    object ResolveFallingThroughTrapdoor : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MovePlayerIntoSquareContext>()
            return compositeCommandOf(
                SetPlayerLocation(context.player, DogOut),
                SetPlayerState(context.player, PlayerState.KNOCKED_DOWN),
                SetContext(RiskingInjuryContext(
                    player = context.player,
                    mode = RiskingInjuryMode.PUSHED_INTO_CROWD
                )),
                ReportGameProgress("${context.player.name} fell through a trapdoor at ${context.target.toLogString()}")
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MovePlayerIntoSquareContext>()
            return compositeCommandOf(
                if (context.player.hasBall()) {
                    // TODO Should also bounce the ball
                    SetTurnOver(true)
                } else null,
                ExitProcedure()
            )
        }
    }
}
