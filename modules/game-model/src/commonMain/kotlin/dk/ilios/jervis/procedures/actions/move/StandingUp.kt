package dk.ilios.jervis.procedures.actions.move

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetPlayerMoveLeft
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Procedure for handling a prone player standing up as part of a Move, Blitz, Pass, Hand-Off or Foul action.
 * See page 44 in the rulebook.
 *
 * Moving normally are handled in [MoveTypeSelectorStep]
 * Jumping are handled in [JumpStep]
 */
object StandingUp : Procedure() {
    override val initialNode: Node = AttemptToStandUpAutomatically

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? {
        if (state.activePlayer?.state != PlayerState.PRONE) {
            INVALID_GAME_STATE("Player must be prone: ${state.activePlayer?.name}")
        }
        return null
    }

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    // If Player has 3+ movement, they stand up automatically,
    // otherwise they need to roll for it
    object AttemptToStandUpAutomatically : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val movingPlayer = state.activePlayer!!
            return if (movingPlayer.movesLeft >= 3) {
                compositeCommandOf(
                    SetPlayerState(movingPlayer, PlayerState.STANDING),
                    SetPlayerMoveLeft(movingPlayer, movingPlayer.movesLeft - 3),
                    ExitProcedure()
                )
            } else {
                GotoNode(RollForStandingUp)
            }
        }
    }

    object RollForStandingUp : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            TODO("Not yet implemented")
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }
}
