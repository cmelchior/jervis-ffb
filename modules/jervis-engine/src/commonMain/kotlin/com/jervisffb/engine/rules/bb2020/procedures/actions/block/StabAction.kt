package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayerContext
import com.jervisffb.engine.utils.INVALID_ACTION
import kotlinx.serialization.Serializable

/**
 * Procedure for handling the Stab special action as described on page 86 in the rulebook
 */
@Serializable
object StabAction : Procedure() {
    override val initialNode: Node = SelectDefenderOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            RemoveContext<BlockContext>(),
            RemoveContext<BlockActionContext>()
        )

    }

    object SelectDefenderOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val end: List<GameActionDescriptor> = listOf(EndActionWhenReady)

            val attacker = state.activePlayer!!
            val eligibleDefenders: List<GameActionDescriptor> =
                attacker.coordinates.getSurroundingCoordinates(rules)
                    .filter { state.field[it].isOccupied() }
                    .filter { state.field[it].player!!.team != attacker.team }
                    .map { SelectPlayer(state.field[it].player!!) }

            return end + eligibleDefenders
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndAction -> {
                    val activeContext = state.getContext<ActivatePlayerContext>()
                    compositeCommandOf(
                        SetContext(activeContext.copy(markActionAsUsed = false)),
                        ExitProcedure()
                    )
                }
                is PlayerSelected -> {
                    val context = BlockActionContext(
                        attacker = state.activePlayer!!,
                        defender = action.getPlayer(state),
                    )
                    compositeCommandOf(
                        SetContext(context),
                        GotoNode(ResolveBlock),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveBlock : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<BlockActionContext>()
            return SetContext(
                BlockContext(
                    attacker = actionContext.attacker,
                    defender = actionContext.defender,
                    blockType = BlockType.STANDARD
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Regardless of the outcome of the block, the action is over
            val activeContext = state.getContext<ActivatePlayerContext>()
            val actionContext = state.getContext<BlockActionContext>()
            return buildCompositeCommand {
                if (!actionContext.hasBlocked) {
                    add(SetContext(activeContext.copy(markActionAsUsed = true)))
                }
                add(ExitProcedure())
            }
        }
    }
}
