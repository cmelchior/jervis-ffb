package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportActionEnded
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION
import kotlinx.serialization.Serializable

/**
 * Procedure for controlling a player's Block action.
 *
 * See page 56 in the rulebook.
 */
@Serializable
object BlockAction : Procedure() {
    override val initialNode: Node = SelectDefenderOrEndAction

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command {
        return ReportActionEnded(state.activePlayer!!, state.activePlayerAction!!)
    }

    object SelectDefenderOrEndAction : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            val end: List<ActionDescriptor> = listOf(EndActionWhenReady)

            val attacker = state.activePlayer!!
            val eligibleDefenders: List<ActionDescriptor> =
                attacker.location.coordinate.getSurroundingCoordinates(rules)
                    .filter { state.field[it].isOccupied() }
                    .filter { state.field[it].player!!.team != attacker.team }
                    .map { SelectPlayer(state.field[it].player!!) }

            return end + eligibleDefenders
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return when (action) {
                EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    val context =
                        BlockContext(
                            attacker = state.activePlayer!!,
                            defender = action.player,
                            isBlitzing = false
                        )
                    compositeCommandOf(
                        SetOldContext(Game::blockContext, context),
                        GotoNode(ResolveBlock),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveBlock : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = BlockStep

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            // Regardless of the outcome of the block, the action is over
            return ExitProcedure()
        }
    }
}
