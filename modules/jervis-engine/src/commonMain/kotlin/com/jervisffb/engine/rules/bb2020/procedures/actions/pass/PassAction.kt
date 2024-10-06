package com.jervisffb.engine.rules.bb2020.procedures.actions.pass

import buildCompositeCommand
import compositeCommandOf
import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayerContext
import com.jervisffb.engine.rules.bb2020.procedures.D6DieRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.calculateMoveTypesAvailable
import com.jervisffb.engine.rules.bb2020.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.Range
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
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
        val activePlayerContext = state.getContext<ActivatePlayerContext>()
        return compositeCommandOf(
            RemoveContext<PassContext>(),
            SetContext(
                activePlayerContext.copy(
                    markActionAsUsed = (context.hasMoved || context.passingRoll != null)
                )
            )
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
            val moveContext = state.getContext<MoveContext>()
            val context = state.getContext<PassContext>()
            return buildCompositeCommand {
                if (moveContext.hasMoved) {
                    add(SetContext(context.copy(hasMoved = true)))
                }
                if (!context.thrower.isStanding(rules)) {
                    add(SetTurnOver(TurnOver.STANDARD))
                    add(ExitProcedure())
                } else {
                    add(GotoNode(MoveOrPassOrEndAction))
                }
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
