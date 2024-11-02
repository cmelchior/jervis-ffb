package com.jervisffb.ui.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameController
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOff
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushStep
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult
import com.jervisffb.ui.UiGameController
import com.jervisffb.ui.UiGameSnapshot
import com.jervisffb.ui.state.decorators.DeselectPlayerDecorator
import com.jervisffb.ui.state.decorators.EndActionDecorator
import com.jervisffb.ui.state.decorators.FieldActionDecorator
import com.jervisffb.ui.state.decorators.SelectDirectionDecorator
import com.jervisffb.ui.state.decorators.SelectFieldLocationDecorator
import com.jervisffb.ui.state.decorators.SelectMoveTypeDecorator
import com.jervisffb.ui.state.decorators.SelectPlayerActionDecorator
import com.jervisffb.ui.state.decorators.SelectPlayerDecorator
import com.jervisffb.ui.view.DialogFactory
import com.jervisffb.ui.viewmodel.Feature
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

typealias QueuedActionsGenerator = (GameController) -> QueuedActionsResult?

data class QueuedActionsResult(val actions: List<GameAction>, val delayBetweenActions: Boolean = false) {
    constructor(action: GameAction, delayEvent: Boolean = false): this(listOf(action), delayEvent)
}

/**
 * Class responsible for enhancing the UI, so it is able to create a [GameAction]
 * that can be sent to the [GameController].
 */
class ManualActionProvider(
    private val uiState: UiGameController,
    private val menuViewModel: MenuViewModel
): UiActionProvider() {

    private lateinit var controller: GameController
    private lateinit var actions: ActionRequest

    // If set, it contains an action that should automatically be sent on the next call to getAction()
    var automatedAction: GameAction? = null

    // If a user selected multiple actions, they are all listed here. This queue should be emptied before
    // sending anything else
    private var delayBetweenActions = false
    private val queuedActions = mutableListOf<GameAction>()

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
        SelectDirection::class to SelectDirectionDecorator(),
        SelectFieldLocation::class to SelectFieldLocationDecorator(),
        SelectMoveType::class to SelectMoveTypeDecorator(),
        SelectPlayer::class to SelectPlayerDecorator(),
        SelectPlayerAction::class to SelectPlayerActionDecorator(),
    )

    private fun <T: GameActionDescriptor> getDecorator(type: KClass<T>): FieldActionDecorator<GameActionDescriptor>? {
        return fieldActionDecorators[type] as? FieldActionDecorator<GameActionDescriptor>
    }

    override fun prepareForNextAction(controller: GameController) {
        this.controller = controller
        this.actions = controller.getAvailableActions()

        // If the UI has registered any queued action generators, we run them first before
        // trying to find other automated actions.
        val iter = queuedActionsGeneratorFuncs.iterator()
        while(iter.hasNext()) {
            val result = iter.next()(controller)
            if (result != null) {
                delayBetweenActions = result.delayBetweenActions
                queuedActions.addAll(result.actions)
                iter.remove()
            }
        }

        // We only want to check for other automated settings if no queued up actions exists.
        // This also means that anyone quing up actions, most queue up all intermediate actions
        // as well.
        if (queuedActions.isEmpty()) {
            automatedAction = calculateAutomaticResponse(controller, controller.getAvailableActions().actions)
        }
    }

    override fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionRequest) {
        if (queuedActions.isNotEmpty()) return

        // TODO What to do here when it is the other team having its turn.
        //  The behavior will depend on the game being a HotSeat vs. Client/Server
        addDialogDecorators(state, actions)

        // If a dialog is being shown, we do not want to enable any other kind of input until
        // the dialog has been resolved.
        if (state.dialogInput == null) {
            addNonDialogActionDecorators(state, actions)
        }
    }

    override fun decorateSelectedAction(state: UiGameSnapshot, action: GameAction) {
        // Do nothing (for now)
    }

    override suspend fun getAction(): GameAction {

        // Empty queued data if present
        if (queuedActions.isNotEmpty()) {
            val action = queuedActions.removeFirst()
            // Do not pause for flow-control events, only events that would appear "visible"
            // to the player
            if (action !is MoveTypeSelected && delayBetweenActions) {
                delay(150)
            }
            return action
        }
        delayBetweenActions = false

        // Otherwise empty automated response
        // otherwise wait for response
        return automatedAction?.let { action ->
            automatedAction = null
            action
        } ?: actionSelectedChannel.receive()
    }

    override fun userActionSelected(action: GameAction) {
        actionScope.launch {
            actionSelectedChannel.send(action)
        }
    }

    override fun userMultipleActionsSelected(actions: List<GameAction>, delayEvent: Boolean) {
        // Store all events to be sent and sent the first one to be processed
        queuedActions.addAll(actions)
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
            controller,
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

        // Choosing whether or not to showing the context menu is a bit complicated.
        // So we cannot decide this until all available actions have been processed.
        // But we employ the rule that if one of the actions is a "main" action, it means
        // the player was just selected, and we should show the context menu up front.
        // Otherwise, it means that the player is in the middle of their action and we should
        // not show the context menu up front. That should be up to the player
        state.activePlayer?.location?.let { activePlayerLocation ->
            val square = snapshot.fieldSquares[activePlayerLocation]
            if (square != null && square.contextMenuOptions.isNotEmpty() && square.contextMenuOptions.count { it.title == "End action" } == 0) {
                snapshot.fieldSquares[activePlayerLocation as FieldCoordinate] = square.copy(
                    showContextMenu = true
                )
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
        controller: GameController,
        actions: List<GameActionDescriptor>,
    ): GameAction? {

        // When reacting to an `Undo` command, all automatic responses are disabled.
        // If not disabled, Undo'ing an action might put us in a state that will
        // automatically move us forward again, which will make it appear as the
        // Undo didn't work.
        if (controller.lastActionWasUndo()) {
            return null
        }

        val currentNode = controller.currentProcedure()?.currentNode()

        // Do not reroll successful rolls that are considered "successful"
        if (menuViewModel.isFeatureEnabled(Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS)) {
            if (actions.filterIsInstance<SelectNoReroll>().count { it.rollSuccessful == true} > 0) {
                return NoRerollSelected()
            }
        }

        // Randomly select a kicking player
        // TODO Should only do this if no-one has kick
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
        if (actions.size == 1 && actions.first() is SelectFieldLocation && currentNode is PushStep.SelectPushDirection) {
            val loc = actions.first() as SelectFieldLocation
            return FieldSquareSelected(loc.coordinate)
        }

        // When selecting block results after reroll and only 1 dice is available.
        if (currentNode == StandardBlockChooseResult.SelectBlockResult && actions.size == 1) {
            val choices = (actions.first() as SelectDicePoolResult).pools
            if (choices.size == 1 && choices.first().dice.size == 1) {
                DicePoolResultsSelected(listOf(
                    DicePoolChoice(id = 0, listOf(choices.first().dice.single().result))
                ))
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
     * of the intermediate action are unknown.
     *
     * E.g. when standing up to move, the coach might (or might not) have to
     * roll for Negatraits or just Standing Up, before being allowed to move.
     * In this case, we will register an action generator that only trigger
     * once the player can actually move.
     */
    fun registerQueuedActionGenerator(function: QueuedActionsGenerator) {
        queuedActionsGeneratorFuncs.add(function)
    }
}


