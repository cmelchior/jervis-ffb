package com.jervisffb.engine.rules.common.procedures.actions.blitz

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerMoveLeft
import com.jervisffb.engine.commands.SetPlayerRushesLeft
import com.jervisffb.engine.commands.SetSkillUsed
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
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.BlitzActionContext
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.OnPitchLocation
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.SPRINT_EXTRA_RUSHES
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.StandardBlockStep
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.BB2020FallingOver
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock.SingleStandardBlockStep
import com.jervisffb.engine.rules.bb2025.procedures.tables.injury.BB2025FallingOver
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.procedures.actions.block.BreatheFireContext
import com.jervisffb.engine.rules.common.procedures.actions.block.BreatheFireStep
import com.jervisffb.engine.rules.common.procedures.actions.block.ProjectileVomitContext
import com.jervisffb.engine.rules.common.procedures.actions.block.ProjectileVomitStep
import com.jervisffb.engine.rules.common.procedures.actions.block.StabContext
import com.jervisffb.engine.rules.common.procedures.actions.block.StabStep
import com.jervisffb.engine.rules.common.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.rules.common.procedures.actions.move.RushRoll
import com.jervisffb.engine.rules.common.procedures.calculateMoveTypesAvailable
import com.jervisffb.engine.rules.common.procedures.estimatedMovesLeft
import com.jervisffb.engine.rules.common.procedures.getResetPlayerTemporaryModifiersCommands
import com.jervisffb.engine.rules.common.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.addIfNotNull

/**
 * Procedure for controlling a player's Blitz action.
 *
 * See page 43 in the rulebook.
 */
object BlitzAction : Procedure() {
    override val initialNode: Node = SelectTargetOrCancel
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            AddContext(BlitzActionContext(player))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val blitzContext = state.getContext<BlitzActionContext>()
        return compositeCommandOf(
            RemoveContext(blitzContext),
            when (blitzContext.hasBlocked || blitzContext.hasMoved) {
                true -> UpdateContext(activateContext.copy(markActionAsUsed = true))
                false -> null
            },
            *getResetPlayerTemporaryModifiersCommands(state, rules, activateContext.player, Duration.END_OF_ACTION),
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        state.activePlayer ?: INVALID_GAME_STATE("No active player")
    }

    object SelectTargetOrCancel : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlitzActionContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val attacker = state.getContext<BlitzActionContext>().attacker
            val availableTargetPlayers = attacker.team.otherTeam()
                .filter { it.location.isOnPitch(rules) && it.state == PlayerState.STANDING }

            return listOf(SelectPlayer.fromPlayers(availableTargetPlayers), EndActionWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.getContext<BlitzActionContext>()
                    compositeCommandOf(
                        UpdateContext(context.copy(defender = action.getPlayer(state))),
                        GotoNode(MoveOrBlockOrEndAction)
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }

    object MoveOrBlockOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlitzActionContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BlitzActionContext>()
            val blitzer = context.attacker
            val options = mutableListOf<GameActionDescriptor>()

            // Find possible move types
            options.addIfNotNull(calculateMoveTypesAvailable(state, blitzer))

            // Check if the attacker is adjacent to the target of the Blitz and is able to Block them
            val hasMovesLeft = blitzer.estimatedMovesLeft(includeSprint = true) > 0
            val isStanding = rules.isStanding(blitzer)
            if (context.attacker.location.isAdjacent(rules, context.defender!!.location) && hasMovesLeft && isStanding) {
                options.add(SelectPlayer(context.defender))
            }

            // End action before the block
            // As soon as a target is selected, you can no longer cancel the action
            // (Ideally this should be allowed until you take the first move)
            options.add(EndActionWhenReady)

            return options
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BlitzActionContext>()
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.attacker, action.moveType)
                    compositeCommandOf(
                        AddContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }
                is PlayerSelected -> {
                    compositeCommandOf(
                        UpdateContext(context.copy(
                            defender = action.getPlayer(state),
                            hasBlocked = true
                        )),
                        GotoNode(UseMoveToBlock)
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
            // otherwise they are free to continue their blitz
            // TODO This is wrong. It is only a turnover if the player was Knocked Down
            //  or went prone with the Ball. Need to rework this.
            //  This logic will probably also override scoring turn overs
            val moveContext = state.getContext<MoveContext>()
            val blitzContext = state.getContext<BlitzActionContext>()
            val endNow = state.endActionImmediately()
            return buildCompositeCommand {
                add(RemoveContext(moveContext))
                if (!rules.isStanding(blitzContext.attacker) && !endNow) {
                    addAllNonNull(
                        if (moveContext.hasMoved) UpdateContext(blitzContext.copy(hasMoved = true)) else null,
                        if (!state.isTurnOver()) SetTurnOver(TurnOver.STANDARD) else null,
                        ExitProcedure()
                    )
                } else if (!endNow) {
                    addAllNonNull(
                        if (moveContext.hasMoved) UpdateContext(blitzContext.copy(hasMoved = true)) else null,
                        GotoNode(if (blitzContext.hasBlocked) RemainingMovesOrEndAction else MoveOrBlockOrEndAction)
                    )
                } else {
                    // Something caused Blitz to end prematurely.
                    add(ExitProcedure())
                }
            }

        }
    }

    object UseMoveToBlock : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BlitzActionContext>()
            val player = context.attacker
            return when {
                player.movesLeft > 0 -> {
                    // Player has normal moves left to perform the blitz with
                    compositeCommandOf(
                        SetPlayerMoveLeft(state.activePlayer!!, state.activePlayer!!.movesLeft - 1),
                        GotoNode(SelectBlockType)
                    )
                }
                player.rushesLeft > 0 && player.movesLeft == 0 -> {
                    // Player has a base Rush left, so can just use that
                    GotoNode(RushBeforeBlock)
                }
                player.rushesLeft == 0 && player.movesLeft == 0 && player.isSkillAvailable(SkillType.SPRINT) -> {
                    // Player has no normal moves or base rushes left. The only
                    // way to proceed is by using Sprint. Since Sprint is optional,
                    // not using Sprint cancels the block type that was already chosen.
                    GotoNode(ChooseToUseSprintForBlocking)
                }
                else -> INVALID_GAME_STATE("Invalid state: rushes[${player.rushesLeft}], moves[${player.movesLeft}]")
            }
        }
    }

    // We only got here because we optimistically assume that Spring will be used.
    // If that doesn't turn out to be the case, the block will be aborted.
    //
    // As there are no negative consequences of using Sprint, we will always apply
    // it, unless the player has Frenzy and is in the process of executing their
    // 2nd block. In that case, we can not use Sprint to avoid the 2nd block.
    object ChooseToUseSprintForBlocking: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlitzActionContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BlitzActionContext>()
            val player = context.attacker
            val isSecondFrenzyBlock = (player.getSkillOrNull(SkillType.FRENZY)?.used == true)
            return when (isSecondFrenzyBlock) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Cancel -> {
                    GotoNode(RemainingMovesOrEndAction)
                }
                Continue,
                Confirm -> {
                    val context = state.getContext<BlitzActionContext>()
                    val player = context.attacker
                    compositeCommandOf(
                        ReportSkillUsed(player, SkillType.SPRINT),
                        SetSkillUsed(player, player.getSkill(SkillType.SPRINT), used = true),
                        SetPlayerRushesLeft(player, context.attacker.rushesLeft + SPRINT_EXTRA_RUSHES),
                        GotoNode(RushBeforeBlock)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RushBeforeBlock : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val blitzContext = state.getContext<BlitzActionContext>()
            return AddContext(RushRollContext(blitzContext.attacker, blitzContext.attacker.location as OnPitchLocation))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RushRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rushContext = state.getContext<RushRollContext>()
            return buildCompositeCommand {
                add(RemoveContext(rushContext))
                if (rushContext.isSuccess) {
                    add(SetPlayerRushesLeft(rushContext.player, rushContext.player.rushesLeft - 1))
                    add(GotoNode(SelectBlockType))
                } else {
                    add(GotoNode(ResolveFallingOverBeforeBlock))
                }
            }
        }
    }

    object ResolveFallingOverBeforeBlock: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BlitzActionContext>()
            return AddContext(RiskingInjuryContext(context.attacker, mode = RiskingInjuryMode.FALLING_OVER))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return when (rules.baseVersion) {
                GameVersion.BB2020 -> BB2020FallingOver
                GameVersion.BB2025 -> BB2025FallingOver
            }
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Regardless of the outcome of rolling for falling over, the player's action
            // ended in a turnover
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }

    // This procedure assumes that the caller has verified that the player has enough moves left to perform
    // the block.
    object SelectBlockType : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlitzActionContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val attacker = state.getContext<BlitzActionContext>().attacker
            val availableBlockTypes = BlockAction.getAvailableBlockType(attacker, true)
            return buildList {
                add(SelectBlockType(availableBlockTypes))
                // We can always cancel a block unless the player has Frenzy and is about to throw a
                // 2nd block, in that case, it isn't optional.
                // TODO We also need to consider if the player ran out of moves here?
                if (attacker.getSkillOrNull(SkillType.FRENZY)?.used != true) {
                    add(DeselectPlayer(attacker))
                }
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BlitzActionContext>()
            return when (action) {
                is PlayerDeselected -> {
                    GotoNode(MoveOrBlockOrEndAction)
                }
                else -> {
                    castAction<BlockTypeSelected>(action) { typeSelected ->
                        compositeCommandOf(
                            UpdateContext(context.copy(blockType = typeSelected.type)),
                            GotoNode(ResolveBlock),
                        )
                    }
                }
            }
        }
    }

    object ResolveBlock : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BlitzActionContext>()
            return when (context.blockType!!) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.BREATHE_FIRE -> {
                    AddContext(
                        BreatheFireContext(
                            context.attacker,
                            context.defender!!,
                        )
                    )
                }
                BlockType.PROJECTILE_VOMIT -> {
                    AddContext(
                        ProjectileVomitContext(
                            attacker = context.attacker,
                            defender = context.defender!!
                        )
                    )
                }
                BlockType.STAB -> {
                    AddContext(
                        StabContext(
                            attacker = context.attacker,
                            defender = context.defender!!
                        )
                    )
                }
                BlockType.STANDARD -> {
                    AddContext(
                        BlockContext(
                            context.attacker,
                            context.defender!!,
                            isBlitzing = true
                        )
                    )
                }
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<BlitzActionContext>()
            return when (context.blockType!!) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.BREATHE_FIRE -> BreatheFireStep
                BlockType.PROJECTILE_VOMIT -> ProjectileVomitStep
                BlockType.STAB -> StabStep
                BlockType.STANDARD -> {
                    when (rules.baseVersion) {
                        GameVersion.BB2020 -> StandardBlockStep
                        GameVersion.BB2025 -> SingleStandardBlockStep
                    }
                }
            }
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If player is not standing on the pitch after the move, it is a turn over,
            // otherwise they are free to continue their blitz
            // TODO This approach to turn overs might not be correct, i.e. a touchdown
            // could have been scored after a Blitz
            val context = state.getContext<BlitzActionContext>()

            // Check if Block Action was completed or not
            val removeContextCommand = when (context.blockType!!) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.BREATHE_FIRE -> RemoveContext<BreatheFireContext>()
                BlockType.PROJECTILE_VOMIT -> RemoveContext<ProjectileVomitContext>()
                BlockType.STAB -> RemoveContext<StabContext>()
                BlockType.STANDARD -> RemoveContext<BlockContext>()
            }

            // Remove state required for the specific block type
            val hasBlocked = when (context.blockType) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.BREATHE_FIRE -> (state.getContext<BreatheFireContext>().result != null)
                BlockType.PROJECTILE_VOMIT -> (state.getContext<ProjectileVomitContext>().injuryResult != null)
                BlockType.STAB -> (state.getContext<StabContext>().stabResult != null)
                BlockType.STANDARD -> !state.getContext<BlockContext>().aborted
            }

            // After the Push was resolved, if the target is still standing
            // and the attacker has frenzy and was able to follow up, a
            // second block is thrown
            val hasFrenzy = context.attacker.isSkillAvailable(SkillType.FRENZY)
            val isNextToTarget = rules.isStanding(context.defender!!)
                && rules.isStanding(context.attacker)
                && context.attacker.coordinates
                    .getSurroundingCoordinates(rules, distance = 1)
                    .contains(context.defender.coordinates)

            return if (state.endActionImmediately()) {
                compositeCommandOf(
                    removeContextCommand,
                    ExitProcedure()
                )
            } else if (hasBlocked && hasFrenzy && isNextToTarget) {
                val hasMoveLeft = context.attacker.estimatedMovesLeft(includeSprint = true) > 0
                compositeCommandOf(
                    removeContextCommand,
                    UpdateContext(context.copy(hasBlocked = hasBlocked)),
                    SetSkillUsed(context.attacker, context.attacker.getSkill(SkillType.FRENZY), true),
                    if (hasMoveLeft) GotoNode(UseMoveToBlock) else GotoNode(RemainingMovesOrEndAction),
                )
            } else {
                compositeCommandOf(
                    removeContextCommand,
                    UpdateContext(context.copy(hasBlocked = hasBlocked)),
                    GotoNode(RemainingMovesOrEndAction),
                    *getResetPlayerTemporaryModifiersCommands(state, rules, context.attacker, Duration.END_OF_ACTION),
                )
            }
        }
    }

    object RemainingMovesOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlitzActionContext>().attacker.team

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val options = mutableListOf<GameActionDescriptor>()
            // Find possible move types
            options.addIfNotNull(calculateMoveTypesAvailable(state, state.activePlayer!!))

            // End action before the block
            // As soon as a target is selected, you can no longer cancel the action
            // (Ideally this should be allowed until you take the first move)
            options.add(EndActionWhenReady)
            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BlitzActionContext>()
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.attacker, action.moveType)
                    compositeCommandOf(
                        UpdateContext(context.copy(hasMoved = true)),
                        AddContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }
}
