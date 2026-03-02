package com.jervisffb.engine.rules.bb2025.procedures.actions.block

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
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
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.BB2025MultipleBlockContext
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.context.ScoringATouchDownContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.multipleblock.MultipleBlockChoseResults
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.multipleblock.MultipleBlockRerollDice
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.multipleblock.MultipleBlockResolveInjuries
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.ThrowIn
import com.jervisffb.engine.rules.common.procedures.ThrowInContext
import com.jervisffb.engine.rules.common.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * This procedure is responsible for handling Multiple Block as described on
 * page 132 in the BB2025 rulebook.
 *
 * Designer's Commentary (BB2020):
 * Even though these clarifications are for BB2020, we will carry them over to
 * BB2025, this that behavior is not defined by those rules either.
 *
 * - When using Grab, the blocked players starting square are treated as being
 *   Occupied, regardless of who is moved first.
 * - Blocked players cannot offer assist to each other.
 *
 * Developer's Commentary:
 * Multiple Block is a challenge to implement as it breaks the flow of multiple
 * skills and actions. It is also very vaguely worded, and the description
 * doesn't cover a lot of edge cases.
 *
 * For that reason, it is up to [BB2025MultipleBlockContext] to handle tracking
 * and resetting of the [Skill.used] state of skills used during a Multiple
 * Block. We can only rely on [Skill.resetAt] for skills that can only be used
 * once across blocks.
 *
 * It is also especially unclear how "performed simultaneously" should be
 * interpreted. A full discussion on how to interpret the rules for Multiple
 * Block is in `rules-faq-bb2025.md`, so here we will just focus on the
 * implementation.
 *
 * The sequence of events in a Multiple Block is resolved in the following
 * order:
 *
 * 1. Select the Multiple Block Special Action.
 *
 * 2. Select Target 1. Only a normal block is allowed.
 *
 * 3. Select Target 2. Only a normal block is allowed.
 *
 * 4. Roll block dice for Target 1.
 *
 * 5. Roll block dice for Target 2.
 *
 * 6. Choose a reroll type or accept roll for all dice against both targets. This
 *    is done at the same time.
 *    - Brawler
 *    - Hatred
 *    - Pro
 *    - Savage Blow
 *    - Team Reroll
 *
 * 7. Use skills that modify the final results. This is done at the same time
 *    for both targets.
 *     - Juggernaut
 *
 * 8. Select the final dice results for both targets. This is done at the same
 *    time.
 *
 * 9. Choose which target to resolve block for first, now named Block A and
 *    Block B.
 *
 * 10. Resolve Block A. What happens, differs slightly depending on the block
 *     dice:
 *     - PlayerDown
 *         - Mark Attacker as being Knocked Down, but player is not Knocked Down
 *           yet.
 *         - If Attacker is holding the ball, mark it as loose and bouncing, but do
 *           not roll for bouncing yet.
 *
 *     - BothDown
 *         - Handle skills that trigger on Both Down. First Defender, then
 *           Attacker.
 *         - Mark Defender/Attacker as being Knocked Down, but they are not
 *           Knocked Down yet. No skills that trigger on being Knocked Down are
 *           applied yet.
 *         - If Defender is holding the ball, mark it as loose and bouncing, but do
 *           not roll for bouncing yet.
 *         - If Attacker is holding the ball, mark it as loose and bouncing, but do
 *           not roll for bouncing yet.
 *
 *     - Push Back
 *         - Create the Push Chain, i.e., determine were all pushed players will
 *           be pushed to. Using skills as appropriate.
 *             - Sidestep
 *             - Grab
 *             - Stand Firm
 *         - Do not move any players yet.
 *         - Do not roll any dice yet.
 *
 *     - Stumble
 *         - Choose to use Tackle and Dodge.
 *         - Stumble is converted to either a Push Back or Pow and will use their
 *           resolution order.
 *
 *     - Pow
 *         - Create Push Chain. See Push Back for details.
 *         - Mark Defender as being Knocked Down, but they are not Knocked Down
 *           yet. No skills that trigger on being Knocked Down are applied yet.
 *         - If Defender is holding the ball, mark it as loose and bouncing, but do
 *           not roll for bouncing yet.
 *
 * 11. Resolve Block B, using the same steps as Block A. No dice have been rolled
 *     yet.
 *
 * 12. Move all Players in Push Chain A (if any).
 *
 * 12. Move all Players in Push Chain B (if any). Note, a Push Chain is defined by
 *     the squares affected and not players, so by resolving Push Chain A first,
 *     some players might be moved twice or end up in a position not intended
 *     when only looking at a single Push Chain.
 *
 * 13. Follow-up is not applicable when using Multiple Block.
 *
 * 14. Decide if Strip Ball is used against Defender A.
 *
 * 15. Decide if Strip Ball is used against Defender B.
 *
 * 16. Resolve Push Chain for Block A. Look at one square at a time, starting
 *     from Defender A and all the way through the Push Chain. We will check for
 *     touchdowns at every step, but the entire chain must still be resolved
 *     regardless of a touchdown being scored in the middle. For each square
 *     resolve events in the same order as for single blocks:.
 *     - Check for a loose ball. If found, it will bounce.
 *     - If standing on a Trapdoor. Roll to see if the player falls through.
 *       If Yes:
 *         - Roll for Injury immediately and resolve it
 *         - If holding a ball, resolve bouncing from the square.
 *         - Will not suffer the consequences of being Knocked Down (if any).
 *     - If pushed into the crowd:
 *         - Roll for Injury immediately and resolve it.
 *         - Throw the ball back in.
 *     - Check for Touchdown from the player standing in the square.
 *
 * 17. Resolve Push Chain for Block B. Using the same sequence as for Block A.
 *
 * 18. Resolve Events in Attacker's square, if not done already as part of a Push
 *     Chain. This concludes the Pushed Back sequence, and we can continue with the
 *     Knocked Down
 *
 * 19. Resolve skills for Block A that trigger on Knocked Down.
 *     - Safe Pair of Hands
 *     - Steady Footing
 *     - Saboteur
 *
 * 20. Resolve skills for Block B that trigger on Knocked Down.
 *
 * 21. Put Attacker and Defenders Prone as needed.
 *
 * 22. Roll Armour and Injury for Defender A. If injuries, do not resolve
 *     completely, but add to a Defender Injury Pool instead.
 *
 * 23. Roll Armour and Injury for Defender B. If injuries, do not resolve
 *     completely, but add to a Defender Injury Pool instead.
 *
 * 24. Roll Armour and Injury for Attacker A, first for Block A, then for Block B.
 *     Injuries are added to the Attacker Injury Pool.
 *
 * 25. Resolve Defender Injury Pool, ie. resolve apothecary and regen both
 *     players at the same time
 *
 * 26. Resolve Attacker Injury Pool, ie. resolve apothecary and regen for both
 *     injuries at the same time.
 *
 * 27. If defenders or attackers dropped a ball as part of being Knocked Down.
 *     Bounce now: Defender A, Defender B, then Attacker.
 *
 * 27. If Attacker is still standing, holding the ball. Check for a Touchdown.
 */
object MultipleBlockAction: Procedure() {
    override val initialNode: Node = SelectDefenderOrAbortActionOrContinueBlock
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val activeContext = state.getContext<ActivatePlayerContext>()
        return SetContext(
            BB2025MultipleBlockContext(
                attacker = activeContext.player
            )
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<BB2025MultipleBlockContext>()
        return compositeCommandOf(
            SetContext(state.getContext<ActivatePlayerContext>().copy(markActionAsUsed = !context.actionAborted)),
            RemoveContext<BB2025MultipleBlockContext>()
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
            val context = state.getContext<BB2025MultipleBlockContext>()
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
            val context = state.getContext<BB2025MultipleBlockContext>()
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
                                SetContext(
                                    context.copy(
                                        defender1 = player,
                                        defender1Location = player.coordinates,
                                        activeDefender = 0
                                    )
                                ),
                                GotoNode(SelectBlockTypeAgainstSelectedDefender)
                            )
                        }
                        context.defender2 == null -> {
                            compositeCommandOf(
                                SetContext(
                                    context.copy(
                                        defender2 = player,
                                        defender2Location = player.coordinates,
                                        activeDefender = 1
                                    )
                                ),
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
     * In BB2020, Multiple Block could be used with other Special Actions like Stab.
     * This has been removed in BB2025, so now only the standard block can be used
     * against both selected targets.
     *
     * We still handle this in a separate Node to make it easy to refactor if it changes
     * in the future.
     */
    object SelectBlockTypeAgainstSelectedDefender: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BB2025MultipleBlockContext>()
            val type = BlockType.STANDARD
            val updatedContext = context.copyAndSetBlockTypeForActiveDefender(type)
            return compositeCommandOf(
                SetContext(updatedContext),
                GotoNode(SelectDefenderOrAbortActionOrContinueBlock)
            )
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
            val context = state.getContext<BB2025MultipleBlockContext>()
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
            val context = state.getContext<BB2025MultipleBlockContext>()
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
            val context = state.getContext<BB2025MultipleBlockContext>()
            return listOf(
                SelectPlayer.Companion.fromPlayers(listOf(context.defender1!!, context.defender2!!)),
            )
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<PlayerSelected>(action) { playerSelected ->
                val context = state.getContext<BB2025MultipleBlockContext>()
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
     * Resolve the result of the first player
     *
     *
     *
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
            val context = state.getContext<BB2025MultipleBlockContext>()
            return getEnterBlockTypeNodeCommands(state, context.activeDefender!!)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<BB2025MultipleBlockContext>()
            return context.getResolveBlockResultProcedure()
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BB2025MultipleBlockContext>()
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
                SetContextProperty(BB2025MultipleBlockContext::activeDefender, context, nextDefender),
                SetContextProperty(BB2025MultipleBlockContext::postponeTurnOver, context, state.turnOver),
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
            val context = state.getContext<BB2025MultipleBlockContext>()
            return getEnterBlockTypeNodeCommands(state, context.activeDefender!!)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<BB2025MultipleBlockContext>()
            return context.getResolveBlockResultProcedure()
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BB2025MultipleBlockContext>()
            val contextClass = context.getContextForCurrentBlock()::class
            val updatedMultiBlockContextCommand = context.updateWithLatestBlockTypeContext(state)
            return compositeCommandOf(
                RemoveContext(contextClass),
                SetTurnOver(context.postponeTurnOver),
                updatedMultiBlockContextCommand,
                SetContextProperty(BB2025MultipleBlockContext::activeDefender, context, null),
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
            val context = state.getContext<BB2025MultipleBlockContext>()
            return compositeCommandOf(
                SetContextProperty(BB2025MultipleBlockContext::activeDefender, context, 1),
                GotoNode(ResolveBlock2Trapdoors)
            )
        }
    }

    // TODO
    object ResolveBlock2Trapdoors: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BB2025MultipleBlockContext>()
            return compositeCommandOf(
                SetContextProperty(BB2025MultipleBlockContext::activeDefender, context, 0),
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
            val context = state.getContext<BB2025MultipleBlockContext>().defender1PushChain
            val pushStep = context?.pushChain?.firstOrNull { checkPlayerForTouchdown(it) }
            return if (pushStep == null) CheckBlock2Touchdowns else null
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BB2025MultipleBlockContext>().defender1PushChain!!
            val push = context.pushChain.first { checkPlayerForTouchdown(it) }
            val player = push.pushee
            return SetContext(ScoringATouchDownContext(player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context =  state.getContext<BB2025MultipleBlockContext>().defender1PushChain!!
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
            val context = state.getContext<BB2025MultipleBlockContext>().defender2PushChain
            val pushStep = context?.pushChain?.firstOrNull { checkPlayerForTouchdown(it) }
            return if (pushStep == null) ResolveBlock1DefendersBallEvents else null
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BB2025MultipleBlockContext>().defender2PushChain!!
            val push = context.pushChain.first { checkPlayerForTouchdown(it) }
            val player = push.pushee
            return SetContext(ScoringATouchDownContext(player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context =  state.getContext<BB2025MultipleBlockContext>().defender2PushChain!!
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
            val context = state.getContext<BB2025MultipleBlockContext>()
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
            val context = state.getContext<BB2025MultipleBlockContext>()
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
            val context = state.getContext<BB2025MultipleBlockContext>()
            val nextNode = when (context.defender1BallsHandled) {
                false -> ResolveBlock1DefendersBallEvents
                true -> ResolveBlock2DefendersBallEvents
            }
            return compositeCommandOf(
                SetCurrentBall(null),
                GotoNode(nextNode)
            )
        }
    }

    object ThrowInCurrentBall: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            return SetContext(
                ThrowInContext(
                    ball,
                    ball.outOfBoundsAt!!
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules) = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BB2025MultipleBlockContext>()
            val nextNodeCommand = when (context.defender1BallsHandled) {
                false -> GotoNode(ResolveBlock1DefendersBallEvents)
                true -> GotoNode(ResolveBlock2DefendersBallEvents)
            }
            return compositeCommandOf(
                SetCurrentBall(null),
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
        val context = state.getContext<BB2025MultipleBlockContext>()
        val updatedContext = context.copy(activeDefender = activeDefender)
        return compositeCommandOf(
            SetContext(updatedContext),
            SetContext(updatedContext.getContextForCurrentBlock())
        )
    }

    /**
     * Return the commands needed to clear the context from a specific sub procedure,
     * as well as making sure that [BB2025MultipleBlockContext] is updated.
     */
    private fun getLeaveBlockTypeNodeCommands(state: Game): Command {
        val context = state.getContext<BB2025MultipleBlockContext>()
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
        if (selectedBall == null && context.pushChain.last().to?.isOutOfBounds(state.rules) == true) {
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
