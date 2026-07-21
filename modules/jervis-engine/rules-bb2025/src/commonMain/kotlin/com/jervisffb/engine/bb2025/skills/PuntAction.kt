package com.jervisffb.engine.rules.common.procedures.actions.punt

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.SelectPassType
import com.jervisffb.engine.bb2025.skills.PuntStep
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.SetTurnOver
import com.jervisffb.engine.common.context.ActivatePlayerContext
import com.jervisffb.engine.common.context.PuntContext
import com.jervisffb.engine.common.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.common.procedures.calculateMoveTypesAvailable
import com.jervisffb.engine.common.procedures.getResetPlayerTemporaryModifiersCommands
import com.jervisffb.engine.common.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.common.utils.endActionImmediately
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.addIfNotNull

/**
 * Procedure for handling Punt as a standalone special action.
 *
 * See page 135 in the BB2025 rulebook.
 */
object PuntAction : Procedure() {
    override val initialNode: Node = MoveOrPuntOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            AddContext(PuntContext(punter = player))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<PuntContext>()
        val activePlayerContext = state.getContext<ActivatePlayerContext>()
        return compositeCommandOf(
            RemoveContext(context),
            UpdateContext(activePlayerContext.copyWithMarkedAction(context.hasMoved || context.hasPunted)),
            *getResetPlayerTemporaryModifiersCommands(state, rules, activePlayerContext.player, Duration.END_OF_ACTION),
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        state.activePlayer ?: error("No active player")
    }

    object MoveOrPuntOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PuntContext>()
            val options = mutableListOf<GameActionDescriptor>()
            options.addIfNotNull(calculateMoveTypesAvailable(state, state.activePlayer!!))
            if (!context.hasPunted && context.punter.hasBall()) {
                options.add(SelectPassType(listOf(PassType.PUNT)))
            }
            options.add(EndActionWhenReady)
            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PuntContext>()
            return when (action) {
                EndAction -> ExitProcedure()
                is PassTypeSelected -> GotoNode(ResolvePunt)
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.punter, action.moveType)
                    compositeCommandOf(
                        AddContext(moveContext),
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
            val moveContext = state.getContext<MoveContext>()
            val puntContext = state.getContext<PuntContext>()
            return buildCompositeCommand {
                if (moveContext.hasMoved) {
                    add(UpdateContext(puntContext.copy(hasMoved = true)))
                }
                add(RemoveContext(moveContext))
                if (state.endActionImmediately()) {
                    add(ExitProcedure())
                } else if (!rules.isStanding(puntContext.punter)) {
                    addAll(
                        SetTurnOver(TurnOver.STANDARD),
                        ExitProcedure()
                    )
                } else {
                    add(GotoNode(MoveOrPuntOrEndAction))
                }
            }
        }
    }

    object ResolvePunt : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PuntStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
