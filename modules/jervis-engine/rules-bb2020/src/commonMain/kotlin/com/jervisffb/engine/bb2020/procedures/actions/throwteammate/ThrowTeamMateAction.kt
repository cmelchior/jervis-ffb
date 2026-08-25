package com.jervisffb.engine.bb2020.procedures.actions.throwteammate

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
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.SetTurnOver
import com.jervisffb.engine.common.context.ActivatePlayerContext
import com.jervisffb.engine.common.context.ThrowTeamMateContext
import com.jervisffb.engine.common.modifiers.isRooted
import com.jervisffb.engine.common.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.common.procedures.getResetPlayerTemporaryModifiersCommands
import com.jervisffb.engine.common.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.common.reports.ReportPickingUpPlayerToThrow
import com.jervisffb.engine.common.utils.endActionImmediately
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.OnPitchLocation
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.addIfNotNull

/**
 * Procedure for controlling a player's Throw team-mate action.
 * See page 52 in the BB2020 rulebook.
 *
 * This procedure assumes that the caller has checked that the thrower has the
 * Throw Team-mate trait.
 */
object ThrowTeamMateAction : Procedure() {
    override val initialNode: Node = MoveOrThrowPlayerOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            AddContext(
                ThrowTeamMateContext(
                    thrower = player,
                )
            )
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<ThrowTeamMateContext>()
        val activePlayerContext = state.getContext<ActivatePlayerContext>()
        return compositeCommandOf(
            RemoveContext(context),
            UpdateContext(
                activePlayerContext.copyWithMarkedAction(context.hasMoved || context.qualityRoll != null)
            ),
            *getResetPlayerTemporaryModifiersCommands(state, rules, activePlayerContext.player, Duration.END_OF_ACTION),
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        state.activePlayer ?: INVALID_GAME_STATE("No active player")
        if (state.activePlayer?.hasSkill(SkillType.THROW_TEAMMATE) != true) {
            INVALID_GAME_STATE("Player does not have Throw Team-mate: ${state.activePlayer}")
        }
    }

    object MoveOrThrowPlayerOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            if (state.endActionImmediately()) {
                return listOf(ContinueWhenReady)
            }

            val context = state.getContext<ThrowTeamMateContext>()
            val options = mutableListOf<GameActionDescriptor>()
            val thrower = context.thrower
            val throwerLocation = thrower.location

            // Find possible move types
            options.addIfNotNull(rules.calculateMoveTypesAvailable(state, state.activePlayer!!))

            // If the thrower is next to a player with "Right Stuff", they can be selected for the throw
            if (rules.isStanding(thrower)) {
                (throwerLocation as OnPitchLocation).getSurroundingCoordinates(rules, 1)
                    .mapNotNull {
                        state.pitch[it].player
                    }
                    .mapNotNull { player ->
                        val maxStrength = when (player.getSkillOrNull(SkillType.RIGHT_STUFF)) {
                            null -> Int.MIN_VALUE
                            else -> rules.rightStuffMaxStrength
                        }

                        // Rooted players cannot leave their square, so they cannot be thrown.
                        val isRooted = player.isRooted()

                        val canBeThrown = player.team == thrower.team
                            && player.isSkillAvailable(SkillType.RIGHT_STUFF)
                            && player.strength <= maxStrength
                            && !isRooted
                        if (canBeThrown) {
                            player
                        } else {
                            null
                        }
                    }.let { eligiblePlayers ->
                        if (eligiblePlayers.isNotEmpty()) {
                            options.add(SelectPlayer.fromPlayers(eligiblePlayers))
                        }
                    }
            }

            // End the pass action before trying to throw the ball
            options.add(EndActionWhenReady)

            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            return when (action) {
                Continue, EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.getContext<ThrowTeamMateContext>()
                    val thrownPlayer = action.getPlayer(state)
                    val willCrashLand = !thrownPlayer.hasTackleZones
                        || thrownPlayer.state == PlayerPitchState.PRONE
                        || thrownPlayer.state == PlayerPitchState.STUNNED
                        || thrownPlayer.state == PlayerPitchState.STUNNED_OWN_TURN
                    compositeCommandOf(
                        ReportPickingUpPlayerToThrow(context, thrownPlayer),
                        UpdateContext(
                            context.copy(
                                thrownPlayer = thrownPlayer,
                                willCrashLand = willCrashLand
                            )
                        ),
                        GotoNode(ResolveThrowPlayer)
                    )
                }

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

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ResolveMoveTypeStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If a player is not standing on the pitch after the move, it is a turnover,
            // otherwise they are free to continue their Throw Team-mate action.
            val moveContext = state.getContext<MoveContext>()
            val context = state.getContext<ThrowTeamMateContext>()
            return buildCompositeCommand {
                if (moveContext.hasMoved) {
                    add(UpdateContext(context.copy(hasMoved = true)))
                }
                add(RemoveContext(moveContext))
                if (state.endActionImmediately()) {
                    add(ExitProcedure())
                } else if (!rules.isStanding(context.thrower)) {
                    add(SetTurnOver(TurnOver.STANDARD))
                    add(ExitProcedure())
                } else {
                    add(GotoNode(MoveOrThrowPlayerOrEndAction))
                }
            }
        }
    }

    object ResolveThrowPlayer : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.throwPlayerStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            return compositeCommandOf(
                if (context.target == null && !state.isTurnOver()) {
                    // No target was selected, so no throw was attempted, continue the action.
                    GotoNode(MoveOrThrowPlayerOrEndAction)
                } else {
                    // Thrower is not allowed to move after the throw, regardless of the outcome.
                    ExitProcedure()
                }
            )
        }
    }
}
