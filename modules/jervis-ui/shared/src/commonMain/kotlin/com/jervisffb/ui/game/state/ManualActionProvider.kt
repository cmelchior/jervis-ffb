package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.NodeStep
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectPassType
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayers
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.bb2025.procedures.actions.move.JumpStep
import com.jervisffb.engine.bb2025.procedures.actions.move.LeapStep
import com.jervisffb.engine.bb2025.procedures.actions.move.PogoStep
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.procedures.ActivatePlayer
import com.jervisffb.engine.rules.common.procedures.actions.move.StandardMoveStep
import com.jervisffb.engine.utils.containsActionWithRandomBehavior
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.actionwheel.ActionWheelDialogController
import com.jervisffb.ui.game.state.decorators.CancelDecorator
import com.jervisffb.ui.game.state.decorators.EndActionDecorator
import com.jervisffb.ui.game.state.decorators.EndSetupDecorator
import com.jervisffb.ui.game.state.decorators.EndTurnDecorator
import com.jervisffb.ui.game.state.decorators.PitchActionDecorator
import com.jervisffb.ui.game.state.decorators.SelectDirectionDecorator
import com.jervisffb.ui.game.state.decorators.SelectDogoutDecorator
import com.jervisffb.ui.game.state.decorators.SelectMoveTypeDecorator
import com.jervisffb.ui.game.state.decorators.SelectPassTypeDecorator
import com.jervisffb.ui.game.state.decorators.SelectPitchLocationDecorator
import com.jervisffb.ui.game.state.decorators.SelectPlayerDecorator
import com.jervisffb.ui.game.state.decorators.SelectPlayersDecorator
import com.jervisffb.ui.game.state.decorators.SelectRandomPlayersDecorator
import com.jervisffb.ui.game.view.DialogFactory
import com.jervisffb.ui.game.viewmodel.Feature
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

/**
 * Class responsible for enhancing the UI, so it is able to create a [GameAction].
 *
 * See [UiGameController.startGameEventLoop]
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

    val automatedActionsFactory = AutomatedActionsFactory(game, menuViewModel, gameSettings)

    // If set, it contains an action that should automatically be sent on the next call to getAction()
    var automatedAction: GameAction? = null

    // If a user selected multiple actions, they are all listed here. This queue should be emptied before
    // sending anything else
    private var delayBetweenActions = false
    private val queuedActions = mutableListOf<GameAction>()
    private val queuedActionsGeneratorFuncs = mutableListOf<QueuedActionsGenerator>()
    private var sharedData: LocalPitchDataWrapper? = null

    var nextFumblerooskiCommand: GameAction? = null
        private set

    private val pitchActionDecorators = mapOf(
        // EndSetupWhenReady -> TODO()
        // EndTurnWhenReady -> TODO()
        // is RollDice -> TODO()
        // is SelectBlockType -> TODO()
        // SelectCoinSide -> TODO()
        // is SelectDicePoolResult -> TODO()
        // is SelectInducement -> TODO()
        // is SelectNoReroll -> TODO()
        // is SelectRandomPlayers -> TODO()
        // is SelectRerollOption -> TODO()
        // is SelectSkill -> TODO()
        // TossCoin -> TODO()
        // DeselectPlayer::class to DeselectPlayerDecorator,
        // SelectPlayerAction::class to SelectPlayerActionDecorator,
        CancelWhenReady::class to CancelDecorator,
        EndActionWhenReady::class to EndActionDecorator,
        EndSetupWhenReady::class to EndSetupDecorator,
        EndTurnWhenReady::class to EndTurnDecorator,
        SelectDirection::class to SelectDirectionDecorator,
        SelectDogout::class to SelectDogoutDecorator,
        SelectMoveType::class to SelectMoveTypeDecorator,
        SelectPassType::class to SelectPassTypeDecorator,
        SelectPitchLocation::class to SelectPitchLocationDecorator,
        SelectPlayer::class to SelectPlayerDecorator,
        SelectPlayers::class to SelectPlayersDecorator,
        SelectRandomPlayers::class to SelectRandomPlayersDecorator,
    )

    private var nodeToActionWheelController: Map<Node, ActionWheelDialogController> = emptyMap()

    override fun init(controller: UiGameController) {
        nodeToActionWheelController = buildMap {
            controller.actionWheelControllers.forEach { controller ->
                controller.nodes.forEach { node ->
                    this[node] = controller
                }
            }
        }
    }

    override fun startHandler() {
        // Do nothing. We are sharing the controller with the main UiGameController
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        // Do nothing. We are sharing the controller with the main UiGameController
    }

    override fun updateSharedData(sharedData: LocalPitchDataWrapper) {
        this.sharedData = sharedData
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        this.availableActions = controller.getAvailableActions()


        val searchNode = NodeStep(ActivatePlayer, Procedure.ExitProcedureNode::class)
        val endActivation = controller.getDelta().steps.any {
            it.intermediateNodes.contains(searchNode)
        }
        if (endActivation) {
            resetFumblerooskiCommand()
        }

        // If the UI has registered any queued action generators, we run them first before
        // trying to find other automated actions.
        val iter = queuedActionsGeneratorFuncs.iterator()
        while (iter.hasNext()) {
            val result = iter.next()(controller)
            if (result != null) {
                delayBetweenActions = result.delayBetweenActions
                queuedActions.addAll(result.actions)
                iter.remove()
            }
        }

        // We only want to check for other automated settings if no queued up actions exist.
        // This also means that anyone queuing up actions, must queue up all intermediate actions
        // as well. Even the ones that are normally automatically created.
        if (queuedActions.isEmpty()) {
            automatedAction = calculateAutomaticResponse(controller.getAvailableActions())
        }
    }

    override fun decorateAvailableActions(actions: ActionRequest, acc: UiSnapshotAccumulator) {
        availableActions = actions
        if (queuedActions.isNotEmpty()) return
        if (automatedAction != null) return

        // TODO What to do here when it is the other team having its turn.
        //  The behavior will depend on the game being a HotSeat vs. Client/Server
        var showActionDecorators = when (clientMode) {
            TeamActionMode.HOME_TEAM -> actions.team == null || actions.team?.id == game.state.homeTeam.id
            TeamActionMode.AWAY_TEAM -> actions.team?.id == game.state.awayTeam.id
            TeamActionMode.ALL_TEAMS -> true
        }

        // If the available actions are random, we only want to show controls for the UI if configured so.
        // This is mostly a developer or custom play setting and not something commonly used for normal games.
        if (actions.containsActionWithRandomBehavior() && !gameSettings.clientSelectedDiceRolls) {
            showActionDecorators = false
        }

        if (showActionDecorators) {
            addActionWheelDecorators(this, acc, actions)
            val actionWheelVisible = acc.gameController.currentNode()?.let { nodeToActionWheelController.contains(it) } ?: false
            if (!actionWheelVisible) {
                addModalDialogDecorators(this, acc, actions)
            }
            // If a dialog is being shown, we do not want to enable any other kind of input until
            // the dialog has been resolved.
            if (acc.dialogInput == null && !actionWheelVisible) {
                addNonDialogActionDecorators(acc, actions)
            }
        }
    }

    override fun decorateSelectedAction(action: GameAction, acc: UiSnapshotAccumulator) {
        // Do nothing
    }

    override suspend fun getAction(): GameAction {
        // When returning actions we resolve it with the following priority
        // 1. All Queued actions
        // 2. Then automated actions
        // 3. Actions from the UI

        // Empty queued data if present
        if (queuedActions.isNotEmpty()) {
            val action = queuedActions.removeFirst()
            // Do not pause for flow-control events, only events that would appear "visible"
            // to the player
            if (action !is MoveTypeSelected && delayBetweenActions) {
                delay(150.milliseconds)
            }
            return action
        }
        delayBetweenActions = false

        // Otherwise empty automated response
        // And finally ask the UI
        return automatedAction?.let { action ->
            automatedAction = null
            action
        } ?: actionSelectedChannel.receive()
    }

    override fun userActionSelected(action: GameAction) {
        if (actionSelectedChannel.trySend(action).isFailure) {
            error("Unable to send action to channel. Is the channel closed?")
        }
    }

    override fun userMultipleActionsSelected(actions: List<GameAction>, delayEvent: Boolean) {
        if (actions.isEmpty()) throw IllegalArgumentException("Action list must contain at least one action")
        // Store all events to be sent and sent the first one to be processed
        queuedActions.addAll(actions)
        delayBetweenActions = delayEvent
        actionScope.launch {
            val action = queuedActions.removeFirst()
            actionSelectedChannel.send(action)
        }
    }

    private fun <T: GameActionDescriptor> getDecorator(type: KClass<T>): PitchActionDecorator<GameActionDescriptor>? {
        @Suppress("UNCHECKED_CAST")
        return pitchActionDecorators[type] as? PitchActionDecorator<GameActionDescriptor>
    }

    /**
     * Check if the game are in a state where we want to show an Action Wheel in order
     * to create a [GameAction].
     *
     * Action Wheels at this stage are assumed to be visible from the start.
     * Similar to how a modal dialog would be.
     *
     * If they are optional they are instead configured as [com.jervisffb.ui.game.view.ContextMenuOption]
     * using a [PitchActionDecorator].
     *
     * Because ActionWheels sometimes have multiple steps e.g. roll-reroll-roll, we need to handle
     * transitions between these. This is why action wheel lifecycles and their setup at each stage
     * are controlled by a [com.jervisffb.ui.game.state.actionwheel.ActionWheelDialogController] subclass
     * which encapsulates the entire lifecycle, including both forward and backward transactions
     */
    private fun addActionWheelDecorators(
        provider: UiActionProvider,
        acc: UiSnapshotAccumulator,
        actions: ActionRequest,
    ) {
        val currentNode = acc.gameController.currentNode()
        nodeToActionWheelController[currentNode]?.onDecorateActions(
            acc,
            provider,
            actions,
            sharedData!!
        )
    }

    /**
     * Check if the game are in a state where we want to show a pop-up dialog in order
     * to create a [GameAction]. If yes, the data needed to build the dialog is added
     * to the UI state.
     */
    private fun addModalDialogDecorators(provider: UiActionProvider, state: UiSnapshotAccumulator, actions: ActionRequest) {
        // First configure "standard" modal dialogs
        val dialogData = DialogFactory.createDialogIfPossible(
            game,
            actions,
            provider,
            sharedData!!,
            state,
            { actionDescriptors-> mapUnknownAction(actionDescriptors.actions) }
        )
        if (state.dialogInput != null) {
            error("Only 1 dialog is allowed. Dialog already configured: ${state.dialogInput}")
        }
        when (dialogData) {
            else -> {
                state.dialogInput = dialogData
            }
        }
    }

    /**
     * Modify the UI state, so it is ready to accept user input in order to generate the next
     * [GameAction]. This mostly means adding click-listeners, but also modify the UI so it is
     * visible that UI elements can be interacted with.
     */
    // TODO Should probably refactor this so every case is in its own function. Perhaps move to a separate
    //  class to make it more explicit?
    private fun addNonDialogActionDecorators(acc: UiSnapshotAccumulator, request: ActionRequest) {
        val state = acc.game
        request.actions.forEach { descriptor ->
            val decorator = getDecorator(descriptor::class)
            if (decorator != null && decorator.isApplicable(acc.game, request)) {
                decorator.decorate(this, state, descriptor, request.team, acc)
            } else {
                // Any action that isn't being mapped to an UI component needs to go here.
                // This way, we ensure that the UI is never blocked during development.
                // In an ideal world, nothing should ever go here.
                mapUnknownAction(descriptor).forEach { acc.addUnknownAction(it) }
            }
        }

        // Choosing whether to showing the context menu is a bit complicated.
        // So we cannot decide this until all available actions have been processed.
        // But we employ the rule that if one of the actions is a "main" action, it means
        // the player was just selected, and we should show the context menu up front.
        // Otherwise, it means that the player is in the middle of their action and we should
        // not show the context menu up front. That should be up to the player
        state.activePlayer?.location?.let { activePlayerLocation ->
            acc.updateSquare(activePlayerLocation as PitchCoordinate) {
                if (it.contextMenuOptions.isNotEmpty() && it.contextMenuOptions.none { it.title == "End action" }) {
                    it.copy(
                        showContextMenu = true
                    )
                } else {
                    it
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
     * - Any action returned this way should also have an entry in [Feature], so the User can toggle the behavior.
     */
    private fun calculateAutomaticResponse(
        actions: ActionRequest,
    ): GameAction? {
        val currentNode = game.currentNode()

        // When reacting to an `Undo` command, all automatic responses are disabled.
        // If not disabled, Undo'ing an action might put us in a state that will
        // automatically move us forward again, which will make it appear as if the
        // Undo didn't work.
        if (game.lastActionWasUndo()) {
            return null
        }

        // The option for selecting Fumblerooskie is done in a slightly different place in the UI, so we need to check
        // if we should provide the response here.
        if (currentNode == StandardMoveStep.ChooseToUseFumblerooski
            || currentNode == JumpStep.ChooseToUseFumblerooskiAfterJumpingToTargetSquare
            || currentNode == LeapStep.ChooseToUseFumblerooskiAfterLeapingToTargetSquare
            || currentNode == PogoStep.ChooseToUseFumblerooskiAfterPogoToTargetSquare
        ) {
            val nextAction = nextFumblerooskiCommand
            nextFumblerooskiCommand = null
            sharedData?.uiDecorations?.useFumblerooskiOnNextMove(null)
            return nextAction ?: Cancel
        }

        return automatedActionsFactory.createAction(actions)
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
        // This is currently only used to filter Game Status Messages, so we do not
        // need to consider Fumblerooski (for now)
        return automatedAction != null || queuedActions.isNotEmpty()
    }

    // Fumblerooski is selected outside its normal place in the Rules Engine.
    // Set the return value here.
    // TODO This is effectively a "future" action, i.e. not just the next one.
    //  Consider if we need a proper API for this kind of thing, but wait until
    //  we have more cases than Fumblerooski
    fun nextFumblerooskiCommand(player: Player, action: GameAction?) {
        nextFumblerooskiCommand = action
        if (action == Confirm) {
            sharedData?.uiDecorations?.useFumblerooskiOnNextMove(player)
        } else {
            sharedData?.uiDecorations?.useFumblerooskiOnNextMove(null)
        }
    }

    fun resetFumblerooskiCommand() {
        nextFumblerooskiCommand = null
        sharedData?.uiDecorations?.useFumblerooskiOnNextMove(null)
    }
}


