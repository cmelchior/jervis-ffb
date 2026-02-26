package com.jervisffb.engine.rules.bb2025.procedures.actions.block.push

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContextListItem
import com.jervisffb.engine.commands.context.ReplaceContextListItem
import com.jervisffb.engine.commands.context.SetContextProperty
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.BB2025MultipleBlockContext
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025PushBack
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.MultipleBlockAction
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Procedure for creating the first part of a Push Chain, i.e., the part where
 * we define the skills used at each step and the direction pushed back. No
 * players must be moved, nor any dice rolled (that are not a direct result
 * of any skills used).
 *
 * A Pushback is split into multiple phases to support both normal blocks and
 * Multiple Block as their order of resolution differs.
 *
 * See [BB2025PushBack] and [MultipleBlockAction] for more details on each.
 *
 * Developer's Commentary:
 * When determining a step in the push chain, it involves a number of skills
 * and interactions that are not straight-forward. The exact sequence of these
 * is described below with Player A = pusher and Player B = pushee
 *
 * 1. Player A starts blitz or block and must decide to use Juggernaut or not
 *    (before the push start).
 *    a. Juggernaut prevents the use of Stand Firm in chain pushes (NAF Ruling).
 *    b. Juggernaut prevents the use of Fend when following up.
 *
 * 2. Player B must decide whether to use Stand Firm. Page 136 in the BB2025
 *    rulebook.
 *    a. Cannot be used if Player A used Juggernaut.
 *    b. Can be used on chain pushes.
 *
 * 3. Player A must decide whether to use Grab. Page 128 in the BB2025 rulebook.
 *    a. Cannot be used while blitzing.
 *    b. Cannot be used on chain pushes.
 *    c. Cannot be used if no unoccupied squares exist adjacent to Player B.
 *
 * 4. Player B must decide whether to use Sidestep. Page 135 in the BB2025
 *    rulebook.
 *.   a. Cannot be used if Player A used Grab.
 *    b. Cannot be used if no unoccupied squares exist adjacent to Player B.
 */
object CreatePushChainStep: Procedure() {
    override val initialNode: Node = DecideToUseJuggernaut
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val blockContext = state.getContext<BlockContext>()
        return if (blockContext.isUsingMultiBlock) {
            // For Multiple Block, the different phases are run in lock-step, which
            // is controlled by the MultipleBlockAction
            val blockContext = state.getContext<BlockContext>()
            val pushContext = state.getContext<PushContext>()
            buildCompositeCommand {
                if (blockContext.isUsingMultiBlock) {
                    val multipleBlockContext = state.getContext<BB2025MultipleBlockContext>()
                    val property = if (multipleBlockContext.activeDefender == 0) {
                        BB2025MultipleBlockContext::defender1PushChain
                    } else {
                        BB2025MultipleBlockContext::defender2PushChain
                    }
                    add(SetContextProperty(property, multipleBlockContext, pushContext))
                }
            }
        } else {
            null
        }
    }

    // TODO Is this where we decide on Juggernaut? Or should we somehow make it a node outside
    //  the push chain (since it doesn't apply to chain pushes)
    // TODO Juggernaut probably doesn't apply to chain pushes?
    object DecideToUseJuggernaut: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().pushChain.last().pusher.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PushContext>()
            val hasJuggernaut = false // How to check?
            val canUseJuggernaut = !context.isAttackerUsingJuggernaut
            return when (hasJuggernaut && canUseJuggernaut) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Confirm -> {
                    val context = state.getContext<PushContext>()
                    val pushData = context.pushChain.last()
                    return compositeCommandOf(
                        // SetContextProperty(PushContext.PushData::usingJuggernaut, pushData, true),
                        GotoNode(DecideToUseStandFirm)
                    )
                }
                Cancel, Continue -> {
                    GotoNode(DecideToUseStandFirm)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object DecideToUseStandFirm: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? {
            return state.getContext<PushContext>().pushee().team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PushContext>()
            val hasStandFirm = context.pushee().isSkillAvailable(SkillType.STAND_FIRM)
            val isUsingJuggernaut = context.isAttackerUsingJuggernaut
            val canUseStandFirm = hasStandFirm && !isUsingJuggernaut
            return when (canUseStandFirm) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Confirm -> {
                    val context = state.getContext<PushContext>()
                    val pushData = context.pushChain.last()
                    compositeCommandOf(
                        ReportSkillUsed(pushData.pushee, SkillType.STAND_FIRM),
                        SetContextProperty(PushContext.PushData::usedStandFirm, pushData, true),
                        GotoNode(DecideToUseGrab)
                    )
                }
                Cancel, Continue -> {
                    GotoNode(DecideToUseGrab)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object DecideToUseGrab: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().pushChain.first().pusher.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val pushContext = state.getContext<PushContext>()
            val blockContext = state.getContext<BlockContext>()
            val hasGrab = pushContext.firstPusher.isSkillAvailable(SkillType.GRAB)
            val isFirstBlock = (pushContext.pushChain.size == 1)
            val isBlitz = blockContext.isBlitzing
            val isStandFirmUsed = pushContext.pushChain.first().usedStandFirm
            val hasValidGrabTargets = pushContext.firstPushee.coordinates
                .getSurroundingCoordinates(rules, includeOutOfBounds = false)
                .any { state.field[it].isUnoccupied() }
            val canUseGrab = hasGrab && isFirstBlock && !isBlitz && !isStandFirmUsed && hasValidGrabTargets
            return when (canUseGrab) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Confirm -> {
                    val context = state.getContext<PushContext>()
                    val pushData = context.pushChain.last()
                    compositeCommandOf(
                        ReportSkillUsed(pushData.pusher, SkillType.GRAB),
                        SetContextProperty(PushContext.PushData::usedGrab, pushData, true),
                        ReplaceContextListItem(context.pushChain, pushData),
                        GotoNode(DecideToUseSidestep)
                    )
                }
                Cancel, Continue -> {
                    GotoNode(DecideToUseSidestep)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object DecideToUseSidestep: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().pushChain.last().pushee.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PushContext>().pushChain.last()
            val hasSidestep = context.pushee.isSkillAvailable(SkillType.SIDESTEP)
            val validSideStepTargets = context.pushee.coordinates
                .getSurroundingCoordinates(rules, includeOutOfBounds = false)
                .any { state.field[it].isUnoccupied() }
            val isGrabUsed = context.usedGrab
            val isStandFirmUsed = context.usedStandFirm
            val canUseSidestep = hasSidestep && !isGrabUsed && !isStandFirmUsed && validSideStepTargets
            return when (canUseSidestep) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Confirm -> {
                    val context = state.getContext<PushContext>()
                    val pushData = context.pushChain.last()
                    compositeCommandOf(
                        ReportSkillUsed(pushData.pushee, SkillType.SIDESTEP),
                        SetContextProperty(PushContext.PushData::usedSideStep, pushData, true),
                        GotoNode(SelectPushDirection)
                    )
                }
                Cancel, Continue -> {
                    GotoNode(SelectPushDirection)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    // Select where to push the player
    object SelectPushDirection: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val context = state.getContext<PushContext>()
            return if (context.pushChain.last().usedSideStep) {
                context.pushChain.last().pushee.team
            } else {
                context.firstPusher.team
            }
        }

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val pushContext = state.getContext<PushContext>()
            val lastPushInChain = pushContext.pushChain.last()

            // If Stand Firm is used, the Push Chain just stops in its current state,
            if (lastPushInChain.usedStandFirm) {
                return listOf(ContinueWhenReady)
            }

            // Otherwise, we need to find the squares the player can be pushed back into
            val pushOptions = if (lastPushInChain.usedSideStep || lastPushInChain.usedGrab) {
                // Should be safe as Grab only works cannot be used on chain pushes.
                lastPushInChain.pushee.coordinates.getSurroundingCoordinates(rules).toSet()
            } else {
                rules.getPushOptions(lastPushInChain.pusher, lastPushInChain.pushee)
            }

            // Calculate all push options taking into account a chain push in progress.
            // In chain pushes, only the square of Player B could be empty, but it might
            // not be in case of a circular chain.
            val emptyFields = getEmptySquaresForPushing(pushContext, pushOptions, state)
            return listOf(
                if (emptyFields.isNotEmpty()) {
                    SelectDirection(
                        origin = lastPushInChain.pushee.coordinates,
                        directions = emptyFields.map {Direction.from(lastPushInChain.pushee.coordinates, it) }
                    )
                } else {
                    SelectDirection(
                        origin = lastPushInChain.pushee.coordinates,
                        directions = pushOptions.map {Direction.from(lastPushInChain.pushee.coordinates, it) }
                    )
                }
            )
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()
            if (action == Continue) {
                // If the Player cannot be pushed back (for some reason), we terminate the Push Chain here.
                val pushData = context.pushChain.last()
                return compositeCommandOf(
                    SetContextProperty(PushContext.PushData::to, pushData, pushData.from),
                    ExitProcedure()
                )
            } else {
                // If the chosen direction results in a chain push, modify the push context
                // and redo the entire chain.
                return castAction<DirectionSelected>(action) { squareSelected ->
                    val origin = context.pushee().coordinates
                    val target = origin.move(squareSelected.direction, 1)
                    val isEmptyOptionAvailable = getEmptySquaresForPushing(
                        pushContext = state.getContext<PushContext>(),
                        pushOptions = setOf(target),
                        state = state
                    ).isNotEmpty()
                    val pushData = context.pushChain.last()
                    val updateActions = listOfNotNull(
                        SetContextProperty(PushContext.PushData::to, pushData, target),
                        if (target.isOnField(rules)) {
                            AddContextListItem(context.looseBalls, state.field[target].balls)
                        } else {
                            null
                        }
                    ).toTypedArray()
                    val commands = if (isEmptyOptionAvailable) {
                        // Player was moved into an empty square, which means we can start resolving
                        // the entire chain.
                        compositeCommandOf(
                            *updateActions,
                            ExitProcedure()
                        )
                    } else {
                        // Target field is occupied, resulting in a chain push, add the
                        // new chain push to the context and restart the process
                        val newPush = PushContext.PushData(
                            pusher = context.pushChain.last().pushee,
                            pushee = state.field[target].player!!, // TODO This doesn't take into account chain pushes
                            from = target,
                            isChainPush = true,
                        )
                        compositeCommandOf(
                            *updateActions,
                            AddContextListItem(context.pushChain, newPush),
                            GotoNode(DecideToUseJuggernaut)
                        )
                    }
                    commands
                }
            }
        }

        // -- HELPER FUNCTIONS --

        // Return squares considered "empty" when doing a Push. This takes into account any ongoing chain pushes.
        private fun getEmptySquaresForPushing(
            pushContext: PushContext,
            pushOptions: Set<FieldCoordinate>,
            state: Game,
        ): List<FieldCoordinate> {
            val options = pushOptions.toMutableSet()

            // Find all occupied squares
            val firstPushedFromLocation = pushContext.pushChain.first().from
            val isFirstPushLocationAvailable = pushContext.pushChain.none { it.to == firstPushedFromLocation }
            val onFieldSquares = options.filter { it.isOnField(state.rules) }
            val occupiedSquares = onFieldSquares.filter {
                // This also takes into account chain-pushes. E.g. the first square in the chain
                // might be available, but only if something else wasn't chain pushed into it.
                state.field[it].isOccupied()
                    || (it == firstPushedFromLocation && !isFirstPushLocationAvailable)
            }

            // All squares on the field are taken. It is only in this case anyone can be pushed out of bounds.
            return if (onFieldSquares.size == occupiedSquares.size) {
                options.filter { it.isOutOfBounds(state.rules) }
            } else {
                (onFieldSquares.toSet() - occupiedSquares.toSet()).toList()
            }
        }
    }
}
