package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOff
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushStep
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult
import com.jervisffb.engine.utils.containsActionWithRandomBehavior
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.UiSnapshotTimerData
import com.jervisffb.ui.game.state.decorators.DeselectPlayerDecorator
import com.jervisffb.ui.game.state.decorators.EndActionDecorator
import com.jervisffb.ui.game.state.decorators.EndSetupDecorator
import com.jervisffb.ui.game.state.decorators.FieldActionDecorator
import com.jervisffb.ui.game.state.decorators.SelectBlockTypeDecorator
import com.jervisffb.ui.game.state.decorators.SelectDirectionDecorator
import com.jervisffb.ui.game.state.decorators.SelectFieldLocationDecorator
import com.jervisffb.ui.game.state.decorators.SelectMoveTypeDecorator
import com.jervisffb.ui.game.state.decorators.SelectPlayerActionDecorator
import com.jervisffb.ui.game.state.decorators.SelectPlayerDecorator
import com.jervisffb.ui.game.view.DialogFactory
import com.jervisffb.ui.game.viewmodel.Feature
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Class responsible for enhancing the UI, so it is able to create a [GameAction].
 *
 * See [com.jervisffb.ui.game.UiGameController.startGameEventLoop]
 */
open class ManualActionProvider(
    protected val game: GameEngineController,
    private val menuViewModel: MenuViewModel,
    private val clientMode: TeamActionMode, // Which teams are controlled by this game client
    private val gameSettings: GameSettings
): UiActionProvider() {

    companion object {
        val LOG = jervisLogger()
    }

    private lateinit var availableActions: ActionRequest

    // If set, it contains an action that should automatically be sent on the next call to getAction()
    var automatedAction: GeneratedAction? = null

    // If a user selected multiple actions, they are all listed here. This queue should be emptied before
    // sending anything else
    private var delayBetweenActions = false
    private val queuedActions = mutableListOf<GeneratedAction>()
    private val queuedActionsGeneratorFuncs = mutableListOf<QueuedActionsGenerator>()

    val fieldActionDecorators = mapOf(
        // EndSetupWhenReady -> TODO()
        // EndTurnWhenReady -> TODO()
        // is RollDice -> TODO()
        // is SelectBlockType -> TODO()
        // SelectCoinSide -> TODO()
        // is SelectDicePoolResult -> TODO()
        // SelectDogout -> TODO()
        // is SelectInducement -> TODO()
        // is SelectNoReroll -> TODO()
        // is SelectRandomPlayers -> TODO()
        // is SelectRerollOption -> TODO()
        // is SelectSkill -> TODO()
        // TossCoin -> TODO()
        DeselectPlayer::class to DeselectPlayerDecorator(),
        EndActionWhenReady::class to EndActionDecorator(),
        EndSetupWhenReady::class to EndSetupDecorator(),
        SelectBlockType::class to SelectBlockTypeDecorator(),
        SelectDirection::class to SelectDirectionDecorator(),
        SelectFieldLocation::class to SelectFieldLocationDecorator(),
        SelectMoveType::class to SelectMoveTypeDecorator(),
        SelectPlayer::class to SelectPlayerDecorator(),
        SelectPlayerAction::class to SelectPlayerActionDecorator(),
    )

    private fun <T: GameActionDescriptor> getDecorator(type: KClass<T>): FieldActionDecorator<GameActionDescriptor>? {
        return fieldActionDecorators[type] as? FieldActionDecorator<GameActionDescriptor>
    }

    override fun startHandler(uiTimerData: MutableSharedFlow<UiSnapshotTimerData>) {
        // Do nothing. We are sharing the controller with the main UiGameController
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        // Do nothing. We are sharing the controller with the main UiGameController
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        this.availableActions = controller.getAvailableActions()

        // If the UI has registered any queued action generators, we run them first before
        // trying to find other automated actions.
        val iter = queuedActionsGeneratorFuncs.iterator()
        var actionId = actions.nextActionId
        while (iter.hasNext()) {
            val result = iter.next()(controller)
            if (result != null) {
                delayBetweenActions = result.delayBetweenActions
                queuedActions.addAll(result.actions.map {
                    GeneratedAction(actionId, it).also {
                        actionId += 1
                    }
                })
                iter.remove()
            }
        }

        // We only want to check for other automated settings if no queued up actions exists.
        // This also means that anyone queuing up actions, most queue up all intermediate actions
        // as well. Even the ones that are normally automatically created.
        if (queuedActions.isEmpty()) {
            calculateAutomaticResponse(controller, controller.getAvailableActions().actions)?.let {
                automatedAction = GeneratedAction(actionId, it)
            }
        }
    }

    override fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionRequest) {
        availableActions = actions
        // If we have automatic or queued actions up, we do not want to show any UI elements that could generate
        // actions as they would either interrupt the queue or be illegal outright.
        if (queuedActions.isNotEmpty()) return
        if (automatedAction != null) return

        // TODO What to do here when it is the other team having its turn.
        //  The behavior will depend on the game being a HotSeat vs. Client/Server
        var showActionDecorators = when (clientMode) {
            TeamActionMode.HOME_TEAM -> actions.team.id == game.state.homeTeam.id
            TeamActionMode.AWAY_TEAM -> actions.team.id == game.state.awayTeam.id
            TeamActionMode.ALL_TEAMS -> true
        }

        // If the available actions are random, we only want to show controls for the UI if configured so.
        // This is mostly a developer or custom play setting and not something commonly used for normal games.
        if (actions.containsActionWithRandomBehavior() && !gameSettings.clientSelectedDiceRolls) {
            showActionDecorators = false
        }

        if (showActionDecorators) {
            addDialogDecorators(state, actions)

            // If a dialog is being shown, we do not want to enable any other kind of input until
            // the dialog has been resolved.
            if (state.dialogInput == null) {
                addNonDialogActionDecorators(state, actions)
            }
        }
    }

    override fun decorateSelectedAction(state: UiGameSnapshot, action: GameAction) {
        // Do nothing (for now)
    }

    override suspend fun getAction(id: GameActionId): GeneratedAction {
        // When returning actions, we resolve it with the following priority
        // 1. All Queued actions
        // 2. Then automated actions
        // 3. Actions from the UI

        // Drain queued data first, if present.
        if (queuedActions.isNotEmpty()) {
            val action = queuedActions.removeFirst()
            // Do not pause for flow-control events, only events that would appear "visible"
            // to the player
            if (delayBetweenActions) {
                delay(150)
            }
            when {
                action.id < id -> {
                    LOG.i { "[ManualProvider] Dropping outdated queued action(${action.id.value} < ${id.value}): $action" }
                    // If the first item in the queue is out of order, then the rest is as well. So we should remove them.
                    // Note, this assumes that we only try to enqueue one sequence at a time (which is probably a fair
                    // assumption for now)
                    queuedActions.clear()
                }
                action.id > id -> {
                    error("[ManualProvider] Received future queued action. This should not be possible (${action.id.value} > ${id.value}): $action")
                }
                else -> return action
            }
        }
        delayBetweenActions = false

        // Otherwise, drain automated response
        val autoAction =  automatedAction?.let { action ->
            automatedAction = null
            when {
                action.id < id -> {
                    LOG.i { "[ManualProvider] Dropping outdated automated action(${action.id.value} < ${id.value}): $action" }
                    null
                }
                action.id > id -> {
                    error("[ManualProvider] Received future automated action. This should not be possible (${action.id.value} > ${id.value}): $action")
                }
                else -> action
            }
        }

        // And finally ask the UI
        val manualAction = if (autoAction == null) {
            var selectedAction: GeneratedAction? = null
            while (selectedAction == null) {
                val newAction = actionSelectedChannel.receive()
                when {
                    newAction.id < id -> LOG.i { "[ManualProvider] Dropping outdated manual action (${newAction.id.value} < ${id.value}: ${newAction.action}" }
                    newAction.id > id -> error("[ManualProvider] Received future manual event. This should never happen (${newAction.id.value} > ${id.value}): ${newAction.action}")
                    else -> {
                        selectedAction = newAction
                    }
                }
            }
            selectedAction
        } else {
            null
        }

        return autoAction ?: manualAction ?: error("Invariant when choosing actions was broken")
    }

    override fun userActionSelected(id: GameActionId, action: GameAction) {
        if (actionSelectedChannel.trySend(GeneratedAction(id, action)).isFailure) {
            error("Unable to send action to channel. Is the channel closed?")
        }
    }

    override fun userMultipleActionsSelected(startingId: GameActionId, actions: List<GameAction>, delayEvent: Boolean) {
        if (actions.isEmpty()) throw IllegalArgumentException("Action list must contain at least one action")
        // Store all events to be sent and sent the first one to be processed
        var actionId = startingId
        queuedActions.addAll(actions.map {
            GeneratedAction(actionId, it).also { action ->
                actionId += 1
            }
        })
        delayBetweenActions = delayEvent
        actionScope.launch {
            val action = queuedActions.removeFirst()
            actionSelectedChannel.send(action)
        }
    }

    /**
     * Check if the game are in a state where we want to show a pop-up dialog in order
     * to create a [GameAction]. If yes, the data needed to build the dialog is added
     * to the UI state.
     */
    private fun addDialogDecorators(state: UiGameSnapshot, actions: ActionRequest) {
        val dialogData = DialogFactory.createDialogIfPossible(
            game,
            actions,
            { actionDescriptors-> mapUnknownAction(actionDescriptors.actions) }
        )
        if (state.dialogInput != null) {
            error("Only 1 dialog is allowed. Dialog already configured: ${state.dialogInput}")
        }
        state.dialogInput = dialogData
    }

    /**
     * Modify the UI state, so it is ready to accept user input in order to generate the next
     * [GameAction]. This mostly means adding click-listeners, but also modify the UI so it is
     * visible that UI elements can be interacted with.
     */
    // TODO Should probably refactor this so every case is in its own function. Perhaps move to a separate
    //  class to make it more explicit?
    private fun addNonDialogActionDecorators(snapshot: UiGameSnapshot, request: ActionRequest) {
        val state = snapshot.game
        request.actions.forEach { descriptor ->
            val decorator = getDecorator(descriptor::class)
            if (decorator != null) {
                decorator.decorate(this, state, snapshot, descriptor)
            } else {
                // Any action that isn't being mapped to an UI component needs to go here.
                // This way, we ensure that the UI is never blocked during development.
                // In an ideal world, nothing should ever go here.
                snapshot.unknownActions.addAll(mapUnknownAction(descriptor))
            }
        }

        // Choosing whether to showing the context menu is a bit complicated.
        // So we cannot decide this until all available actions have been processed.
        // But we employ the rule that if one of the actions is a "main" action, it means
        // the player was just selected, and we should show the context menu up front.
        // Otherwise, it means that the player is in the middle of their action and we should
        // not show the context menu up front. That should be up to the player
        state.activePlayer?.location?.let { activePlayerLocation ->
            val square = snapshot.fieldSquares[activePlayerLocation]
            if (square != null && square.contextMenuOptions.isNotEmpty() && square.contextMenuOptions.count { it.title == "End action" } == 0) {
                snapshot.fieldSquares[activePlayerLocation as FieldCoordinate]?.apply {
                    showContextMenu = true
                }
            }
        }

        // TODO Add other actions that cannot be found in the Actions, like "Stand-Up and End turn"
    }

    /**
     * Unknown actions are actions we haven't handled yet. This is really an error, but in an
     * attempt to unblock testing/development, we instead map the action to the best possible
     * [GameAction] we can. This enables us to show a list of "unknown actions" in the UI (which should
     * only be shown during development).
     */
    private fun mapUnknownAction(action: GameActionDescriptor): List<GameAction> {
        return action.createAll()
    }

    private fun mapUnknownAction(actions: List<GameActionDescriptor>): List<GameAction> {
        return actions.flatMap { mapUnknownAction(it) }
    }

    /**
     * Check if we can respond automatically to an event without having to involve the user.
     *
     * Some requirements:
     * - Any action returned this way should also have an entry in [Feature]
     */
    private fun calculateAutomaticResponse(
        controller: GameEngineController,
        actions: List<GameActionDescriptor>,
    ): GameAction? {

        // When reacting to an `Undo` command, all automatic responses are disabled.
        // If not disabled, Undo'ing an action might put us in a state that will
        // automatically move us forward again, which will make it appear as the
        // Undo didn't work.
        if (controller.lastActionWasUndo()) {
            return null
        }

        // First, we check if we are playing Hotseat and the game is set to roll random
        // actions on the "server". In this case, they are generated here.
        if (!gameSettings.clientSelectedDiceRolls && gameSettings.isHotseatGame && actions.containsActionWithRandomBehavior()) {
            return createRandomAction(controller.state, actions)
        }

        // Do not reroll successful rolls that are considered "successful"
        if (menuViewModel.isFeatureEnabled(Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS)) {
            if (actions.filterIsInstance<SelectNoReroll>().count { it.rollSuccessful == true} > 0) {
                return NoRerollSelected()
            }
        }

        // Randomly select a kicking player
        // TODO Should only do this if no-one has kick
        val currentNode = controller.currentProcedure()?.currentNode()
        if (currentNode == TheKickOff.NominateKickingPlayer && menuViewModel.isFeatureEnabled(
                Feature.SELECT_KICKING_PLAYER
            )) {
            return (currentNode as ActionNode).getAvailableActions(controller.state, controller.rules)
                .filterIsInstance<SelectPlayer>()
                .single()
                .createRandom()
        }

        // If a player action can only end, just end it immediately
        if (menuViewModel.isFeatureEnabled(Feature.END_PLAYER_ACTION_IF_ONLY_OPTON) && actions.size == 1 && actions.first() is EndActionWhenReady) {
            return EndAction
        }

        // Automatically select pushback direction when only one option is available.
        if (actions.size == 1 && actions.first() is SelectFieldLocation && actions.first().createAll().size == 1 && currentNode is PushStep.SelectPushDirection) {
            val loc = (actions.first() as SelectFieldLocation).squares.first()
            return FieldSquareSelected(loc.coordinate)
        }

        // When selecting block results after reroll and only 1 dice is available.
        if (currentNode == StandardBlockChooseResult.SelectBlockResult && actions.size == 1) {
            val choices = (actions.first() as SelectDicePoolResult).pools
            if (choices.size == 1 && choices.first().dice.size == 1) {
                return DicePoolResultsSelected(listOf(
                    DicePoolChoice(id = 0, listOf(choices.first().dice.single().result))
                ))
            }
        }

        // When there is only one block type for a block, just select that one straight away
        if (
            menuViewModel.isFeatureEnabled(Feature.SELECT_BLOCK_TYPE_IF_ONLY_OPTON) &&
            (currentNode == BlockAction.SelectBlockType || currentNode == BlitzAction.SelectBlockType)
        ) {
            actions.filterIsInstance<SelectBlockType>().firstOrNull()?.let {
                if (it.size == 1) {
                    return BlockTypeSelected(it.types.first())
                }
            }
        }

        // Automatically decide to follow op (or not), if you there really isn't a choice in the matter
        if (currentNode is PushStep.DecideToFollowUp && actions.size == 1) {
            when (val action = actions.first()) {
                is ConfirmWhenReady -> Confirm
                is CancelWhenReady -> Cancel
                else -> error("Unexpected action: $action")
            }
        }

        return null
    }

    /**
     * Allow the UI to register a queued action generator, that will run at a
     * later stage. This is useful if the UI wants to generate a chain of actions, but some
     * of the intermediate actions are unknown.
     *
     * E.g., when standing up to move, the coach might (or might not) have to
     * roll for Negatraits or just Standing Up, before being allowed to move.
     * In this case, we will register an action generator that only trigger
     * once the player can actually move.
     */
    override fun registerQueuedActionGenerator(generator: QueuedActionsGenerator) {
        queuedActionsGeneratorFuncs.add(generator)
    }

    override fun hasQueuedActions(): Boolean {
        return queuedActions.isNotEmpty()
    }

    override fun clearQueuedActions() {
        queuedActions.clear()
        queuedActionsGeneratorFuncs.clear()
        // This shouldn't be needed, since this is only set in `prepareForNextAction`
        // and that method should not be called in case of time running out.
        automatedAction = null
    }
}


