package com.jervisffb.ui.state

import com.jervisffb.engine.ActionsRequest
import com.jervisffb.engine.GameController
import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.InducementSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectCoinSide
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectInducement
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.SelectSkill
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.actions.TossCoin
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.rules.PlayerSpecialActionType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOff
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushStep
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult
import com.jervisffb.ui.UiGameController
import com.jervisffb.ui.UiGameSnapshot
import com.jervisffb.ui.view.ContextMenuOption
import com.jervisffb.ui.view.DialogFactory
import com.jervisffb.ui.viewmodel.Feature
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class ManualActionProvider(
    private val uiState: UiGameController,
    private val menuViewModel: MenuViewModel
): UiActionProvider() {

    private lateinit var controller: GameController
    private lateinit var actions: ActionsRequest

    // If set, it contains an action that should automatically be sent on the next call to getAction()
    var automatedAction: GameAction? = null

    // If a user selected multiple actions, they are all listed here. This queue should be emptied before
    // sending anything else
    var delayEvents = false
    val queuedActions = mutableListOf<GameAction>()

    override fun prepareForNextAction(controller: GameController) {
        this.controller = controller
        this.actions = controller.getAvailableActions()
        if (queuedActions.isEmpty()) {
            automatedAction = calculateAutomaticResponse(controller, controller.getAvailableActions().actions)
        }
    }

    override fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionsRequest) {
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
            if (action !is MoveTypeSelected && delayEvents) {
                delay(150)
            }
            return action
        }
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
        delayEvents = delayEvent
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
    private fun addDialogDecorators(state: UiGameSnapshot, actions: ActionsRequest) {
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
    private fun addNonDialogActionDecorators(snapshot: UiGameSnapshot, request: ActionsRequest) {
        request.actions.forEach { action ->
            when (action) {
                is DeselectPlayer -> {
                    val coordinate = action.player.location as FieldCoordinate
                    snapshot.fieldSquares[coordinate] = snapshot.fieldSquares[coordinate]?.copy(
                        onMenuHidden = { userActionSelected(PlayerDeselected(action.player)) }
                    ) ?: error ("Could not find square: $coordinate")
                }
                EndActionWhenReady -> {
                    snapshot.game.activePlayer?.location?.let { location ->
                        snapshot.fieldSquares[location as FieldCoordinate] = snapshot.fieldSquares[location]?.copyAddContextMenu(
                            ContextMenuOption(
                                "End action",
                                { userActionSelected(EndAction) },
                            )
                        ) ?: error("Could not find square: $location")
                    } ?: error("No active player")
                }
                is SelectDirection -> {
                    val origin = snapshot.game.field[action.origin as FieldCoordinate]
                    action.directions.forEach { direction ->
                        val square = snapshot.fieldSquares[origin.move(direction, 1)]
                        snapshot.fieldSquares[origin.move(direction, 1)] = square?.copy(
                            onSelected = { userActionSelected(DirectionSelected(direction)) },
                            selectableDirection = direction
                        ) ?: error("Cannot find square: ${origin.move(direction, 1)}")
                    }
                }
                is SelectFieldLocation -> {
                    val selectedAction = {
                        userActionSelected(FieldSquareSelected(action.coordinate))
                    }
                    val square = snapshot.fieldSquares[action.coordinate]
                    snapshot.fieldSquares[action.coordinate] = square?.copy(
                        onSelected = selectedAction,
                        requiresRoll = (action.requiresRush || action.requiresDodge)
                    ) ?: error("Unexpected location : ${action.coordinate}")
                }
                is SelectMoveType -> {
                    val player = snapshot.game.activePlayer ?: error("No active player")
                    val activeLocation = player.location as OnFieldLocation
                    val activeSquare = snapshot.fieldSquares[activeLocation] ?: error("No square found: $activeLocation")
                    // For move selection, some types of moves we want to display on the field
                    // others should be a specific action that must be selected.
                    // On-field moves are shortcutting the Rules engine, so we need to account for that as well
                    when (action.type) {
                        MoveType.JUMP -> {
                            activeSquare.contextMenuOptions.add(
                                ContextMenuOption(
                                    "Jump",
                                    { userActionSelected(MoveTypeSelected(MoveType.JUMP)) },
                                )
                            )
                        }
                        MoveType.LEAP -> {
                            activeSquare.contextMenuOptions.add(
                                ContextMenuOption(
                                    "Leap",
                                    { userActionSelected(MoveTypeSelected(MoveType.LEAP)) },
                                )
                            )
                        }
                        MoveType.STANDARD -> {
                            val requiresDodge = controller.rules.calculateMarks(controller.state, player.team, activeLocation) > 0
                            val requiresRush = player.movesLeft == 0 && player.rushesLeft > 0

                            // We calculate all paths here, rather than doing it in the ViewModel. Mostly because
                            // it allows us to front-load slightly more computations. But it hasn't been benchmarked,
                            // Maybe doing the calculation on the fly is fine.
                            val allPaths = controller.rules.pathFinder.calculateAllPaths(
                                controller.state,
                                activeLocation as FieldCoordinate,
                                if (requiresDodge) 1 else player.movesLeft,
                            )
                            snapshot.pathFinder = allPaths

                            // Also mark all fields around the player as immediately selectable
                            activeLocation.getSurroundingCoordinates(snapshot.game.rules, 1, includeOutOfBounds = false)
                                .filter { snapshot.game.field[it].isUnoccupied() }
                                .forEach { loc ->
                                    val square = snapshot.fieldSquares[loc]
                                    snapshot.fieldSquares[loc] = square?.copy(
                                        onSelected = {
                                            userActionSelected(CompositeGameAction(
                                                listOf(
                                                    MoveTypeSelected(MoveType.STANDARD),
                                                    FieldSquareSelected(loc)
                                                )
                                            ))
                                        },
                                        requiresRoll = requiresDodge || requiresRush
                                    ) ?: error("Could not find square: $loc")
                                }
                        }

                        MoveType.STAND_UP -> {
                            activeSquare.contextMenuOptions.add(
                                ContextMenuOption(
                                    "Stand-Up",
                                    { userActionSelected(MoveTypeSelected(MoveType.JUMP)) },
                                )
                            )
                        }
                    }
                }
                is SelectPlayer -> {
                    // Define onClick event
                    val selectedAction = {
                        userActionSelected(PlayerSelected(action.player))
                    }

                    // Depending on the location, the event is tracked slightly different
                    when (val location = snapshot.game.getPlayerById(action.player).location) {
                        DogOut -> {
                            snapshot.dogoutActions[action.player] = selectedAction
                        }
                        is FieldCoordinate -> {
                            val square = snapshot.fieldSquares[location]
                            snapshot.fieldSquares[location] = square?.copy(
                                onSelected = selectedAction
                            ) ?: error("Unexpected player location : $location")
                        }
                        is GiantLocation -> TODO("Not supported right now")
                    }

                    // TODO Other UI modifications, like dice decoration
                }
                is SelectPlayerAction -> {
                    snapshot.game.activePlayer?.location?.let { location ->
                        snapshot.fieldSquares[location]?.let { activePlayerSquare ->
                            activePlayerSquare.contextMenuOptions.add(
                                action.action.let {
                                    val name = when (it.type) {
                                        PlayerStandardActionType.MOVE -> "Move"
                                        PlayerStandardActionType.PASS -> "Pass"
                                        PlayerStandardActionType.HAND_OFF -> "Hand-off"
                                        PlayerStandardActionType.BLOCK -> "Block"
                                        PlayerStandardActionType.BLITZ -> "Blitz"
                                        PlayerStandardActionType.FOUL -> "Foul"
                                        PlayerStandardActionType.SPECIAL -> "Special"
                                        PlayerStandardActionType.THROW_TEAM_MATE -> "Throw Team-mate"
                                        PlayerSpecialActionType.BALL_AND_CHAIN -> "Ball & Chain"
                                        PlayerSpecialActionType.BOMBARDIER -> "Bombardier"
                                        PlayerSpecialActionType.BREATHE_FIRE -> "Breathe Fire"
                                        PlayerSpecialActionType.CHAINSAW -> "Chainsaw"
                                        PlayerSpecialActionType.HYPNOTIC_GAZE -> "Hypnotic Gaze"
                                        PlayerSpecialActionType.KICK_TEAM_MATE -> "Kick Team-mate"
                                        PlayerSpecialActionType.MULTIPLE_BLOCK -> "Multiple Block"
                                        PlayerSpecialActionType.PROJECTILE_VOMIT -> "Projectile Vomit"
                                        PlayerSpecialActionType.STAB -> "Stab"
                                    }
                                    ContextMenuOption(
                                        title = name,
                                        command = { userActionSelected(PlayerActionSelected(it.type)) },
                                    )
                                },
                            )
                        } ?: error("Could not find square: $location")
                    } ?: error("No active player")
                }
//                EndSetupWhenReady -> TODO()
//                EndTurnWhenReady -> TODO()
//                is RollDice -> TODO()
//                is SelectBlockType -> TODO()
//                SelectCoinSide -> TODO()
//                is SelectDicePoolResult -> TODO()
//                SelectDogout -> TODO()
//                is SelectInducement -> TODO()
//                is SelectNoReroll -> TODO()
//                is SelectRandomPlayers -> TODO()
//                is SelectRerollOption -> TODO()
//                is SelectSkill -> TODO()
//                TossCoin -> TODO()
                else -> {
                    // Any action that isn't being mapped to an UI component needs to go here.
                    // This way, we ensure that the UI is never blocked during development.
                    // In an ideal world, nothing should ever go here.
                    snapshot.unknownActions.add(
                        mapUnknownAction(action)
                    )
                }
            }
        }

        // Choosing whether or not to showing the context menu is a bit complicated.
        // So we cannot decide this until all available actions have been processed.
        // But we employ the rule that if one of the actions is a "main" action, it means
        // the player was just selected, and we should show the context menu up front.
        // Otherwise, it means that the player is in the middle of their action and we should
        // not show the context menu up front. That should be up to the player
        snapshot.game.activePlayer?.location?.let { activePlayerLocation ->
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
    private fun mapUnknownAction(action: ActionDescriptor): GameAction {
        return when (action) {
            CancelWhenReady -> Cancel
            ConfirmWhenReady -> Confirm
            ContinueWhenReady -> Continue
            is DeselectPlayer -> PlayerDeselected(action.player)
            EndActionWhenReady -> EndAction
            EndSetupWhenReady -> EndSetup
            EndTurnWhenReady -> EndTurn
            is RollDice -> {
                val rolls =
                    action.dice.map {
                        when (it) {
                            Dice.D2 -> D2Result()
                            Dice.D3 -> D3Result()
                            Dice.D4 -> D4Result()
                            Dice.D6 -> D6Result()
                            Dice.D8 -> D8Result()
                            Dice.D12 -> D12Result()
                            Dice.D16 -> D16Result()
                            Dice.D20 -> D20Result()
                            Dice.BLOCK -> DBlockResult()
                        }
                    }
                if (rolls.size == 1) {
                    rolls.first()
                } else {
                    DiceRollResults(rolls)
                }
            }
            is SelectBlockType -> BlockTypeSelected(action.type)
            SelectCoinSide -> {
                when (Random.nextInt(2)) {
                    0 -> CoinSideSelected(Coin.HEAD)
                    1 -> CoinSideSelected(Coin.TAIL)
                    else -> throw IllegalStateException("Unsupported value")
                }
            }
            is SelectDicePoolResult -> {
                DicePoolResultsSelected(action.pools.map { pool ->
                    DicePoolChoice(pool.id, pool.dice.shuffled().subList(0, pool.selectDice).map { it.result })
                })
            }
            SelectDogout -> DogoutSelected
            is SelectFieldLocation -> FieldSquareSelected(action.x, action.y)
            is SelectInducement -> InducementSelected(action.id)
            is SelectMoveType -> MoveTypeSelected(action.type)
            is SelectNoReroll -> NoRerollSelected(action.dicePoolId)
            is SelectPlayer -> PlayerSelected(action.player)
            is SelectPlayerAction -> PlayerActionSelected(action.action.type)
            is SelectRandomPlayers -> {
                RandomPlayersSelected(action.players.shuffled().subList(0, action.count))
            }
            is SelectRerollOption -> RerollOptionSelected(action.option)
            is SelectSkill -> SkillSelected(action.skill)
            TossCoin -> {
                when (Random.nextInt(2)) {
                    0 -> CoinTossResult(Coin.HEAD)
                    1 -> CoinTossResult(Coin.TAIL)
                    else -> throw IllegalStateException("Unsupported value")
                }
            }

            is SelectDirection -> action.directions.random().let { DirectionSelected(it) }
        }
    }

    private fun mapUnknownAction(actions: List<ActionDescriptor>): List<GameAction> {
        return actions.map { mapUnknownAction(it) }
    }

    /**
     * Check if we can respond automatically to an event without having to involve the user.
     *
     * Some requirements:
     * - Any action returned this way should also have an entry in [Feature]
     */
    private fun calculateAutomaticResponse(
        controller: GameController,
        actions: List<ActionDescriptor>,
    ): GameAction? {

        // When reacting to an `Undo` command, all automatic responses are disabled.
        // If not disabled, Undo'ing an action might put us in a state that will
        // automatically move us forward again, which will make it appear as the
        // Undo didn't work.
        if (controller.lastActionWasUndo) {
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
                .random().let {
                    PlayerSelected((it as SelectPlayer).player)
                }
        }

        // If a team has no further actions, just end their turn immediately
        if (actions.size == 1 && actions.first() is EndActionWhenReady) {
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
}
