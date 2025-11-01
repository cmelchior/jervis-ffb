package com.jervisffb.engine.rules.common.procedures.actions.throwteammate

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
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.ReportPickingUpPlayerToThrow
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.RightStuff
import com.jervisffb.engine.rules.common.procedures.ActivatePlayerContext
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.rules.common.procedures.calculateMoveTypesAvailable
import com.jervisffb.engine.rules.common.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.addIfNotNull

// See page 53 in the rulebook
enum class ThrowPlayerResult {
    SUPERB_THROW,
    SUCCESSFUL_THROW,
    TERRIBLE_THROW,
    FUMBLED_THROW,
}

data class ThrowTeamMateContext(
    val thrower: Player,
    val thrownPlayer: Player? = null,
    val hasMoved: Boolean = false,
    // Target of the throw in the current step. This means it will be updated when the ball scatters, deviates, etc.
    val target: FieldCoordinate? = null,
    val range: Range? = null,
    val qualityRoll: D6DieRoll? = null,
    val qualityRollModifiers: List<DiceModifier> = emptyList(),
    val qualityRollResult: ThrowPlayerResult? = null,
    // If a player without TZ or prone/stunned are thrown they will bounce one
    // extra time before landing.
    val willCrashLand: Boolean = false,
    // If a player bounces on another player, they will automatically be Knocked Down when finally landing.
    val knockedDownWhenLanding: Boolean = false,
    // If the player scattered, deviated or bounced into the crowd while holding the ball.
    // The ball should be thrown in from this field.
    val outOfBoundsAt: FieldCoordinate? = null
) : ProcedureContext

/**
 * Procedure for controlling a player's Throw team-mate action.
 * See page 52 in the rulebook.
 *
 * This procedure assumes that the caller has checked that the thrower has the
 * Throw Team-mate trait
 *
 */
object ThrowTeamMateAction : Procedure() {
    override val initialNode: Node = MoveOrThrowPlayerOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            SetContext(
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
            RemoveContext<ThrowTeamMateContext>(),
            SetContext(
                activePlayerContext.copy(
                    markActionAsUsed = (context.hasMoved || context.qualityRoll != null)
                )
            )
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
            options.addIfNotNull(calculateMoveTypesAvailable(state, state.activePlayer!!))

            // If the thrower is next to a standing player with "Right Stuff", they can be selected for the throw
            if (thrower.state == PlayerState.STANDING && throwerLocation is OnFieldLocation) {
                val eligiblePlayers = throwerLocation.getSurroundingCoordinates(rules, 1).mapNotNull {
                    val player = state.field[it].player

                    val maxStrength = when (val rightStuffSkill = player?.getSkillOrNull(SkillType.RIGHT_STUFF)) {
                        null -> Int.MIN_VALUE
                        is RightStuff -> rightStuffSkill.maxStrength
                        is com.jervisffb.engine.rules.bb2025.skills.RightStuff -> rightStuffSkill.maxStrength
                        else -> INVALID_GAME_STATE("Unknown Right Stuff skill type: ${rightStuffSkill::class.simpleName}")
                    }

                    val canBeThrown = player != null
                        && player.team == thrower.team
                        && player.hasSkill(SkillType.RIGHT_STUFF)
                        && player.strength <= maxStrength
                    if (canBeThrown) {
                        player.id
                    } else {
                        null
                    }
                }
                if (eligiblePlayers.isNotEmpty()) {
                    options.add(SelectPlayer(eligiblePlayers))
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
                is PlayerSelected -> checkTypeAndValue<PlayerSelected>(state, action) {
                    val context = state.getContext<ThrowTeamMateContext>()
                    val thrownPlayer = it.getPlayer(state)
                    val willCrashLand = !thrownPlayer.hasTackleZones
                        || thrownPlayer.state == PlayerState.PRONE
                        || thrownPlayer.state == PlayerState.STUNNED
                        || thrownPlayer.state == PlayerState.STUNNED_OWN_TURN
                    compositeCommandOf(
                        ReportPickingUpPlayerToThrow(context, thrownPlayer),
                        SetContext(context.copy(
                            thrownPlayer = thrownPlayer,
                            willCrashLand = willCrashLand
                        )),
                        GotoNode(ResolveThrowPlayer)
                    )
                }
                is MoveTypeSelected -> checkTypeAndValue<MoveTypeSelected>(state, action) { moveTypeAction ->
                    val moveContext = MoveContext(context.thrower, moveTypeAction.moveType)
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
            // If a player is not standing on the field after the move, it is a turnover,
            // otherwise they are free to continue their Throw Team-mate action.
            val moveContext = state.getContext<MoveContext>()
            val context = state.getContext<ThrowTeamMateContext>()
            return buildCompositeCommand {
                if (moveContext.hasMoved) {
                    add(SetContext(context.copy(hasMoved = true)))
                }
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowPlayerStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ThrowTeamMateContext>()
            return compositeCommandOf(
                if (context.target == null) {
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
