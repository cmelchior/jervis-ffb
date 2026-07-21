package com.jervisffb.engine.bb2025.skills

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.HypnoticGazeContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.rules.common.procedures.calculateMoveTypesAvailable
import com.jervisffb.engine.rules.common.procedures.getResetPlayerTemporaryModifiersCommands
import com.jervisffb.engine.rules.common.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.utils.endActionImmediately
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.addIfNotNull

/**
 * Procedure for handling Hypnotic Gaze as a standalone special action.
 *
 * The player can make a normal Move Action before attempting the Hypnotic Gaze.
 * Once the gaze has been attempted, the activation ends immediately.
 *
 * See page 129 in the BB2025 rulebook.
 */
object HypnoticGazeAction : Procedure() {
    override val initialNode: Node = MoveOrGazeOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            AddContext(HypnoticGazeContext(gazer = player))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<HypnoticGazeContext>()
        val activePlayerContext = state.getContext<ActivatePlayerContext>()
        return compositeCommandOf(
            RemoveContext(context),
            UpdateContext(activePlayerContext.copyWithMarkedAction(context.hasMoved || context.hasGazed)),
            *getResetPlayerTemporaryModifiersCommands(state, rules, activePlayerContext.player, Duration.END_OF_ACTION),
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        state.activePlayer ?: INVALID_GAME_STATE("Missing active player")
    }

    object MoveOrGazeOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<HypnoticGazeContext>()
            val options = mutableListOf<GameActionDescriptor>()
            options.addIfNotNull(calculateMoveTypesAvailable(state, state.activePlayer!!))
            if (!context.hasGazed) {
                val eligibleTargets = context.gazer.coordinates.getSurroundingCoordinates(rules, 1)
                    .mapNotNull { state.pitch[it].player }
                    .filter { it.team != context.gazer.team }
                    .filter { rules.isStanding(it) }
                    .let { players ->
                        if (players.isNotEmpty()) {
                            options.add(SelectPlayer.fromPlayers(players))
                        }
                    }
            }
            options.add(EndActionWhenReady)
            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<HypnoticGazeContext>()
            return when (action) {
                EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    val target = action.getPlayer(state)
                    compositeCommandOf(
                        UpdateContext(context.copy(target = target)),
                        GotoNode(ResolveGaze)
                    )
                }
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.gazer, action.moveType)
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
            val gazeContext = state.getContext<HypnoticGazeContext>()
            return buildCompositeCommand {
                if (moveContext.hasMoved) {
                    add(UpdateContext(gazeContext.copy(hasMoved = true)))
                }
                add(RemoveContext(moveContext))
                if (state.endActionImmediately()) {
                    add(ExitProcedure())
                } else if (!rules.isStanding(gazeContext.gazer)) {
                    addAll(
                        SetTurnOver(TurnOver.STANDARD),
                        ExitProcedure()
                    )
                } else {
                    add(GotoNode(MoveOrGazeOrEndAction))
                }
            }
        }
    }

    object ResolveGaze : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = HypnoticGazeStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
