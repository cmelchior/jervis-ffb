package com.jervisffb.engine.rules.bb2020.procedures.actions.block

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
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContextListItem
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.ReplaceContextListItem
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
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportPushedIntoCrowd
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.ThrowInContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.rules.bb2020.skills.Frenzy
import com.jervisffb.engine.rules.bb2020.skills.Sidestep
import com.jervisffb.engine.utils.INVALID_ACTION

data class PushContext(
    // Player starting the first push
    val firstPusher: Player,
    // First player being pushed
    val firstPushee: Player,
    // firstPushee is knocked down after the pushback has resolved. So if they
    // have the ball, it will bounce.
    val isDefenderKnockedDown: Boolean,
    // Is the push part of a multiple block
    val isMultipleBlock: Boolean,
    // Chain of pushes. For a single push, this contains one element
    // Should only be modified from within the `PushStep` procedure.
    val pushChain: MutableList<PushData>,
    // If `true`, the `firstPusher` will follow up after resolving the rest of the chain.
    var followsUp: Boolean = false,
    // Temporary state tracking the current player being resolved for this push step.
    var fullyResolveInProgress: Player? = null,
    // Track any balls that must bounce after the push is resolved.
    // Either because a player was pushed into it, or because a trapdoor
    // swallowed a player with a ball. Balls should be added and resolved in
    // order.
    val looseBalls: MutableList<Ball> = mutableListOf(),
) : ProcedureContext {

    // Returns last "pusher" in the push chain
    fun pusher(): Player {
        return pushChain.last().pusher
    }

    // Returns the last "pushee" in the chain
    fun pushee(): Player {
        return pushChain.last().pushee
    }

    data class PushData(
        val pusher: Player,
        val pushee: Player,
        // Where is the pushee being pushed from?
        val from: FieldCoordinate,
        // Where is the pushee being pushed to?
        var to: FieldCoordinate? = null, // If `null` push direction has not been selected yet
        val isBlitzing: Boolean = false, // If first pusher is doing a Blitz
        val isChainPush: Boolean = false, // True for every push beyond the first
        var usingJuggernaut: Boolean = false,
        var usedGrab: Boolean = false,
        var usedStandFirm: Boolean = false,
        var usedSideStep: Boolean = false,
        var usedFend: Boolean = false,
        // Set to `true` if we checked the player in this step for scoring
        var checkedForScoringAfterTrapdoors: Boolean = false,
    )
}

/**
 * This procedure is responsible for handling the first parts of a push started
 * from a block action, i.e. a POW! or Push (Stumble is converted into either
 * of these two before loading this procedure).
 *
 * The steps involved in resolving a push is pretty complicated and differs
 * slightly between single and multiple blocks, but this procedure is the
 * entry point for both of them and will describe the full sequence of events.
 *
 *
 *
 * -----
 *
 * Since the steps involved in this are complicated, this procedure is
 * responsible for handling all chain-pushes, injuries to all players and ball
 * bouncing/being throw-in that occurred from it. The reason behind the exact
 * sequence of events are discussed in rules-faq.md.
 *
 * NOTE: While the Push Chain for single blocks and Multiple Block has
 * many similarities. There are a also many differences. So for now, Multiple
 * Block uses their own procedure for Push in Multiple Block. See [XXX].
 * Helper functions shared between the two are found in [XXX].
 *
 * Note: We introduce the concept "Push Chain" in this doc, this simply means
 * the data structure that tracks all the pushes and chain-pushes, starting
 * from the attacker and ending with the last player pushed.
 *
 * ## Full Sequence of Events in a Push
 *
 * The full sequence looks like this:
 *
 * 1. Roll block dice and select either Push, Stumble or POW. This triggers this
 *    procedure.
 *
 * 2. Determine the push chain, including all relevant skills at each step, like
 *    Sidestep, Stand Firm or Grab. For chain-pushes, players are considered as
 *    having left their square, but does not yet count as having entered the
 *    target square. No dice are rolled yet. See the section after this list
 *    on exactly how these skills are being applied and in what order.
 *
 * 3. Once a push chain is determined, moves are resolved from the front,
 *    starting with the defender. This might result in some players being moved
 *    twice. No dice are rolled yet.
 *
 * 4. If a player is pushed into the crowd, this is resolved now, i.e., rolling
 *    for injury and choosing to use the apothecary.
 *
 * 5. Choose to follow up or not. If yes, the attacker is moved.
 *
 * 6. If the defender was knocked down, roll for armour/injury and use the
 *    apothecary. If they had the ball it is knocked loose, but does not
 *    bounce yet.
 *
 * 7. Check for Skills affecting the ball, like Strip Ball. If triggered, the
 *    ball is knocked loose, but no dice is rolled yet.
 *
 * 8. Go through the push chain starting with the defender ending with the
 *    attacker. If a player is standing on a trapdoor, roll to see if they are
 *    removed. If yes, any ball they are holding is knocked loose. Do not roll
 *    for bounce yet.
 *
 * 9. Go through the push chain starting with the defender and ending with the
 *    attacker. The first player detected holding the ball will score a
 *    touchdown. If multiple players are in a scoring position, only the first
 *    player will score. All the remaining steps still needs to be completed.
 *
 * 10. Go through the push chain starting with the defender's location and
 *     ending with the attacker's. If there is a loose ball there, it will
 *     either bounce or be thrown in. Check for a touchdown after each ball
 *     (if there are multiple). The first player to hold a ball in a scoring
 *     position will get the touchdown, but all balls must be fully resolved,
 *     i.e., the ball is either caught or landed in an empty square.
 **
 * ## Skills used during a single Push
 *
 * When determining a step in the push chain, it involves a number of skills
 * and interactions, that are not straight-forward. The exact sequence of these
 * are described below with Player A = pusher and Player B = pushee.
 *
 * 1. Player A starts blitz or block and must decide to use Juggernaut or not
 *    (before the push start).
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
 */
// TODO Add support for Treacherous Trapdoor
object PushStepInitialMoveSequence: Procedure() {

    // Start the push by figuring out what kind of push and what skills could impact it.
    // The chain is as follows: Juggernaut -> Stand Firm -> Grab -> Sidestep.
    // If a skill isn't applicable the Node is skipped through a Continue action.
    override val initialNode: Node = DecideToUseJuggernaut
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PushContext>()

    // TODO Is this where we decide on Juggernaut? Or should we somehow make it a node outside
    //  the push chain (since it doesn't apply to chain pushes)
    // TODO Juggernaut probably doesn't apply to chain pushes?
    object DecideToUseJuggernaut: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().pushChain.last().pusher.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PushContext>()
            val hasJuggernaut = false // How to check?
            val canUseJuggernaut = !context.pushChain.last().usingJuggernaut
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
                        SetContextProperty(PushContext.PushData::usingJuggernaut, pushData, true),
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
            return state.getContext<PushContext>().pushChain.last().pushee.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PushContext>()
            val hasStandFirm = false // How to check?
            val canUseStandFirm = !context.pushChain.last().usingJuggernaut
            return when (hasStandFirm && canUseStandFirm) {
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
            val context = state.getContext<PushContext>()
            val hasGrab = false // How to check?
            val canUseGrab = true // TODO Is this true?
            return when (hasGrab && canUseGrab) {
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
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().pushChain.first().pushee.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PushContext>().pushChain.last()
            val hasSidestep = context.pushee.hasSkill<Sidestep>()
            val validSideStepTargets = context.pushee.coordinates
                .getSurroundingCoordinates(rules)
                .count { state.field[it].isUnoccupied() } > 0
            val canUseSidestep = !(context.usedGrab || context.usedStandFirm)
            return when (hasSidestep && canUseSidestep) {
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
                        SetContextProperty(PushContext.PushData::usedSideStep, pushData, true),
                        GotoNode(DecideToUseFend)
                    )
                }
                Cancel, Continue -> {
                    GotoNode(DecideToUseFend)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object DecideToUseFend: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().pushChain.first().pushee.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PushContext>().pushChain.last()
            val hasFend = false // How to check?
            val canUseFend = false // How?
            return when (hasFend && canUseFend) {
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
                        SetContextProperty(PushContext.PushData::usedFend, pushData, true),
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
                context.pushChain.last().pusher.team
            }
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
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
            return checkTypeAndValue<DirectionSelected>(state, action) { squareSelected ->
                val context = state.getContext<PushContext>()
                val origin = context.pushee().coordinates
                val target = origin.move(squareSelected.direction, 1).let {
                    if (it.isOnField(rules)) it else FieldCoordinate.OUT_OF_BOUNDS
                }
                val isEmpty = isSquaresEmptyForPushing(
                    pushContext = state.getContext<PushContext>(),
                    pushOptions = setOf(target),
                    state = state
                ).isNotEmpty()
                val pushData = context.pushChain.last()
                val updateActions = listOfNotNull(
                    SetContextProperty(PushContext.PushData::to, pushData, target),
                    if (target.isOnField(rules)) {
                        AddContextListItem(context.looseBalls,state.field[target].balls)
                    } else {
                        null
                    }
                ).toTypedArray()
                val commands = if (isEmpty) {
                    // Player was moved into an empty square, which means we can start resolving
                    // the entire chain.
                    compositeCommandOf(
                        *updateActions,
                        GotoNode(MovePushedPlayers)
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

        // Check if a square is empty while taking into account any ongoing chain pushes.
        private fun isSquaresEmptyForPushing(
            pushContext: PushContext,
            pushOptions: Set<FieldCoordinate>,
            state: Game,
        ): List<FieldCoordinate> {
            val filteredOptions = pushOptions.toMutableSet()

            // OUT_OF_BOUNDS is only allowed if it is the only option (should this constraint be in Rules?)
            if (filteredOptions.contains(FieldCoordinate.OUT_OF_BOUNDS) && pushOptions.size > 1) {
                filteredOptions.remove(FieldCoordinate.OUT_OF_BOUNDS)
            }

            val firstPushedFromLocation = pushContext.pushChain.first().from
            val isFirstPushLocationAvailable = pushContext.pushChain.none { it.to == firstPushedFromLocation }
            return filteredOptions.filter {
                it == FieldCoordinate.OUT_OF_BOUNDS
                    || state.field[it].isUnoccupied()
                    || (it == firstPushedFromLocation && isFirstPushLocationAvailable)
            }
        }
    }

    /**
     * Resolve the push-chain by moving all players part of it. For now, we only
     * update their field location. Crowd injuries and balls bouncing happens
     * later.
     */
    object MovePushedPlayers: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()
            // Execute push commands from the last to the first, this way we avoid having to deal
            // with squares needing to have to players temporarily. This should be a safe implementation
            // detail, since all commands are executed before creating the game delta.
            return buildCompositeCommand {
                var pushedIntoCrowd = false
                context.pushChain.reversed().forEach { push ->
                    val to = push.to!!
                    // If OUT_OF_BOUNDS, further processing happens in `ResolvePushedIntoTheCrowd`
                    val outOfBounds = (to == FieldCoordinate.OUT_OF_BOUNDS)
                    if (outOfBounds) {
                        // See page 58 in the rulebook. If a player with the ball is pushed into the crowd,
                        // it is a turnover. The Throw-in is handled in `ResolvePushedIntoTheCrowd`
                        if (push.pushee.hasBall() && push.pushee.team == state.activeTeam) {
                            add(SetTurnOver(TurnOver.STANDARD))
                        }
                        addAll(
                            SetPlayerLocation(push.pushee, DogOut),
                            ReportPushedIntoCrowd(push.pushee, push.from)
                        )
                        pushedIntoCrowd = true
                    } else {
                        // At this stage, there should only be one ball on the square,
                        // Even if the player is holding another ball, it isn't knocked loose yet.
                        add(SetPlayerLocation(push.pushee, to))
                        state.field[to].balls.singleOrNull()?.let {
                            add(SetBallState.bouncing(it))
                        }
                        if (context.isDefenderKnockedDown && push.pushee == context.firstPushee) {
                            add(SetPlayerState(push.pushee, PlayerState.KNOCKED_DOWN))
                        }
                    }
                }
                val nextNode = if (pushedIntoCrowd) ResolvePushedIntoTheCrowd else DecideToFollowUp
                add(GotoNode(nextNode))
            }
        }
    }

    // It is only ever the last player in the chain that risks being pushed into
    // the crow. We only resolve the player injury here. Throwing the ball back
    // in doesn't happen until later in the push sequence.
    object ResolvePushedIntoTheCrowd: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<PushContext>()
            return if (!context.pushChain.last().pushee.location.isOnField(rules)) {
                null
            } else {
                DecideToFollowUp
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()
            val pushStep = context.pushChain.last()
            val player = pushStep.pushee
            return buildCompositeCommand {
                // If player had the ball, prepare it to be thrown in again.
                // But it will not happen until later in Push sequence.
                if (player.hasBall()) {
                    val ball = player.ball!!
                    val throwContext = ThrowInContext(
                        ball = ball,
                        outOfBoundsAt = pushStep.from,
                    )
                    addAll(
                        SetBallState.outOfBounds(ball, pushStep.from),
                        SetContext(throwContext)
                    )
                }
                val injuryContext = RiskingInjuryContext(
                    player = context.pushChain.last().pushee,
                    mode = RiskingInjuryMode.PUSHED_INTO_CROWD
                )
                add(SetContext(injuryContext))
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                GotoNode(DecideToFollowUp)
            )
        }
    }

    object DecideToFollowUp: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().firstPusher.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
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
            val pushContext = state.getContext<PushContext>()
            val actions = when (action) {
                is Confirm -> arrayOf(
                    SetContextProperty(PushContext::followsUp, pushContext, true),
                    SetPlayerLocation(pushContext.firstPusher, pushContext.pushChain.first().from)
                )
                is Cancel -> arrayOf() // Do nothing
                is Continue -> {
                    if (pushContext.firstPusher.hasSkill<Frenzy>()) {
                        arrayOf(
                            SetContextProperty(PushContext::followsUp, pushContext, true),
                            SetPlayerLocation(pushContext.firstPusher, pushContext.pushChain.first().from)
                        )
                    } else {
                        arrayOf(
                            SetContextProperty(PushContext::followsUp, pushContext, false),
                        )
                    }
                }
                else -> INVALID_ACTION(action)
            }
            // The parent procedure is responsible for delegating to the next
            // parts of the push chain which should be resolving defender
            // injuries. But this will differ slightly between single and multiple
            // blocks.
            return compositeCommandOf(
                *actions,
                ExitProcedure(),
            )
        }
    }
}
