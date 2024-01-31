package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetRollContext
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.DiceModifier
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.reports.ReportCatch
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.RerollSource
import dk.ilios.jervis.utils.INVALID_GAME_STATE


enum class CatchModifier(override val modifier: Int, override val description: String): DiceModifier {
    CONVERT_DEFLECTION(-1, "Deflection"),
    BOUNCING(-1, "Bouncing ball"),
    THROW_IN(-1, "Throw-in"),
    SCATTERED(-1, "Scattered"),
    DEVIATED(-1, "Deviated"),
    MARKED(-1, "Marked"),
    DISTURBING_PRESENCE(-1, "Disturbing Presence"),
    POURING_RAIN(-1, "Pouring Rain")
}

data class CatchRollContext(
    val catchingPlayer: Player,
    val diceRollTarget: Int,
    val modifiers: List<DiceModifier>
) {
    // The sum of modifiers
    fun diceModifier(): Int = modifiers.fold(0) { acc: Int, el: DiceModifier ->  acc + el.modifier }
}

data class CatchRollResultContext(
    val catchingPlayer: Player,
    val target: Int,
    val diceRoll: D6Result,
    val modifiers: List<DiceModifier>,
    val rerolled: Boolean,
    val rerolledBy: RerollSource?,
    val success: Boolean
)

/**
 * Resolve a player attempting to catch the ball.
 *
 * This can be used as a placeholder during development or testing.
 */
object Catch: Procedure() {
    override val initialNode: Node = RollToCatch
    override fun isValid(state: Game, rules: Rules) {
        // Check that this is only called on a standing player with tacklezones
        val ballLocation = state.ball.location
        if (state.field[ballLocation].player == null) {
            INVALID_GAME_STATE("No player available to catch the ball at: $ballLocation")
        }
        if (!rules.canCatch(state, state.field[ballLocation].player!!)) {
            INVALID_GAME_STATE("Player is not eligible for catching the ball at: $ballLocation")
        }
    }
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        // Determine target and modifiers for the Catch roll
        val catchingPlayer = state.field[state.ball.location].player!!
        val diceRollTarget = catchingPlayer.agility
        val modifiers = mutableListOf<DiceModifier>()
        // TODO Convert deflection into Intercept
        if (state.ball.state == BallState.BOUNCING) modifiers.add(CatchModifier.BOUNCING)
        if (state.ball.state == BallState.THROW_IN) modifiers.add(CatchModifier.THROW_IN)
        if (state.ball.state == BallState.DEVIATING) modifiers.add(CatchModifier.DEVIATED)
        if (state.ball.state == BallState.SCATTERED) modifiers.add(CatchModifier.SCATTERED)
        // TODO Check for disturbing presence.
        state.ballSquare.coordinates.getSurroundingCoordinates(rules).forEach {
            val markingPlayer: Player? = state.field[it].player
            if (markingPlayer != null) {
                if (markingPlayer.team != catchingPlayer.team && rules.canMark(markingPlayer)) {
                    modifiers.add(CatchModifier.MARKED)
                }
            }
        }
        val rollContext = CatchRollContext(catchingPlayer, diceRollTarget, modifiers)
        return compositeCommandOf(
            SetRollContext(Game::catchRollContext, rollContext)
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollToCatch: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = CatchRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val result = state.catchRollResult!!
            return if (result.success) {
                compositeCommandOf(
                    SetBallState.carried(result.catchingPlayer),
                    ReportCatch(result.catchingPlayer, result.target, result.modifiers, result.diceRoll, true),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    SetBallState.bouncing(),
                    ReportCatch(result.catchingPlayer, result.target, result.modifiers, result.diceRoll, false),
                    GotoNode(CatchFailed)
                )
            }
        }
    }

    object CatchFailed: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure() // TODO Not 100% sure what to do here?
        }
    }
}