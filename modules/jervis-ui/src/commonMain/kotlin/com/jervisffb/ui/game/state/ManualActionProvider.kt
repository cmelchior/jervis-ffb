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
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOff
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushStepInitialMoveSequence
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRerollDice
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRollDice
import com.jervisffb.engine.utils.containsActionWithRandomBehavior
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.ActionWheelInputDialog
import com.jervisffb.ui.game.state.decorators.DeselectPlayerDecorator
import com.jervisffb.ui.game.state.decorators.EndActionDecorator
import com.jervisffb.ui.game.state.decorators.EndSetupDecorator
import com.jervisffb.ui.game.state.decorators.EndTurnDecorator
import com.jervisffb.ui.game.state.decorators.FieldActionDecorator
import com.jervisffb.ui.game.state.decorators.SelectBlockTypeDecorator
import com.jervisffb.ui.game.state.decorators.SelectDirectionDecorator
import com.jervisffb.ui.game.state.decorators.SelectDogoutDecorator
import com.jervisffb.ui.game.state.decorators.SelectFieldLocationDecorator
import com.jervisffb.ui.game.state.decorators.SelectMoveTypeDecorator
import com.jervisffb.ui.game.state.decorators.SelectPlayerActionDecorator
import com.jervisffb.ui.game.state.decorators.SelectPlayerDecorator
import com.jervisffb.ui.game.view.DialogFactory
import com.jervisffb.ui.game.viewmodel.Feature
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.to

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
    var automatedAction: GameAction? = null

    // If a user selected multiple actions, they are all listed here. This queue should be emptied before
    // sending anything else
    private var delayBetweenActions = false
    private val queuedActions = mutableListOf<GameAction>()
    private val queuedActionsGeneratorFuncs = mutableListOf<QueuedActionsGenerator>()
    private var sharedData: LocalFieldDataWrapper? = null

    val fieldActionDecorators = mapOf(
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
        DeselectPlayer::class to DeselectPlayerDecorator,
        EndActionWhenReady::class to EndActionDecorator,
        EndSetupWhenReady::class to EndSetupDecorator,
        EndTurnWhenReady::class to EndTurnDecorator,
        SelectBlockType::class to SelectBlockTypeDecorator,
        SelectDirection::class to SelectDirectionDecorator,
        SelectDogout::class to SelectDogoutDecorator,
        SelectFieldLocation::class to SelectFieldLocationDecorator,
        SelectMoveType::class to SelectMoveTypeDecorator,
        SelectPlayer::class to SelectPlayerDecorator,
        SelectPlayerAction::class to SelectPlayerActionDecorator,
    )

    override fun startHandler() {
        // Do nothing. We are sharing the controller with the main UiGameController
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        // Do nothing. We are sharing the controller with the main UiGameController
    }

    override fun updateSharedData(sharedData: LocalFieldDataWrapper) {
        this.sharedData = sharedData
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        this.availableActions = controller.getAvailableActions()

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
        // This also means that anyone queuing up actions, most queue up all intermediate actions
        // as well. Even the ones that are normally automatically created.
        if (queuedActions.isEmpty()) {
            automatedAction = calculateAutomaticResponse(controller, controller.getAvailableActions().actions)
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
            addModalDialogDecorators(this, acc, actions)

            // If a dialog is being shown, we do not want to enable any other kind of input until
            // the dialog has been resolved.
            if (acc.dialogInput == null) {
                addNonDialogActionDecorators(acc, actions)
            }

            // Check for the context menu. The context menu is not considered a modal dialog, so is
            // added during `addNonDialogActionDecorators()`. Thus we need to check for it here.
            // TODO This is probably the wrong architecture as we can have multiple menus like during
            acc.squares.values.firstOrNull { it.contextMenuOptions.isNotEmpty() }?.let {
                acc.dialogInput = it.createActionWheelContextMenu(game.state)
            }

            acc.dialogInput?.let { dialog ->
                if (dialog is ActionWheelInputDialog) {
                    val square = dialog.viewModel.center
                    if (square != null) {
                        acc.updateSquare(square) {
                            it.copy(
                                isActionWheelFocus = true
                            )
                        }
                    }
                }
            }
        }
    }

    override fun decorateSelectedAction(action: GameAction, acc: UiSnapshotAccumulator) {
        // If Undo'ing actions, this might happen through short-cuts and not the UI.
        // If this happens while a context menu is open, its state will be left hanging.
        // In particular `LocalFieldDataWrapper.isContentMenuVisible`. For that reason, we always
        // that state here when the action is Undo or Revert.
        // TODO In general we need to rethink the lifecycle of the Action Wheel since we want to enable
        //  animations between states in the Wheel. At that point, this logic should probably be revisited.
        if (action is Undo || action is Revert) {
            sharedData?.isContentMenuVisible = false
        }
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
                delay(150)
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

    private fun <T: GameActionDescriptor> getDecorator(type: KClass<T>): FieldActionDecorator<GameActionDescriptor>? {
        return fieldActionDecorators[type] as? FieldActionDecorator<GameActionDescriptor>
    }

    /**
     * Check if the game are in a state where we want to show a pop-up dialog in order
     * to create a [GameAction]. If yes, the data needed to build the dialog is added
     * to the UI state.
     */
    private fun addModalDialogDecorators(provider: UiActionProvider, state: UiSnapshotAccumulator, actions: ActionRequest) {
        val dialogData = DialogFactory.createDialogIfPossible(
            game,
            actions,
            provider,
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
    private fun addNonDialogActionDecorators(acc: UiSnapshotAccumulator, request: ActionRequest) {
        val state = acc.game
        request.actions.forEach { descriptor ->
            val decorator = getDecorator(descriptor::class)
            if (decorator != null) {
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
            acc.updateSquare(activePlayerLocation as FieldCoordinate) {
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
     * - Any action returned this way should also have an entry in [Feature]
     * - Except StandardBlock.RolLDice, since the ActionWheel behaves slightly different.
     */
    private fun calculateAutomaticResponse(
        controller: GameEngineController,
        actions: List<GameActionDescriptor>,
    ): GameAction? {

        // When reacting to an `Undo` command, all automatic responses are disabled.
        // If not disabled, Undo'ing an action might put us in a state that will
        // automatically move us forward again, which will make it appear as if the
        // Undo didn't work.
        if (controller.lastActionWasUndo()) {
            return null
        }

        // First, we check if we are playing Hotseat and the game is set to roll random
        // actions on the "server". In this case, they are generated here as no server exists.
        if (!gameSettings.clientSelectedDiceRolls && gameSettings.isHotseatGame && actions.containsActionWithRandomBehavior()) {
            return createRandomAction(controller.state, actions)
        }

        // If the Client is allowed to select dice rolls, this normally happens at the correct Node in the Engine,
        // but for blocks where we are using the Action Wheel, the UI gets a little wonky. So in that case, we
        // always generate the roll here, and if the coach isn't happy with the result, they can modify it. Which
        // will result in an Undo.
        if (gameSettings.clientSelectedDiceRolls && (controller.currentNode() is StandardBlockRollDice.RollDice || controller.currentNode() is StandardBlockRerollDice.ReRollDie)) {
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
        if (menuViewModel.isFeatureEnabled(Feature.END_PLAYER_ACTION_IF_ONLY_OPTION) && actions.size == 1 && actions.first() is EndActionWhenReady) {
            return EndAction
        }

        // Automatically select pushback direction when only one option is available.
        if (actions.size == 1 && actions.first() is SelectFieldLocation && actions.first().createAll().size == 1 && currentNode is PushStepInitialMoveSequence.SelectPushDirection) {
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
            menuViewModel.isFeatureEnabled(Feature.SELECT_BLOCK_TYPE_IF_ONLY_OPTION) &&
            (currentNode == BlockAction.SelectBlockType || currentNode == BlitzAction.SelectBlockType)
        ) {
            actions.filterIsInstance<SelectBlockType>().firstOrNull()?.let {
                if (it.size == 1) {
                    return BlockTypeSelected(it.types.first())
                }
            }
        }

        // Automatically select "Push into the crowd" when it is the only option
        if (
            menuViewModel.isFeatureEnabled(Feature.PUSH_PLAYER_INTO_CROWD)
            && actions.size == 1
            && actions.first().let {
                if (it is SelectDirection && it.directions.size == 1) {
                    val target = it.origin.move(it.directions.first(), 1)
                    target.isOutOfBounds(controller.rules)
                } else {
                    false
                }
            }
        ) {
            return DirectionSelected((actions.first() as SelectDirection).directions.first())
        }

        // Automatically decide to follow op (or not), if you there really isn't a choice in the matter
        if (currentNode is PushStepInitialMoveSequence.DecideToFollowUp && actions.size == 1) {
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
}


