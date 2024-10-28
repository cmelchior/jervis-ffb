package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushStep.ResolvePush
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.rules.bb2020.skills.Frenzy
import com.jervisffb.engine.rules.bb2020.skills.SideStep
import com.jervisffb.engine.utils.INVALID_ACTION

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
 * Resolve push, including any chain pushes. If the last player is pushed into
 * the crowd, it is resolved here.
 *
 * Pushing players is a complicated process, involving a lot of skills and timings.
 * The logic is implemented in the following way, with Player A = pusher and
 * Player B = pushee.
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
 * 1. With 24 players, it is possible for a Player C to push Player A away from
 *    its starting location, so it no longer is adjacent to Player B's starting
 *    location.
 *
 * 2. With 24 players, it is possible to potentially push a player C into Player
 *    B's starting location.
 *
 * 3. With 28 players, it is possible to create an infinite circle that never ends.
 *    However, this is up to the pushing coach, and they can just choose a different
 *    chain push sequence to break it.
 *
 * To account for these cases, this procedure implements the following logic:
 *
 * - When calculating a chain push, players are considered as having left their
 *   square as soon as the push direction is selected, i.e., their square is
 *   available in case of a circular chain. But the players are not moved into
 *   their target square until the entire chain is resolved.
 *
 * - If Player A is moved away so it is no longer adjacent to Player B's starting
 *   square, they are no longer allowed to follow up. The reason is due to the
 *   following sentence in the rules: "Sometimes, a player must follow-up due to
 *   an in-game effect, a special rule, or a Skill or Trait, whether they want to
 *   or not.", in this case, the in-game effect is a push or chain-push.
 *
 * - Due to the possibility of circular chain pushes, we risk having two players
 *   in the same location no matter if the chain is resolved from the start or
 *   from the end. To make it more natural, we resolve the chain from the
 *   beginning.
 *
 * The choice of resolving from the start has consequences for Treacherous
 * Trapdoor, Scoring, Ball Clone, and being Pushed Into The Crowd.
 *
 * **Treacherous Trapdoor**
 * If a chain-push results in a player being pushed into another square with a
 * player standing on a trapdoor, what happens?
 *
 *   a. The player can fall into the trapdoor before chain-pushing the other
 *      player out. If it falls through, the chain just stops there. The original
 *      player stays on the trapdoor.
 *   b. The check for trapdoor isn't done until after the full chain is resolved.
 *      Potentially leaving a "hole" in the chain.
 *
 * You could probably argue for both interpretations, so in this case, we use
 * option A, as it is easier to implement in [ResolvePush]. However, due to how
 * the logic is set up, you select all steps of the chain without checking for
 * trapdoors, and the trapdoor check is then done when fully resolving the chain.
 *
 * **Scoring **
 * If Ball Clone is in play, and you have a ball on the ground and a ball carried
 * by a player that is pushed into the End Zone, then we are going to check for
 * scoring for the player being pushed into the End Zone first, before checking
 * for Pickup (because that will happen by the end of chain-push).
 *
 * If a touchdown is scored during the push-back, we will still resolve the entire
 * chain (including pushing people into the crowd) before the turn-over is triggered.
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
// TODO Add support for Treacherous Trapdorr
// TODO Add support for scoring
// TODO Probably have to rethink the logic in this procedure a bit.
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
            return listOf(
                if (emptyFields.isNotEmpty()) {
                    SelectDirection(
                        origin = lastPushInChain.pushee.coordinates,
                        directions = emptyFields.map { Direction.from(lastPushInChain.pushee.coordinates, it) }
                    )
                } else {
                    SelectDirection(
                        origin = lastPushInChain.pushee.coordinates,
                        directions = pushOptions.map { Direction.from(lastPushInChain.pushee.coordinates, it) }
                    )
                }
            )
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            // If the chosen direction results in a chain push, modify the push context
            // and redo the entire chain.
            return checkTypeAndValue<DirectionSelected>(state, rules, action) { squareSelected ->
                val context = state.getContext<PushContext>()
                val origin = context.pushee().coordinates
                val target = origin.move(squareSelected.direction, 1)
                val isEmpty = isSquaresEmptyForPushing(
                    state.getContext<PushContext>(),
                    setOf(target),
                    state
                ).isNotEmpty()

                val updatedContext = context.copyModifyPushChain(context.pushChain.last().copy(to = target))

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
                        pushee = state.field[target].player!!, // TODO This doesn't take into account chain pushes
                        from = target,
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
            // Resolve push from the last to the first. As doing the other way seems
            // to cause issues with Undo. Probably a bug, so it needs to be investigated.
            val moveCommands = context.pushChain.reversed().map { push ->
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
            // TODO If the last player is being pushed into the ball, they get a chance to pick
            //  it up. Which can potentially trigger a scoring event.
            return compositeCommandOf(
                *moveCommands.toTypedArray(),
                GotoNode(DecideToFollowUp)
            )
        }
    }

    object PushedIntoTheCrowd: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val context = state.getContext<PushContext>()
            return SetContext(
                    RiskingInjuryContext(
                        player = context.pushChain.last().pushee,
                        mode = RiskingInjuryMode.PUSHED_INTO_CROWD
                    )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            // TODO What about turnovers
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
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
                        )
                    }
                }
                else -> INVALID_ACTION(action)
            }
            val pushedIntoTheCrowd = context.pushChain.last().to == FieldCoordinate.OUT_OF_BOUNDS
            return if (pushedIntoTheCrowd) {
                compositeCommandOf(
                    *actions,
//                    SetPlayerLocation(context.pushChain.last().pushee, FieldCoordinate.UNKNOWN),
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
