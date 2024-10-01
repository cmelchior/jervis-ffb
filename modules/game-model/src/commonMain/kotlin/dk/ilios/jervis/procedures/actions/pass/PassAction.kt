package dk.ilios.jervis.procedures.actions.pass

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetCurrentBall
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.TurnOver
import dk.ilios.jervis.model.context.MoveContext
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.procedures.ActivatePlayerContext
import dk.ilios.jervis.procedures.D6DieRoll
import dk.ilios.jervis.procedures.actions.move.ResolveMoveTypeStep
import dk.ilios.jervis.procedures.actions.move.calculateMoveTypesAvailable
import dk.ilios.jervis.procedures.getSetPlayerRushesCommand
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.Range
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

enum class PassingType {
    ACCURATE,
    INACCURATE,
    WILDLY_INACCURATE,
    FUMBLED
}

data class PassContext(
    val thrower: Player,
    val hasMoved: Boolean = false,
    val target: FieldCoordinate? = null,
    val range: Range? = null,
    val passingRoll: D6DieRoll? = null,
    val passingModifiers: List<DiceModifier> = emptyList(),
    val passingResult: PassingType? = null,
    val runInterference: Player? = null,
    val passingInterference: PassingInteferenceContext? = null,
) : ProcedureContext

/**
 * Procedure for controlling a player's Pass action.
 *
 * See page 48 in the rulebook.
 */
@Serializable
object PassAction : Procedure() {
    override val initialNode: Node = MoveOrPassOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            SetContext(PassContext(thrower = player))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<PassContext>()
        return compositeCommandOf(
            RemoveContext<PassContext>(),
            SetContext(state.getContext<ActivatePlayerContext>().copy(markActionAsUsed = context.hasMoved))
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        state.activePlayer ?: INVALID_GAME_STATE("No active player")
    }

    object MoveOrPassOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<PassContext>()
            val options = mutableListOf<ActionDescriptor>()

            // Find possible move types
            options.addAll(calculateMoveTypesAvailable(state.activePlayer!!, rules))

            // If holding the ball, the player can start the "Pass" section of the Pass action
            if (context.thrower.hasBall()) {
                options.add(ConfirmWhenReady) // TODO Do something more specific here?
            }

            // End the pass action before trying to throw the ball
            options.add(EndActionWhenReady)

            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return when (action) {
                Confirm -> {
                    GotoNode(ResolveThrow)
                }
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.thrower, action.moveType)
                    compositeCommandOf(
                        SetContext(context.copy(hasMoved = true)),
                        SetContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ResolveMoveTypeStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If player is not standing on the field after the move, it is a turn over,
            // otherwise they are free to continue their pass action.
            val context = state.getContext<PassContext>()
            return if (!context.thrower.isStanding(rules)) {
                compositeCommandOf(
                    SetTurnOver(TurnOver.STANDARD),
                    ExitProcedure()
                )
            } else {
                GotoNode(MoveOrPassOrEndAction)
            }
        }
    }

    object ResolveThrow : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return SetCurrentBall(context.thrower.ball)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PassStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return compositeCommandOf(
                SetCurrentBall(null),
                if (context.target == null) {
                    // No target was selected, so no pass was attempted, continue the pass.
                    GotoNode(MoveOrPassOrEndAction)
                } else {
                    ExitProcedure()
                }
            )
        }
    }
}
