package com.jervisffb.engine.rules.common.procedures.actions.handoff

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.CatchContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.Catch
import com.jervisffb.engine.rules.common.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.rules.common.procedures.calculateMoveTypesAvailable
import com.jervisffb.engine.rules.common.procedures.getResetPlayerTemporaryModifiersCommands
import com.jervisffb.engine.rules.common.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.addIfNotNull


data class HandOffContext(
    val thrower: Player,
    val catcher: Player? = null,
    val hasMoved: Boolean = false,
    val hasHandedOff: Boolean = false
) : ProcedureContext {
}

/**
 * Procedure for controlling a player's Hand-off action.
 *
 * See page 51 in the BB2020 rulebook (and page 26 for loosing tackle zones).
 * See page 74 in the BB2025 rulebook.
 *
 * Developer's Commentary
 * There is a subtle difference between BB2020 and BB2025. In BB2020, the
 * target (team player) only had to be Standing, whereas in BB2025, the player
 * must also have their tackle zone.
 *
 * In either case, the player cannot catch the ball, but in BB2020, you could
 * actually do the hand-off and let the ball bounce. In BB2025, that is no
 * longer possible.
 */
object HandOffAction : Procedure() {
    override val initialNode: Node = MoveOrHandOffOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            AddContext(HandOffContext(player))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val handOffContext = state.getContext<HandOffContext>()
        val activePlayerContext = state.getContext<ActivatePlayerContext>()
        return compositeCommandOf(
            RemoveContext(handOffContext),
            UpdateContext(activePlayerContext.copyWithMarkedAction(handOffContext.hasMoved || handOffContext.catcher != null)),
            *getResetPlayerTemporaryModifiersCommands(state, rules, activePlayerContext.player, Duration.END_OF_ACTION),
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        if (state.activePlayer == null) INVALID_GAME_STATE("No active player")
    }

    object MoveOrHandOffOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<HandOffContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<HandOffContext>()
            val options = mutableListOf<GameActionDescriptor>()

            // Find possible move types
            options.addIfNotNull(calculateMoveTypesAvailable(state, context.thrower))

            // Check if adjacent to a possible receiver
            if (context.thrower.hasBall()) {
                context.thrower.coordinates.getSurroundingCoordinates(rules, 1)
                    .mapNotNull { state.pitch[it].player }
                    .filter {
                        // In BB2025, the target must also have their tackle zones, unlike
                        // BB2020, where this wasn't a requirement.
                        val verifiedTackleZones = when (rules.baseVersion) {
                            GameVersion.BB2020 -> true
                            GameVersion.BB2025 -> it.hasTackleZones
                        }
                        it.team == context.thrower.team
                            && it.state == PlayerState.STANDING
                            && verifiedTackleZones
                    }
                    .let {
                        if (it.isNotEmpty()) {
                            options.add(SelectPlayer.fromPlayers(it))
                        }
                    }
            }

            // Just end the action
            options.add(EndActionWhenReady)
            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val handOffContext = state.getContext<HandOffContext>()
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(handOffContext.thrower, action.moveType)
                    compositeCommandOf(
                        UpdateContext(handOffContext.copy(hasMoved = true)),
                        AddContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }
                is PlayerSelected -> {
                    val ball = handOffContext.thrower.ball!!
                    compositeCommandOf(
                        UpdateContext(handOffContext.copy(catcher = action.getPlayer(state))),
                        SetCurrentBall(ball),
                        SetBallState.accurateThrow(ball),
                        SetBallLocation(ball, action.getPlayer(state).coordinates),
                        GotoNode(ResolveBallHandedOff),
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ResolveMoveTypeStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If player is not standing on the pitch after the move, it is a turn over,
            // otherwise they are free to continue their hand-off.
            val moveContext = state.getContext<MoveContext>()
            val handOffContext = state.getContext<HandOffContext>()
            val endNow = state.endActionImmediately()
            return buildCompositeCommand {
                if (moveContext.hasMoved) {
                    add(UpdateContext(handOffContext.copy(hasMoved = true)))
                }
                add(RemoveContext(moveContext))
                if (endNow) {
                    add(ExitProcedure())
                } else if (!rules.isStanding(handOffContext.thrower)) {
                    add(SetTurnOver(TurnOver.STANDARD))
                    add(ExitProcedure())
                } else {
                    if (handOffContext.hasHandedOff) {
                        add(GotoNode(MoveOrEndAction))
                    } else {
                        add(GotoNode(MoveOrHandOffOrEndAction))
                    }
                }
            }
        }
    }

    object ResolveBallHandedOff: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<HandOffContext>()
            val canCatch = rules.canCatch(context.catcher!!)
            return when (canCatch) {
                true -> GotoNode(ResolveCatch)
                false -> GotoNode(ResolveBounce)
            }
        }
    }

    object ResolveCatch : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            // Determine target and modifiers for the Catch roll
            val ball = state.currentBall()
            val catchingPlayer = state.pitch[ball.coordinates].player!!
            val rollContext = CatchContext(catchingPlayer, ball)
            return AddContext(rollContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<CatchContext>(),
                GotoNode(ChooseStepAfterHandOff)
            )
        }
    }

    object ResolveBounce: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return SetBallState.bouncing(state.currentBall())
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ChooseStepAfterHandOff)
        }
    }

    object ChooseStepAfterHandOff: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // If no player on the throwers team holds the ball after the hand-off is complete, it is a turnover.
            // If not, if the player has Give and Go they can continue to move, otherwise the action ends.
            val context = state.getContext<HandOffContext>()

            // Check if the conditions for using Give and Go are present
            // While this skill is only available in BB205, it should be safe to do the checks in the common action
            val hasGiveAndGo = context.thrower.isSkillAvailable(SkillType.GIVE_AND_GO)
            val isOtherTurnOverCause = state.isTurnOver()
            val teamHasBall = rules.teamHasBall(context.thrower.team, state.currentBall())
            val canUseGiveAndGo = hasGiveAndGo && teamHasBall && !isOtherTurnOverCause

            return buildCompositeCommand {
                add(SetCurrentBall(null))
                add(UpdateContext(context.copy(hasHandedOff = true)))
                if (!teamHasBall) {
                    add(SetTurnOver(TurnOver.STANDARD))
                }
                if (canUseGiveAndGo) {
                    addAll(
                        ReportSkillUsed(context.thrower, SkillType.GIVE_AND_GO),
                        GotoNode(MoveOrEndAction)
                    )
                } else {
                    add(ExitProcedure())
                }
            }
        }
    }

    // If thrower has Give and Go, after the handoff, they can continue to move.
    object MoveOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<HandOffContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            if (state.endActionImmediately()) {
                return listOf(ContinueWhenReady)
            }
            val options = mutableListOf<GameActionDescriptor>()
            // Find possible move types
            options.addIfNotNull(calculateMoveTypesAvailable(state, state.activePlayer!!))
            // End the action
            options.add(EndActionWhenReady)
            return options
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<HandOffContext>()
            return when (action) {
                Continue, EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.thrower, action.moveType)
                    compositeCommandOf(
                        AddContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }
}
