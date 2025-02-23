package com.jervisffb.ui.game

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayer
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushContext
import com.jervisffb.ui.game.animations.AnimationFactory
import com.jervisffb.ui.game.animations.JervisAnimation
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.model.UiPlayer
import com.jervisffb.ui.game.state.QueuedActionsGenerator
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

// For games fully controlled locally. This means rules concerning timers
// are handled here.
class LocalActionProvider(
    private val engine: GameEngineController,
    private val settings: GameSettings,
    private val homeProvider: UiActionProvider,
    private val awayProvider: UiActionProvider,
): UiActionProvider() {

    private var currentProvider = homeProvider

    private var actionJob: Job? = null

    override fun startHandler() {
        homeProvider.startHandler()
        awayProvider.startHandler()
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        homeProvider.actionHandled(team, action)
        awayProvider.actionHandled(team, action)
    }

    override fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        currentProvider = if (actions.team?.isAwayTeam() == true) {
            awayProvider
        } else {
            homeProvider
        }
    }

    override fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionRequest) {
        currentProvider.decorateAvailableActions(state, actions)
    }

    override fun decorateSelectedAction(state: UiGameSnapshot, action: GameAction) {
        currentProvider.decorateSelectedAction(state, action)
    }

    override suspend fun getAction(): GameAction {
        // Figure out how settings for this work
        // Figure out logging
//        actionJob = GlobalScope.launch(CoroutineName("ActionJob")) {
//            delay(5.seconds)
//            val action = createRandomAction(controller.state, actions)
//            userActionSelected(action)
//        }

        // TODO How to handle dice rolls
//                // Hide this behind a server parameter
//        if (!gameSettings.userSelectedDiceRolls && availableActions.containsActionWithRandomBehavior()) {
//            return createRandomAction(game.state, availableActions)
//        }

        return currentProvider.getAction().also {
            actionJob?.cancel()
        }
    }

    override fun userActionSelected(action: GameAction) {
        currentProvider.userActionSelected(action)
    }

    override fun userMultipleActionsSelected(actions: List<GameAction>, delayEvent: Boolean) {
        currentProvider.userMultipleActionsSelected(actions, delayEvent)
    }

    override fun registerQueuedActionGenerator(generator: QueuedActionsGenerator) {
        currentProvider.registerQueuedActionGenerator(generator)
    }
}

/**
 * This class is the main entry point for holding the UI game state. It acts
 * as the main ViewModel in MVVM.
 *
 * It is responsible for acting as a bridge towards [GameEngineController],
 * which means it should consume all events from there as well as being the only one
 * to send UI actions back to it.
 *
 * This way, we can intercept events and states in both directions and convert them,
 * so they are suitable for being consumed by the UI.
 */
class UiGameController(
    val gameController: GameEngineController,
    val actionProvider: UiActionProvider,
    private val menuViewModel: MenuViewModel,
    private val preloadedActions: List<GameAction>
) {
    // Reference to the current rules engine state of the game
    // DO NOT modify the state on this end.
    val state: Game = gameController.state
    val rules: Rules = gameController.rules
    val diceGenerator: DiceRollGenerator = UnsafeRandomDiceGenerator() // Used by UI to create random results. Should this be somewhere else?

    // Persistent UI decorations that needs to be stored across frames
    val uiDecorations = UiGameDecorations()

    private val animationScope = CoroutineScope(CoroutineName("AnimationScope") + Dispatchers.Default)
    val gameScope = CoroutineScope(Job() + CoroutineName("GameLoopScope") + Dispatchers.Default)

    // Storing a reference to a UiGameSnap is generally a bad idea as it becomes invalid when the game loop
    // rolls over, but we only use the replay during setting up the UI. After that, we should have all consumers
    // set up correctly and the replay is not used.
    private val _uiStateFlow = MutableSharedFlow<UiGameSnapshot>(replay = 1, onBufferOverflow = BufferOverflow.SUSPEND)
    val uiStateFlow: Flow<UiGameSnapshot> = _uiStateFlow

    private val _animationFlow = MutableSharedFlow<JervisAnimation?>(onBufferOverflow = BufferOverflow.SUSPEND)
    val animationFlow: Flow<JervisAnimation?> = _animationFlow

    // Channel used by the UI to indicate when the animation is done
    val animationDone = Channel<Boolean>(capacity = Channel.Factory.RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)

    /**
     * Start the main game loop.
     *
     * This will start executing the game by setting up receiving updates from
     * [GameEngineController], process them to set up the UI as well as sending back
     * actions.
     *
     * Each execution of the loop can thus be seen as the controller of a single
     * logical "step" of the game. It will run until the game is over.
     *
     * TODO How to handle interruptions, i.e. players accidentally leaving and
     *  rejoining.
     */
    fun startGameEventLoop() {
        val controller = gameController

        // We need to start the Rules Engine first.
        // Do this outside the coroutine to ensure that `startHandler` is called correctly
        // when setting up everything.
        controller.startManualMode()
        actionProvider.startHandler()

        gameScope.launch {

            // Pre-loaded actions are used to fast-forward to an initial state.
            // We do this before starting the main loop so the UI start from
            // that state.
            // TODO Error handling here?
            preloadedActions.forEach { preloadedAction ->
                gameController.handleAction(preloadedAction)
                actionProvider.actionHandled(null, preloadedAction)
            }

            // Run main game loop
            var lastUiState: UiGameSnapshot? = null
            while (!controller.stack.isEmpty()) {

                // Read new model state
                val state = controller.state
                val delta = controller.getDelta()
                val actions = controller.getAvailableActions()

                runPreUpdateAnimations()

                // Log entries from last action should be added after the animation,
                // so we don't accidentally reveal the result too soon.

                // TODO Run Sound Decorators

                // Update UI State based on latest model state
                actionProvider.prepareForNextAction(controller, actions)
                var newUiState = createNewUiSnapshot(state, actions, delta, lastUiState)
                _uiStateFlow.emit(newUiState)

                // Detect animations and run them after updating the UI, but before making it ready
                // for the next set of actions
                runPostUpdateAnimations()

                // TODO Just changing the existing uiState might not trigger recomposition correctly
                //  We need an efficient way to copy the old one.
                actionProvider.decorateAvailableActions(newUiState, actions)
                lastUiState = newUiState
                _uiStateFlow.emit(newUiState)

                // Wait for the system to produce the next action, this can either be
                // automatically generated or come from the UI. Here we do not care where
                // it comes from.

                val userAction = actionProvider.getAction()
                println("TEST: Received user action: $userAction")

                // After an action was selected, all decorators to modify
                // the UI while the action is being processed.
                actionProvider.decorateSelectedAction(newUiState, userAction)
                _uiStateFlow.emit(newUiState)

                // Then run any animations triggered by the action (but before the state is updated)
                runPostActionAnimations(userAction)

                // Last, send action to the Rules Engine for processing.
                // This will start the next iteration of the game loop.
                // TODO Add error handling here. What to do for invalid actions?
                gameController.handleAction(userAction)
                actionProvider.actionHandled(actions.team, userAction)
            }
        }.invokeOnCompletion {
            if (it != null && it !is CancellationException) {
                throw it
            }
        }
    }

    private suspend fun runPreUpdateAnimations() {
        if (!gameController.lastActionWasUndo()) {
            val animation = AnimationFactory.getPreUpdateAnimation(state)
            if (animation != null) {
                _animationFlow.emit(animation)
                animationDone.receive()
            }
        }
    }

    private suspend fun runPostUpdateAnimations() {
        if (!gameController.lastActionWasUndo()) {
            val animation = AnimationFactory.getFrameAnimation(state, rules)
            if (animation != null) {
                _animationFlow.emit(animation)
                animationDone.receive()
            }
        }
    }

    private suspend fun runPostActionAnimations(action: GameAction) {
        if (!gameController.lastActionWasUndo()) {
            val animation = AnimationFactory.getPostActionAnimation(state, action)
            if (animation != null) {
                _animationFlow.emit(animation)
                animationDone.receive()
            }
        }
    }

    /**
     * Method responsible for updating the UI state based on recent changes.
     */
    private fun createNewUiSnapshot(state: Game, actions: ActionRequest, delta: GameDelta, lastUiState: UiGameSnapshot?): UiGameSnapshot {

        // Update the persistent UI decorations before starting
        updatePersistentUiDecorations(state, delta, uiDecorations)

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

    private fun updatePersistentUiDecorations(state: Game, delta: GameDelta, uiDecorations: UiGameDecorations) {
        if (delta.reversed) {
            uiDecorations.undo(delta.id)
            return
        }

        // Clear move markers when an action ends
        if (delta.containsCommand { it is ExitProcedure && it.procedure == ActivatePlayer }) {
            // TODO Restore path info
            // uiDecorations.registerUndo(
            //   deltaId = delta.id,
            //   action = { /* TODO */ }
            // )
            uiDecorations.resetMovesUsed()
        }


        // Track standing up so we can adjust "Move used" correctly.
        if (delta.containsAction(MoveTypeSelected(MoveType.STAND_UP))) {
            val activePlayer = state.activePlayer!!
            if (activePlayer.move >= rules.moveRequiredForStandingUp) {
                uiDecorations.addMoveUsedToStandUp(rules.moveRequiredForStandingUp)
            } else {
                uiDecorations.addMoveUsedToStandUp(activePlayer.move)
            }
        }

        // Add decoration when moving player
        // TODO Add support JUMP/LEAP
        if (
            delta.steps.firstOrNull()?.action == MoveTypeSelected(MoveType.STANDARD) &&
            delta.steps.lastOrNull()?.action is FieldSquareSelected
        ) {
            // TODO This seems to break on touch downs
            val start = delta.allCommands().filterIsInstance<SetPlayerLocation>().single().originalPlayerLocation
            uiDecorations.addMoveUsed(start)
            uiDecorations.registerUndo(
                deltaId = delta.id,
                action = { uiDecorations.removeLastMoveUsed() }
            )
        }
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

        // We add a special indicator where the ball is leaving the pitch (if it is)
        val isBallExiting: Boolean = game.balls.any {
            it.state == BallState.OUT_OF_BOUNDS && it.outOfBoundsAt == coordinate
        }

        // Add direction arrows for already selected directions during a chain push
        var directionSelected: Direction? = null
        state.getContextOrNull<PushContext>()?.let { context ->
            directionSelected = context.pushChain
                .firstOrNull { it.to == coordinate }
                ?.let { data: PushContext.PushData ->
                    Direction.from(data.from, data.to!!)
                }
        }

        return UiFieldSquare(
            square,
            isBallOnGround,
            isBallExiting,
            square.player?.hasBall() == true,
            uiPlayer,
            moveUsed = uiDecorations.getMoveUsedOrNull(coordinate),
            directionSelected = directionSelected
        )
    }

    fun userSelectedAction(action: GameAction) {
        actionProvider.userActionSelected(action)
    }

    fun userSelectedMultipleActions(actions: List<GameAction>, delayEvent: Boolean = true) {
        actionProvider.userMultipleActionsSelected(actions, delayEvent)
    }

    fun notifyAnimationDone() {
        animationScope.launch {
            animationDone.send(true)
            _animationFlow.emit(null)
        }.invokeOnCompletion {
            if (it != null && it !is CancellationException) {
                throw it
            }
        }
    }
}
