package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.context.SetContextProperty
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.MultipleBlockContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayerContext
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.rules.bb2020.procedures.ThrowIn
import com.jervisffb.engine.rules.bb2020.procedures.ThrowInContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.multipleblock.MultipleBlockChoseResults
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.multipleblock.MultipleBlockRerollDice
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.multipleblock.MultipleBlockResolveInjuries
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ScoringATouchDownContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * This procedure is responsible for handling Multiple Block as described on
 * page 80 in the rulebook.
 *
 * Designer's Commentary:
 * - When using Grab, the blocked players starting square are treated as being
 *   Occupied, regardless of who is moved first.
 * - Blocked players cannot offer assist to each other.
 * - Stab can be used on both block actions.
 *
 * Developer's Commentary:
 * Multiple Block is a challenge to implement as it breaks the flow of multiple
 * skills and actions. It is also very vaguely worded, and the description
 * doesn't cover a lot of edge cases.
 *
 * It is especially unclear how "performed simultaneously" should be
 * interpreted, but to accommodate Designer's Commentary, we try to resolve the
 * blocks in lock-steps (or as much as possible).
 *
 * A full discussion of the how to interpret the rules for Multiple Block is
 * found in rules-faq.md, so here we will just focus on the implementation.
 *
 * The sequence of events in a Multiple Block are resolved in the following way:
 *
 * 1. Select the Multiple Block Special Action
 *
 * 2. Select target 1 and type of block: Normal or special.
 *
 * 3. Select target 2 and type of block: Normal or special.
 *
 * 4. Roll block dice (or special attack dice) for target 1.
 *
 * 5. Roll block dice (or special attack dice) for target 2.
 *
 * 6. Choose rerolls or accept results for all dice against target 1 and target
 *    2. This is done at the same time for both targets.
 *
 * 7. Select the final dice results for both target 1 and target 2. This is done
 *    at the same time for both targets.
 *
 * 8. Choose which target to resolve block for first, now named A and B.
 *
 * 9. Determine push chain for target A. This is now Push Chain A. Similar to
 *    how it is done for single blocks.
 *
 * 10. Move all players in Push Chain A.
 *
 * 11. If the last player is pushed into the crowd, roll for injury and use the
 *     apothecary. But do not throw in the ball.
 *
 * 12. Determine push chain for target B. This is now Push Chain B. Taking into
 *     account the adjusted positions for players moved in Push Chain A. This
 *     means a player might be moved multiple times. If Grab is used, the
 *     original position for the defender in Block 1, is considered occupied (as
 *     per Designer's Commentary).
 *
 * 13. Move all players in Push Chain B.
 *
 * 14. If the last player is pushed into the crowd, roll for injury and use the
 *     apothecary. But do not throw in the ball.
 *
 * 15. Resolve the result of Block A, i.e. roll for armour/injury if needed. If
 *     an injury occurs, place the player in either the Attacker or Defender
 *     Injury Pool. But do not finalize the injury yet. If the player is knocked
 *     down while holding a ball, it is knocked loose, but does not bounce yet.
 *
 * 16. Resolve the result of Block B, i.e. roll for armour/injury if needed. If
 *     an injury occurs, place the player in either the Attacker or Defender
 *     Injury Pool. The attacker can suffer two different injuries. But do not
 *     finalize the injury yet. If the player is knocked down while holding a
 *     ball, it is knocked loose, but does not bounce yet.
 *
 * 17. Resolve the Defender Injury Pool. Players must be handled in order until
 *     the injury is fully resolved, i.e., using apothecary, regeneration, etc.
 *     Once resolved, the injury is removed. Continue until the pool is empty.
 *
 * 18. Resolve the Attacker Injury Pool. Each injury is fully resolved before
 *     going to the next, i.e., using apothecary, regeneration, etc.
 *     Once resolved, the injury is removed. Continue until the pool is empty.
 *
 * 19. Check for Trapdoors in Push Chain A, starting from defender A. Go
 *     through all squares players are pushed into. Roll for any player standing
 *     on a trapdoor. If they are injured because of it, resolve the injury
 *     immediately.
 *
 * 20. Check for Trapdoors in Push Chain B, starting from the defender B. Go
 *     through all squares players are pushed into. Roll for any player standing
 *     on a trapdoor. If they are injured because of it, resolve the injury
 *     immediately.
 *
 * 21. Check for Touchdowns. Attacker cannot have scored, since they cannot have
 *     moved. Check all players, starting from the defender in Push Chain A,
 *     then in Push Chain B. If a player has scored, no other players can score
 *     as well.
 *
 * 22. Resolve ball events in each square part of the push chain (bounce /
 *     throw-in). Start with defender in Push Chain A. Then Push Chain B, end
 *     with the attacker. If a ball lands on a player after a bounce or throw-in,
 *     that triggers a touchdown. The remaining ball handling sequence still
 *     needs to be completed, but no further touchdowns are scored.
 */
object MultipleBlockAction: Procedure() {
    override val initialNode: Node = SelectDefenderOrAbortActionOrContinueBlock
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val activeContext = state.getContext<ActivatePlayerContext>()
        return SetContext(
            MultipleBlockContext(
                attacker = activeContext.player
            )
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<MultipleBlockContext>()
        return compositeCommandOf(
            SetContext(state.getContext<ActivatePlayerContext>().copy(markActionAsUsed = !context.actionAborted)),
            RemoveContext<MultipleBlockContext>()
        )
    }

    /**
     * Node responsible for selecting targets and optionally deselecting them again. Once
     * both targets are selected, we move to the next step using a `Confirm` action.
     *
     * We could also choose to transition automatically, but currently let this be up to
     * the UI layer, as it can then choose to automatically respond with Confirm rather than showing
     * it to the user.
     */
    object SelectDefenderOrAbortActionOrContinueBlock: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activeTeamOrThrow()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MultipleBlockContext>()
            val attacker = context.attacker
            val defender1 = context.defender1
            val defender2 = context.defender2

            val eligibleDefenders: List<GameActionDescriptor> =
                attacker.coordinates.getSurroundingCoordinates(rules)
                    .filter { state.field[it].isOccupied() }
                    .filter { state.field[it].player!!.let { player ->
                        player.team != attacker.team &&
                            player != defender1 &&
                            player != defender2
                    }}
                    .map { SelectPlayer(state.field[it].player!!) }


            val deselectCommands = listOf(
                DeselectPlayer(attacker),
                if (defender1 != null) DeselectPlayer(defender1) else null,
                if (defender2 != null) DeselectPlayer(defender2) else null,
                CancelWhenReady,
            )

            // If both targets are selected, blocker can continue the multiblock.
            // Otherwise, they must continue to make selections.
            return if (defender1 != null && defender2 != null) {
                (deselectCommands + ConfirmWhenReady).filterNotNull()
            } else {
                (deselectCommands + eligibleDefenders).filterNotNull()
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return when (action) {
                Cancel -> {
                    compositeCommandOf(
                        SetContext(state.getContext<ActivatePlayerContext>().copy(markActionAsUsed = false)),
                        ExitProcedure()
                    )
                }
                is Confirm -> {
                    if (!getAvailableActions(state, rules).contains(ConfirmWhenReady)) {
                        INVALID_ACTION(action)
                    }
                    compositeCommandOf(
                        SetContext(context.copy(activeDefender = null, actionAborted = false)),
                        GotoNode(RollDiceForTarget1)
                    )
                }
                is PlayerDeselected -> {
                    // If the attacker is deselected, it is a sign to cancel the block
                    // if a defender is deselected, that is fine, just update the context and redo this logic.
                    when (val player = action.getPlayer(state)) {
                        context.attacker -> ExitProcedure()
                        else -> {
                            val updatedContext = context.copyAndUnsetDefender(player)
                            compositeCommandOf(
                                SetContext(updatedContext),
                                GotoNode(SelectDefenderOrAbortActionOrContinueBlock)
                            )
                        }
                    }
                }
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    when {
                        context.defender1 == null -> {
                            compositeCommandOf(
                                SetContext(context.copy(
                                    defender1 = player,
                                    defender1Location = player.coordinates,
                                    activeDefender = 0)
                                ),
                                GotoNode(SelectBlockTypeAgainstSelectedDefender)
                            )
                        }
                        context.defender2 == null -> {
                            compositeCommandOf(
                                SetContext(context.copy(
                                    defender2 = player,
                                    defender2Location = player.coordinates,
                                    activeDefender = 1
                                )),
                                GotoNode(SelectBlockTypeAgainstSelectedDefender)
                            )
                        }
                        else -> INVALID_ACTION(action)
                    }
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Once a target has been selected, we need to set the block type for the given player.
     * To give the UI more flexibility, we also allow the player to be deselected at this step,
     * which will remove the player as a target.
     */
    object SelectBlockTypeAgainstSelectedDefender: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activeTeamOrThrow()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val attacker = state.getContext<MultipleBlockContext>().attacker
            val defender = state.getContext<MultipleBlockContext>().getActiveDefender() ?: INVALID_GAME_STATE("No active defender: ${state.getContext<MultipleBlockContext>()}")
            val availableBlockTypes = BlockAction.getAvailableBlockType(attacker, true)
            return listOf(
                SelectBlockType(availableBlockTypes),
                DeselectPlayer(defender),
            )
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return when (action) {
                is PlayerDeselected -> {
                    val player = action.getPlayer(state)
                    if (player != context.getActiveDefender()) {
                        INVALID_ACTION(action, "Player is not the active defender: ${player.id} vs. ${context.getActiveDefender()?.id}")
                    }
                    compositeCommandOf(
                        SetContext(context.copyAndUnsetDefender(player)),
                        GotoNode(SelectDefenderOrAbortActionOrContinueBlock)
                    )
                }
                else -> {
                    checkTypeAndValue<BlockTypeSelected>(state, action) { typeSelected ->
                        val type = typeSelected.type
                        val updatedContext = context.copyAndSetBlockTypeForActiveDefender(type)
                        compositeCommandOf(
                            SetContext(updatedContext),
                            GotoNode(SelectDefenderOrAbortActionOrContinueBlock)
                        )
                    }
                }
            }
        }
    }

    /**
     * Node responsible for rolling the dice for the first target. Which dice to roll depends
     * on the type of block.
     */
    object RollDiceForTarget1: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return getEnterBlockTypeNodeCommands(state, 0)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<MultipleBlockContext>()
            return context.getRollDiceProcedure()
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val leaveCommands = getLeaveBlockTypeNodeCommands(state)
            return compositeCommandOf(
                leaveCommands,
                GotoNode(RollDiceForTarget2)
            )
        }
    }

    /**
     * Node responsible for rolling the dice for the second target. Which dice to roll depends
     * on the type of block.
     */
    object RollDiceForTarget2: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return getEnterBlockTypeNodeCommands(state, 1)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<MultipleBlockContext>()
            return context.getRollDiceProcedure()
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val leaveCommands = getLeaveBlockTypeNodeCommands(state)
            return compositeCommandOf(
                leaveCommands,
                GotoNode(RerollAllDice)
            )
        }
    }

    /**
     * With both blocks having rolled their first set of dice, it is now possible
     * to choose which dice to reroll. When using Multiple Block, it should be possible
     * to see both rolls at the same time and choose rerolls from both.
     */
    object RerollAllDice: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = MultipleBlockRerollDice
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SelectBlockResults)
        }
    }

    /**
     * After rerolls, the active coach now needs to select the results of
     * each block that they should resolve. This is done at the same time.
     */
    object SelectBlockResults: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = MultipleBlockChoseResults
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SelectPlayerToResolve)
        }
    }

    /**
     * After choosing the final dice results, choose which player to resolve
     * first.
     */
    object SelectPlayerToResolve: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activeTeamOrThrow()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MultipleBlockContext>()
            return listOf(
                SelectPlayer(listOf(context.defender1!!.id, context.defender2!!.id)),
            )
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkTypeAndValue<PlayerSelected>(state, action) { playerSelected ->
                val context = state.getContext<MultipleBlockContext>()
                val activeDefenderIndex = when (val player = playerSelected.getPlayer(state)) {
                    context.defender1 -> 0
                    context.defender2 -> 1
                    else -> INVALID_GAME_STATE("No matching defender: $player")
                }
                compositeCommandOf(
                    SetContext(context.copy(activeDefender = activeDefenderIndex)),
                    GotoNode(ResolveFirstPlayer)
                )
            }
        }
    }

    /**
     * Resolve the result of the first player, up until the point of rolling
     * for
     *
     * Fo
     *
     *
     *
     * including rolling for injuries.
     * But do not use the apothecary yet. Instead, the injury is placed in the
     * "injury pool".
     *
     * If it results in a push, we wil only resolve the push chain up until
     * all players have been pushed, but before rolling for anything like
     * trapdoors, bounces etc. THis will happen after we resolved the second
     * block in a similar manner.
     *
     * If resolving this block results in a turnover for the blocking player,
     * the turn-over is delayed until the end of the action. E.g., the blocking
     * player does not lose access to skills because they roll a Player Down
     * before rolling for Stab on the second target.
     */
    object ResolveFirstPlayer: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return getEnterBlockTypeNodeCommands(state, context.activeDefender!!)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<MultipleBlockContext>()
            return context.getResolveBlockResultProcedure()
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            val nextDefender = when (context.activeDefender) {
                0 -> 1
                1 -> 0
                else -> throw IllegalArgumentException("Invalid active defender: ${context.activeDefender}")
            }
            val contextClass = context.getContextForCurrentBlock()::class
            val updateMultipleBlockContextCommand = context.updateWithLatestBlockTypeContext(state)
            return compositeCommandOf(
                RemoveContext(contextClass),
                SetTurnOver(null),
                updateMultipleBlockContextCommand,
                SetContextProperty(MultipleBlockContext::activeDefender, context, nextDefender),
                SetContextProperty(MultipleBlockContext::postponeTurnOver, context, state.turnOver),
                GotoNode(ResolveSecondPlayer)
            )
        }
    }

    /**
     * Resolve the result of the second player, including rolling for injuries.
     * But do not use the apothecary yet.
     */
    object ResolveSecondPlayer: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return getEnterBlockTypeNodeCommands(state, context.activeDefender!!)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<MultipleBlockContext>()
            return context.getResolveBlockResultProcedure()
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            val contextClass = context.getContextForCurrentBlock()::class
            val updatedMultiBlockContextCommand = context.updateWithLatestBlockTypeContext(state)
            return compositeCommandOf(
                RemoveContext(contextClass),
                SetTurnOver(context.postponeTurnOver),
                updatedMultiBlockContextCommand,
                SetContextProperty(MultipleBlockContext::activeDefender,  context, null),
                GotoNode(ResolveInjuries)
            )
        }
    }

    /**
     * Fully resolve the "injury pool", i.e., choose to allow it or use
     * skills or apothecaries to modify the result.
     */
    object ResolveInjuries: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = MultipleBlockResolveInjuries
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ResolveBlock1Trapdoors)
        }
    }

    // TODO
    object ResolveBlock1Trapdoors: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return compositeCommandOf(
                SetContextProperty(MultipleBlockContext::activeDefender,  context, 1),
                GotoNode(ResolveBlock2Trapdoors)
            )
        }
    }

    // TODO
    object ResolveBlock2Trapdoors: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return compositeCommandOf(
                SetContextProperty(MultipleBlockContext::activeDefender,  context, 0),
                GotoNode(CheckBlock1Touchdowns)
            )
        }
    }

    /**
     * During a Multiple Block, it is only possible for touchdowns to happen before ball events
     * if there is a push involved (since follow-up is not allowed). We check the touchdown through
     * the entire push chain starting with the defender. If a touchdown is detected, we skip checking
     * other players.
     */
    // TODO Losts of overlap with checking scoring in PushStepResolveSingleBlockPushChain. Can they be combined somehow?
    object CheckBlock1Touchdowns: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<MultipleBlockContext>().defender1PushChain
            val pushStep = context?.pushChain?.firstOrNull { checkPlayerForTouchdown(it) }
            return if (pushStep == null) CheckBlock2Touchdowns else null
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>().defender1PushChain!!
            val push = context.pushChain.first { checkPlayerForTouchdown(it) }
            val player = push.pushee
            return SetContext(ScoringATouchDownContext(player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context =  state.getContext<MultipleBlockContext>().defender1PushChain!!
            // Mark the step we just checked as complete.
            val pushData: PushContext.PushData = context.pushChain.firstNotNullOf { el ->
                if (checkPlayerForTouchdown(el)) el else null
            }
            return compositeCommandOf(
                RemoveContext<ScoringATouchDownContext>(),
                SetContextProperty(PushContext.PushData::checkedForScoringAfterTrapdoors, pushData, true),
                GotoNode(CheckBlock2Touchdowns)
            )
        }
    }

    object CheckBlock2Touchdowns: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<MultipleBlockContext>().defender2PushChain
            val pushStep = context?.pushChain?.firstOrNull { checkPlayerForTouchdown(it) }
            return if (pushStep == null) ResolveBlock1DefendersBallEvents else null
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>().defender2PushChain!!
            val push = context.pushChain.first { checkPlayerForTouchdown(it) }
            val player = push.pushee
            return SetContext(ScoringATouchDownContext(player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context =  state.getContext<MultipleBlockContext>().defender2PushChain!!
            // Mark the step we just checked as complete.
            val pushData: PushContext.PushData = context.pushChain.firstNotNullOf { el ->
                if (checkPlayerForTouchdown(el)) el else null
            }
            return compositeCommandOf(
                RemoveContext<ScoringATouchDownContext>(),
                SetContextProperty(PushContext.PushData::checkedForScoringAfterTrapdoors, pushData, true),
                GotoNode(ResolveBlock1DefendersBallEvents)
            )
        }
    }

    object ResolveBlock1DefendersBallEvents: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // Figure out if this is a push block or the defender went down in the square
            val context = state.getContext<MultipleBlockContext>()
            val pushData = context.defender1PushChain
            val handleBallCommand = if (pushData == null) {
                // If there is no push, the defender could only be Knocked Down in their starting square
                checkBouncingBallInSquare(state, context.defender1Location!!)
            } else {
                resolveNextBallEventsInPushChain(pushData)
            }
            return handleBallCommand ?: compositeCommandOf(
                SetContext(context.copy(defender1BallsHandled = true)),
                GotoNode(ResolveBlock2DefendersBallEvents)
            )
        }
    }

    object ResolveBlock2DefendersBallEvents: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // Figure out if this is a push block or the defender went down in the square
            val context = state.getContext<MultipleBlockContext>()
            val pushData = context.defender2PushChain
            val handleBallCommand = if (pushData == null) {
                // If there is no push, the defender could only be Knocked Down in their starting square
                checkBouncingBallInSquare(state, context.defender2Location!!)
            } else {
                resolveNextBallEventsInPushChain(pushData)
            }
            return handleBallCommand ?: compositeCommandOf(
                SetContext(context.copy(defender2BallsHandled = true)),
                ExitProcedure()
            )
        }
    }

    object BounceCurrentBall : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return when (context.defender1BallsHandled) {
                false -> GotoNode(ResolveBlock1DefendersBallEvents)
                true -> GotoNode(ResolveBlock2DefendersBallEvents)
            }
        }
    }

    object ThrowInCurrentBall: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            return SetContext(ThrowInContext(
                ball,
                ball.outOfBoundsAt!!
            ))
        }
        override fun getChildProcedure(state: Game, rules: Rules) = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            val nextNodeCommand = when (context.defender1BallsHandled) {
                false -> GotoNode(ResolveBlock1DefendersBallEvents)
                true -> GotoNode(ResolveBlock2DefendersBallEvents)
            }
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                nextNodeCommand
            )
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // HELPER FUNCTIONS

    /**
     * Returns the commands that set the correct contexts just before calling down into
     * a sub procedure for a specific block type.
     */
    private fun getEnterBlockTypeNodeCommands(state: Game, activeDefender: Int): Command {
        val context = state.getContext<MultipleBlockContext>()
        val updatedContext = context.copy(activeDefender = activeDefender)
        return compositeCommandOf(
            SetContext(updatedContext),
            SetContext(updatedContext.getContextForCurrentBlock())
        )
    }

    /**
     * Return the commands needed to clear the context from a specific sub procedure,
     * as well as making sure that [MultipleBlockContext] is updated.
     */
    private fun getLeaveBlockTypeNodeCommands(state: Game): Command {
        val context = state.getContext<MultipleBlockContext>()
        val updateMultipleBlockContextCommand = context.updateWithLatestBlockTypeContext(state)
        val contextClass = context.getContextForCurrentBlock()::class
        return compositeCommandOf(
            RemoveContext(contextClass),
            updateMultipleBlockContextCommand
        )
    }

    private fun checkPlayerForTouchdown(step: PushContext.PushData): Boolean {
        val game = step.pushee.team.game
        val rules = game.rules
        return !game.isTurnOver()
            && rules.isStanding(step.pushee)
            && step.pushee.hasBall()
            && !step.checkedForScoringAfterTrapdoors
    }

    /**
     * Check if there is a bouncing ball in a single square, used for blocks that result in either
     * the attacker or defender being Knocked Over in their square (and not being pushed).
     *
     * Returns `null` if no ball is found, if a ball is found, the [Command] to transition to the next
     * node is returned.
     */
    private fun checkBouncingBallInSquare(state: Game, coordinates: FieldCoordinate): Command? {
        val square = state.field[coordinates]
        return if (square.balls.isNotEmpty()) {
            val ball = square.balls.single()
            if (ball.state == BallState.BOUNCING) {
                compositeCommandOf(
                    SetCurrentBall(ball),
                    GotoNode(BounceCurrentBall)
                )
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun resolveNextBallEventsInPushChain(context: PushContext): Command? {
        val state = context.firstPusher.team.game

        // First run through squares we know are on the field
        var selectedBall: Ball? = null
        for (step in 0..< context.pushChain.size) {
            val square = context.pushChain[step].to!!
            val ball = state.field[square].balls.singleOrNull()
            if (ball != null && ball.state == BallState.BOUNCING) {
                selectedBall = ball
                break
            }
        }
        // Then Check throw-in
        if (selectedBall == null && context.pushChain.last().to == FieldCoordinate.OUT_OF_BOUNDS) {
            val ball = state.balls.singleOrNull { it.state == BallState.OUT_OF_BOUNDS }
            selectedBall = ball
        }

        // Finally check the attackers location (if the choose to follow up, otherwise they coul not have lost the ball)
        if (selectedBall == null && context.followsUp) {
            val ball = state.field[context.pushChain.first().from].balls.singleOrNull()
            if (ball != null) {
                if (ball.state != BallState.BOUNCING) INVALID_GAME_STATE("Unexpected ball state: ${ball.state}")
                selectedBall = ball
            }
        }

        return if (selectedBall != null) {
            buildCompositeCommand {
                add(SetCurrentBall(selectedBall))
                val nextNode =
                    when (selectedBall.state) {
                        BallState.BOUNCING -> BounceCurrentBall
                        BallState.OUT_OF_BOUNDS -> ThrowInCurrentBall
                        else -> INVALID_GAME_STATE("Unexpected ball state: ${selectedBall.state}")
                    }
                add(GotoNode(nextNode))
            }
        } else {
            null
        }
    }
}

