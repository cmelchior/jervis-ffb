package com.jervisffb.ui

import com.jervisffb.engine.ActionsRequest
import com.jervisffb.engine.GameController
import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.ui.model.UiFieldSquare
import com.jervisffb.ui.model.UiPlayer
import com.jervisffb.ui.screen.GameMode
import com.jervisffb.ui.state.UiActionProvider
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


/**
 * This class is the main entry point for holding the UI game state.
 *
 * It responsible for acting as a bridge towards [com.jervisffb.engine.GameController],
 * which means it should consume all events from there as well as being the only one
 * to send UI actions back to it.
 *
 * This way, we can intercept events and states in both directions and map them
 * so they are suitable for being consumed by the UI.
 */
class UiGameController(
    private val mode: GameMode,
    val controller: GameController,
    private val menuViewModel: MenuViewModel,
    private val preloadedActions: List<GameAction>
) {
    // Reference to the current rules engine state of the game
    // DO NOT modify the state on this end.
    val state: Game = controller.state
    val rules: Rules = controller.rules
    lateinit var actionProvider: UiActionProvider

    // Scopes
    val errorHandler = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
    }
    val gameScope = CoroutineScope(CoroutineName("GameLoopScope") + Dispatchers.Default + errorHandler)

    // Storing a reference to a UiGameSnap is generally a bad idea as it becomes invalid when the game loop
    // rolls over, but we only use the replay during setting up the UI. After that, we should have all consumers
    // set up correctly and the replay is not used.
    private val _uiStateFlow = MutableSharedFlow<UiGameSnapshot>(replay = 1, onBufferOverflow = BufferOverflow.SUSPEND)
    val uiStateFlow: Flow<UiGameSnapshot> = _uiStateFlow

    /**
     * Start the main game loop.
     *
     * This will start executing the game by setting up receiving updates from
     * [GameController], process them to set up the UI as well as sending back
     * actions.
     *
     * Each execution of the loop can thus be seen as the controller of a single
     * logical "step" of the game. It will run until the game is over.
     *
     * TODO How to handle interruptions, i.e. players accidentially leaving and
     *  rejoining.
     */
    fun startGameEventLoop(uiActionFactory: UiActionProvider) {
        this.actionProvider = uiActionFactory
        gameScope.launch {

            // We need to start the Rules Engine first.
            controller.startManualMode()

            // Pre-loaded actions are used to fast-forward to an initial state.
            // We do this before starting the main loop so the UI start from
            // that state.
            // TODO Error handling here?
            preloadedActions.forEach {
                controller.processAction(it)
            }

            // Run main game loop
            var lastUiState: UiGameSnapshot? = null
            while (!controller.stack.isEmpty()) {

                // Read new model state
                val state = controller.state
                val delta = controller.getDelta()
                val actions = controller.getAvailableActions()

                // Detect animations and run them before updating the UI
                // TODO Implement two examples: Kick-off result and kick-off.
                //  This should cover our use cases and flesh out problems.

                // Run Sound Decorators

                // Update UI State based on latest model state
                actionProvider.prepareForNextAction(controller)
                var newUiState = createNewUiSnapshot(state, actions, delta, lastUiState)
                actionProvider.decorateAvailableActions(newUiState, controller.getAvailableActions())
                lastUiState = newUiState
                _uiStateFlow.emit(newUiState)

                // Wait for the system to produce the next action, this can either be
                // automatically generated or come from the UI. Here we do not care where
                // it comes from.
                val userAction = actionProvider.getAction()

                // After an action was selected, all decorators to modify
                // the UI while the action is being processed.
                actionProvider.decorateSelectedAction(newUiState, userAction)
                _uiStateFlow.emit(newUiState)

                // Last, send action to the Rules Engine for processing.
                // This will start the next iteration of the game loop.
                // TODO Add error handling here. What to do for invalid actions?
                if (userAction is CompositeGameAction) {
                    userAction.list.forEach { action: GameAction ->
                        controller.processAction(action)
                    }
                } else {
                    controller.processAction(userAction)
                }
            }
        }
    }

    /**
     * Method responsible for updating the UI state based on recent changes.
     */
    private fun createNewUiSnapshot(state: Game, actions: ActionsRequest, delta: GameDelta, lastUiState: UiGameSnapshot?): UiGameSnapshot {
        // Re-render the entire field. This feels a bit like overkill, but making it more granular
        // is going to be painful, and it doesn't look like there is a performance problem doing it.
        val squares: MutableMap<FieldCoordinate, UiFieldSquare> = mutableMapOf<FieldCoordinate, UiFieldSquare>().apply {
            (0 until rules.fieldWidth).forEach { x ->
                (0 until rules.fieldHeight).forEach { y ->
                    val coordinate = FieldCoordinate(x, y)
                    this[coordinate] = renderSquare(coordinate, state)
                }
            }
        }

        return UiGameSnapshot(state, actions, squares)
    }

    private fun renderSquare(
        coordinate: FieldCoordinate,
        game: Game,
    ): UiFieldSquare {
        val square = game.field[coordinate]
        val uiPlayer = square.player?.let { UiPlayer(it) }
        val isBallOnGround: Boolean = square.balls.any {
            (it.state != BallState.CARRIED && it.state != BallState.OUT_OF_BOUNDS) &&
                it.location.x == coordinate.x &&
                it.location.y == coordinate.y
        }
        val isBallExiting: Boolean = game.balls.any {
            it.state == BallState.OUT_OF_BOUNDS && it.outOfBoundsAt == coordinate
        }

        return UiFieldSquare(
            square,
            isBallOnGround,
            isBallExiting,
            square.player?.hasBall() == true,
            uiPlayer,
        )
    }

    fun userSelectedAction(action: GameAction) { actionProvider.userActionSelected(action)}

    fun userSelectedMultipleActions(actions: List<GameAction>, delayEvent: Boolean = true) {
        actionProvider.userMultipleActionsSelected(actions, delayEvent)
    }
}
