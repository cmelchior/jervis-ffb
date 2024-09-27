package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.procedures.actions.block.PushStep.ResolvePush
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Frenzy
import dk.ilios.jervis.rules.skills.SideStep
import dk.ilios.jervis.utils.INVALID_ACTION

data class PushContext(
    val firstPusher: Player,
    val firstPushee: Player,
    // Is the push part of a multiple block
    val isMultipleBlock: Boolean,
    // Chain of pushes, for a single push, this contains one element
    // Should only be modified from within `PushStep`.
    val pushChain: List<PushData>,
    val followsUp: Boolean = false,
) : ProcedureContext {

    // Returns last "pusher" in the push chain
    fun pusher(): Player {
        return pushChain.last().pusher
    }

    // Returns the last "pushee in the chain
    fun pushee(): Player {
        return pushChain.last().pushee
    }

    data class PushData(
        val pusher: Player,
        val pushee: Player,
        val from: FieldCoordinate,
        val to: FieldCoordinate? = null, // If `null` push direction has not been selected yet
        val isBlitzing: Boolean = false, // If first pusher is doing a Blitz
        val isChainPush: Boolean = false, // True for every push beyond the first
        val usingJuggernaut: Boolean = false,
        val usedGrab: Boolean = false,
        val usedStandFirm: Boolean = false,
        val usedSideStep: Boolean = false,
        val usedFend: Boolean = false,
    ) {
    }

    // Copy this context and replace last push chain in the process
    fun copyModifyPushChain(data: PushData): PushContext {
        val newPushChain = pushChain.dropLast(1) + listOf(data)
        return copy(pushChain = newPushChain)
    }

    fun copyAddPushChain(data: PushData): PushContext {
        val newPushChain = pushChain + listOf(data)
        return copy(pushChain = newPushChain)
    }
}

/**
 * Resolve push, including any chain pushes. If the last player is pushed into the crowd,
 * it is resolved here.
 *
 * Pushing players is a complicated process, involving a lot of skills. The logic is
 * implemented in the following way, with Player A = pusher and Player B = pushee.
 *
 * 1. Player A starts blitz or block and must decide to use Juggernaut or not (before the push start).
 *    a. Cannot be used on chain pushes.
 *
 * 2. Player B must decide whether to use Stand Firm. Page 80 in the rulebook.
 *    a. Cannot be used if Player A used Juggernaut.
 *    b. Can be used on chain pushes.
 *
 * 3. Player A must decide whether to use Grab. Page 80 in the rulebook.
 *    a. Cannot be used while blitzing.
 *    b. Cannot be used on chain pushes.
 *    c. Cannot be used if no unoccupied squares exist adjacent to Player B.
 *
 * 4. Player B must decide whether to use Sidestep. Page 75 in the rulebook.
 *.   a. Cannot be used if Player A used Grab.
 *    c. Cannot be used if no unoccupied squares exist adjacent to Player B.
 *
 * 5. Player B must decide whether to use Fend. See page 76 in the rulebook.
 *    a. Cannot be used on a chain push.
 *    b. Cannot be used if Player A has Ball & Chain.
 *    c. Cannot be used if Player A is blitzing and using Juggernaut.
 *
 * I could not find any definitive answer for the next scenarios, i.e., what happens
 * if you end up with a circular chain push. It is theoretically possible to have
 * a chain push go back to the start, but it is unclear what happens in that case.
 *
 * There exist at least three scenarios:
 *
 * 1. With 24 players, it is possible to push Player A away from its location,
 *    so it no longer is adjacent to Player B's starting location.
 *
 * 2. With 24 players, it is possible to potentially push a player into Player B's
 *    starting location.
 *
 * 3. With 28 players, it is possible to create an infinite circle that never ends.
 *    However, this is up to the player, and they can just choose a different chain
 *    push sequence to break it.
 *
 * To account for these cases, this procedure implements the following logic:
 *
 * - When calculating a chain push, players are considered as having left their
 *   square as soon as the push direction is selected, i.e., their square is available
 *   in case of a circular chain. (But the players are not actively moved until the
 *   entire chain is resolved).
 *
 * - If Player A is moved away so it is no longer adjacent to Player B's starting square,
 *   they are no longer allowed to follow up. The reason is due to the following
 *   sentence in the rules: "Sometimes, a player must follow-up due to an in-game effect,
 *   a special rule, or a Skill or Trait, whether they want to or not.", in this case,
 *   the in-game effect is Push/Chain-push.
 *
 * - Due to the possibility of circular chain pushes, we risk having two players
 *   in the same location no matter if the chain is resolved from the start or
 *   from the end. To make it more natural, we thus resolve from the beginning,
 *   which also means that being pushed into the crowd is resolved at the end.
 *
 * This also affects Treacherous Trapdoor, if a chain-push results in a player
 * being pushed into another square with a player, what happens?
 *
 *   a. The player can fall into the trapdoor before chain pushing the other player out.
 *      If it falls through, the chain just stops there. The original player stays on the
 *      trapdoor.
 *   b. The check for trapdoor isn't done until after the full chain is resolved.
 *      Potentially leaving a "hole" in the chain.
 *
 * You could probably argue for both interpretations, so in this case, we use option A,
 * as it is easier to implement in [ResolvePush]. However, due to how the logic is setup,
 * you select all steps of the chain without checking for trapdoors, and the trapdoor check
 * is then done when fully resolving the chain.
 *
 * This means a push is modeled this way:
 * ```
 *   for_each_step_in_chain {
 *      playerA.location = pushed_into_location
 *      field.get(pushed_into_location) = playerA
 *      // At this point in time playerA.location == playerB.location (which is fine)
 *   }
 * ```
 */
object PushStep: Procedure() {

    // Start the push by figuring out what kind of push and what skills could impact it.
    // The chain is as follows: Juggernaut -> Stand Firm -> Grab -> Sidestep.
    // As an optimization this node will try to figure out if any of these can be skipped.
    // If we end up in the middle of the chain due to this, the rest of the nodes will
    // be executed, but will just require "Continue" if they do not apply
    override val initialNode: Node = DecideToUseJuggernaut
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PushContext>()
    }

    // TODO Is this where we decide on Juggernaut?
    // TODO Juggernaut probably doesn't apply to chain pushes?
    object DecideToUseJuggernaut: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().pushChain.last().pusher.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<PushContext>()
            val hasJuggernaut = false // How to check?
            val canUseJuggernaut = !context.pushChain.last().usingJuggernaut
            return when(hasJuggernaut && canUseJuggernaut) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Confirm -> {
                    val context = state.getContext<PushContext>()
                    val newContext = context.copyModifyPushChain(context.pushChain.last().copy(usingJuggernaut = true))
                    return compositeCommandOf(
                        SetContext(newContext),
                        GotoNode(DecideToUseStandFirm)
                    )
                }
                is Cancel,
                is Continue -> {
                    GotoNode(DecideToUseStandFirm)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object DecideToUseStandFirm: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? {
            return state.getContext<PushContext>().pushChain.last().pushee.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<PushContext>()
            val hasStandFirm = false // How to check?
            val canUseStandFirm = !context.pushChain.last().usingJuggernaut
            return when(hasStandFirm && canUseStandFirm) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Confirm -> {
                    val context = state.getContext<PushContext>()
                    val newContext = context.copyModifyPushChain(context.pushChain.last().copy(usedStandFirm = true))
                    return compositeCommandOf(
                        SetContext(newContext),
                        GotoNode(DecideToUseGrab)
                    )
                }
                is Cancel,
                is Continue -> {
                    GotoNode(DecideToUseGrab)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object DecideToUseGrab: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().pushChain.first().pusher.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<PushContext>()
            val hasGrab = false // How to check?
            val canUseGrab = true // TODO Is this true?
            return when(hasGrab && canUseGrab) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Confirm -> {
                    val context = state.getContext<PushContext>()
                    val newContext = context.copyModifyPushChain(context.pushChain.last().copy(usedGrab = true))
                    return compositeCommandOf(
                        SetContext(newContext),
                        GotoNode(DecideToUseSidestep)
                    )
                }
                is Cancel,
                is Continue -> {
                    GotoNode(DecideToUseSidestep)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object DecideToUseSidestep: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().pushChain.first().pushee.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<PushContext>().pushChain.last()
            val hasSidestep = context.pushee.hasSkill<SideStep>()
            val validSideStepTargets = context.pushee.coordinates
                .getSurroundingCoordinates(rules)
                .count { state.field[it].isUnoccupied() } > 0
            val canUseSidestep = !(context.usedGrab || context.usedStandFirm)
            return when(hasSidestep && canUseSidestep) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Confirm -> {
                    val context = state.getContext<PushContext>()
                    val newContext = context.copyModifyPushChain(context.pushChain.last().copy(usedSideStep = true))
                    return compositeCommandOf(
                        SetContext(newContext),
                        GotoNode(DecideToUseFend)
                    )
                }
                is Cancel,
                is Continue -> {
                    GotoNode(DecideToUseFend)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object DecideToUseFend: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().pushChain.first().pushee.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<PushContext>().pushChain.last()
            val hasFend = false // How to check?
            val canUseFend = false // How?
            return when(hasFend && canUseFend) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Confirm -> {
                    val context = state.getContext<PushContext>()
                    val newContext = context.copyModifyPushChain(context.pushChain.last().copy(usedFend = true))
                    return compositeCommandOf(
                        SetContext(newContext),
                        GotoNode(SelectPushDirection)
                    )
                }
                is Cancel,
                is Continue -> {
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
                context.pushChain.last().pusher.team
            }
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val pushContext = state.getContext<PushContext>()
            val lastPushInChain = pushContext.pushChain.last()
            // TODO Add support for skills, right now just go with the default 3 options
            val pushOptions = if (lastPushInChain.usedSideStep) {
                lastPushInChain.pushee.coordinates.getSurroundingCoordinates(rules).toSet()
            } else {
                rules.getPushOptions(lastPushInChain.pusher, lastPushInChain.pushee)
            }

            // Calculate all push options taking into account a chain push in progress.
            // In chain pushes, only the square of Player B could be empty, but it might
            // not be in case of a circular chain.
            val emptyFields = isSquaresEmptyForPushing(pushContext, pushOptions, state)
            return if (emptyFields.isNotEmpty()) {
                emptyFields.map { SelectFieldLocation.push(it) }
            } else {
                pushOptions.map { SelectFieldLocation.push(it) }
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            // If the chosen direction results in a chain push, modify the push context
            // and redo the entire chain.
            return checkType<FieldSquareSelected>(action) { action ->
                val availableActions = getAvailableActions(state, rules)
                if (!availableActions.contains(SelectFieldLocation.push(action.coordinate))) {
                    INVALID_ACTION(action, "Target $action is not valid: $availableActions")
                }

                val isEmpty = isSquaresEmptyForPushing(
                    state.getContext<PushContext>(),
                    setOf(action.coordinate),
                    state
                ).isNotEmpty()

                val context = state.getContext<PushContext>()
                val updatedContext = context.copyModifyPushChain(context.pushChain.last().copy(to = action.coordinate))

                val commands = if (isEmpty) {
                    // Player was moved into an empty square, which means we can start resolving
                    // the entire chain.
                    compositeCommandOf(
                        SetContext(updatedContext),
                        GotoNode(ResolvePush)
                    )
                } else {
                    // Target field is occupied, resulting in a chain push, add the
                    // new chain push to the context and restart the process
                    val newPush = PushContext.PushData(
                        pusher = context.pushChain.last().pushee,
                        pushee = state.field[action.coordinate].player!!, // TODO This doesn't take into account chain pushes
                        from = action.coordinate,
                        isChainPush = true,
                    )
                    val newContext = updatedContext.copyAddPushChain(newPush)
                    compositeCommandOf(
                        SetContext(newContext),
                        GotoNode(DecideToUseJuggernaut)
                    )
                }
                commands
            }
        }

        // Check if a square is empty while taking into account any ongoing chain pushes.
        private fun isSquaresEmptyForPushing(
            pushContext: PushContext,
            pushOptions: Set<FieldCoordinate>,
            state: Game,
        ): List<FieldCoordinate> {
            val firstPushedFromLocation = pushContext.pushChain.first().from
            val isFirstPushLocationAvailable = pushContext.pushChain.filterIndexed { i, el ->
                el.to == firstPushedFromLocation
            }.isEmpty()
            return pushOptions.filter {
                it == FieldCoordinate.OUT_OF_BOUNDS ||
                    state.field[it].isUnoccupied() ||
                    it == firstPushedFromLocation && isFirstPushLocationAvailable
            }
        }
    }

    object ResolvePush: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()
            val moveCommands = context.pushChain.map { push ->
                val to = push.to!!
                if (to == FieldCoordinate.OUT_OF_BOUNDS) {
                    // We do not know where the player is going until after the injury roll,
                    // but they are not on the field. The pusher must decide whether to
                    // follow up before any injury roll.
                    SetPlayerLocation(push.pushee, FieldCoordinate.UNKNOWN)
                } else {
                    SetPlayerLocation(push.pushee, to)
                }
            }
            return compositeCommandOf(
                *moveCommands.toTypedArray(),
                GotoNode(DecideToFollowUp)
            )
        }
    }

    object PushedIntoTheCrowd: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            TODO("Not yet implemented")
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }

    object DecideToFollowUp: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().firstPusher.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<PushContext>()
            return if (
                context.firstPusher.hasSkill<Frenzy>() || // Always follow up when having Frenzy
                context.isMultipleBlock // Never follow up when using Multiple Block
            ) {
                listOf(ContinueWhenReady)
            } else {
                return listOf(
                    CancelWhenReady,
                    ConfirmWhenReady
                )
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()
            val actions = when(action) {
                is Confirm -> arrayOf(
                    SetContext(context.copy(followsUp = true)),
                    SetPlayerLocation(context.firstPusher, context.pushChain.first().from)
                )
                is Cancel -> arrayOf() // Do nothing
                is Continue -> {
                    if (context.firstPusher.hasSkill<Frenzy>()) {
                        arrayOf(
                            SetContext(context.copy(followsUp = true)),
                            SetPlayerLocation(context.firstPusher, context.pushChain.first().from)
                        )
                    } else {
                        arrayOf(
                            SetContext(context.copy(followsUp = false)),
                        ) // Do nothing
                    }
                }
                else -> INVALID_ACTION(action)
            }
            val pushedIntoTheCrowd = context.pushChain.last().to == FieldCoordinate.OUT_OF_BOUNDS
            return if (pushedIntoTheCrowd) {
                compositeCommandOf(
                    *actions,
                    GotoNode(PushedIntoTheCrowd)
                )
            } else {
                compositeCommandOf(
                    *actions,
                    ExitProcedure()
                )
            }
        }
    }
}
