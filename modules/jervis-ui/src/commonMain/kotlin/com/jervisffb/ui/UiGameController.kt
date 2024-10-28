package com.jervisffb.ui

import com.jervisffb.engine.ActionsRequest
import com.jervisffb.engine.GameController
import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOffEvent
import com.jervisffb.engine.rules.bb2020.procedures.WeatherRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.ChangingWeather
import com.jervisffb.engine.rules.bb2020.tables.KickOffEvent
import com.jervisffb.engine.rules.bb2020.tables.TableResult
import com.jervisffb.engine.rules.bb2020.tables.Weather
import com.jervisffb.ui.model.UiFieldSquare
import com.jervisffb.ui.model.UiPlayer
import com.jervisffb.ui.screen.GameMode
import com.jervisffb.ui.state.UiActionProvider
import com.jervisffb.ui.viewmodel.MenuViewModel
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.Res
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_blitz
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_blizzard
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_brilliant_coaching
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_cheering_fans
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_get_the_ref
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_high_kick
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_nice
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_officious_ref
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_pitch_invasion
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_pouring_rain
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_quick_snap
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_solid_defence
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_sweltering_heat
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_timeout
import dk.ilios.bloodbowl.ui.jervis_ui.generated.resources.icons_animation_kickoff_kick_off_very_sunny
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource

sealed interface JervisAnimation
class KickOffEventAnimation(val image: DrawableResource): JervisAnimation
class PassAnimation(val from: OnFieldLocation, val to: FieldCoordinate) : JervisAnimation

/**
 * Class responsible for detecting if an animation should be run, and which one.
 * There are 3 places an animation can run:
 *
 * 1. At the beginning of the loop, but before the UI is updated.
 * 2. After the UI is updated, but before action decorators are used.
 * 3. After an action has been selected, but before it is applied to the model.
 */
object AnimationFactory {

    /**
     * Return animation being run at the beginning of a frame, before the
     * UI has updated to the latest state.
     */
    fun getPreUpdateAnimation(state: Game): JervisAnimation? {
        return null
    }

    /**
     * Return animation being run after the UI has been updated to the latest state,
     * but before action decorators are used.
     */
    fun getFrameAnimation(state: Game, rules: Rules): JervisAnimation? {
        val currentNode = state.currentProcedure()?.currentNode()

        // Animate kick-off
        if (state.stack.containsNode(TheKickOffEvent.ResolveBallLanding)) {
            val from = state.kickingPlayer!!.location as OnFieldLocation
            val to = state.singleBall().location
            return PassAnimation(from, to)
        }

        return null
    }

    /**
     * Returns animation being run after an action has been selected, but
     * before it is being sent to the [GameController].
     */
    fun getPostActionAnimation(state: Game, action: GameAction): JervisAnimation? {
        if (action == Undo) return null
        val currentNode = state.currentProcedure()?.currentNode()

        // Animate KickOff Event Result
        // Right now we just "guess" that the rules do the same table lookup.
        // This is pretty annoying, but there is no stable place we can check before
        // executing the event (and some events are just pure computation nodes).
        // We could also introduce a "Confirm"-node in the Rules, but doing that solely
        // to support animations is also annoying.
        if (currentNode == TheKickOffEvent.RollForKickOffEvent) {
            val roll = (action as DiceRollResults).rolls.map { it as D6Result }
            val result: TableResult = state.rules.kickOffEventTable.roll(roll.first(), roll.last())
            val image = when (result) {
                KickOffEvent.GET_THE_REF -> Res.drawable.icons_animation_kickoff_kick_off_get_the_ref
                KickOffEvent.TIME_OUT -> Res.drawable.icons_animation_kickoff_kick_off_timeout
                KickOffEvent.SOLID_DEFENSE -> Res.drawable.icons_animation_kickoff_kick_off_solid_defence
                KickOffEvent.HIGH_KICK -> Res.drawable.icons_animation_kickoff_kick_off_high_kick
                KickOffEvent.CHEERING_FANS -> Res.drawable.icons_animation_kickoff_kick_off_cheering_fans
                KickOffEvent.CHANGING_WEATHER -> null
                KickOffEvent.BRILLIANT_COACHING -> Res.drawable.icons_animation_kickoff_kick_off_brilliant_coaching
                KickOffEvent.QUICK_SNAP -> Res.drawable.icons_animation_kickoff_kick_off_quick_snap
                KickOffEvent.BLITZ -> Res.drawable.icons_animation_kickoff_kick_off_blitz
                KickOffEvent.OFFICIOUS_REF -> Res.drawable.icons_animation_kickoff_kick_off_officious_ref
                KickOffEvent.PITCH_INVASION -> Res.drawable.icons_animation_kickoff_kick_off_pitch_invasion
                else -> null
            }
            return if (image != null) {
                KickOffEventAnimation(image)
            } else {
                null
            }
        }

        // Weather changes due to Changing Weather kickoff event is reported as the final weather result
        if (currentNode == WeatherRoll.RollWeatherDice && state.stack.get(-1).currentNode() == ChangingWeather.ChangeWeather) {
            val roll = (action as DiceRollResults).rolls.map { it as D6Result }
            val result: Weather = state.rules.weatherTable.roll(roll.first(), roll.last())
            val weatherImage = when (result) {
                Weather.SWELTERING_HEAT -> Res.drawable.icons_animation_kickoff_kick_off_sweltering_heat
                Weather.VERY_SUNNY -> Res.drawable.icons_animation_kickoff_kick_off_very_sunny
                Weather.PERFECT_CONDITIONS -> Res.drawable.icons_animation_kickoff_kick_off_nice
                Weather.POURING_RAIN -> Res.drawable.icons_animation_kickoff_kick_off_pouring_rain
                Weather.BLIZZARD -> Res.drawable.icons_animation_kickoff_kick_off_blizzard
            }
            return KickOffEventAnimation(weatherImage)
        }

        return null
    }
}




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
    private val animationScope = CoroutineScope(CoroutineName("AnimationScope") + Dispatchers.Default + errorHandler)
    val gameScope = CoroutineScope(CoroutineName("GameLoopScope") + Dispatchers.Default + errorHandler)

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

                runPreUpdateAnimations()

                // Log entries from last action should be added after the animation,
                // so we don't accidentally reveal the result too soon.

                // TODO Run Sound Decorators

                // Update UI State based on latest model state
                actionProvider.prepareForNextAction(controller)
                var newUiState = createNewUiSnapshot(state, actions, delta, lastUiState)
                _uiStateFlow.emit(newUiState)

                // Detect animations and run them after updating the UI, but before making it ready
                // for the next set of actions
                runPostUpdateAnimations()

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

                // Then run any animations triggered by the action (but before the state is updated)
                runPostActionAnimations(userAction)

                // Last, send action to the Rules Engine for processing.
                // This will start the next iteration of the game loop.
                // TODO Add error handling here. What to do for invalid actions?
                // TODO This approach doesn't support UNDO very well, as Undo doesn't
                //  treat composite actions as a "whole". This probably needs to be
                //  thought a bit about
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

    private suspend fun runPreUpdateAnimations() {
        if (!controller.lastActionWasUndo) {
            val animation = AnimationFactory.getPreUpdateAnimation(state)
            if (animation != null) {
                _animationFlow.emit(animation)
                animationDone.receive()
            }
        }
    }

    private suspend fun runPostUpdateAnimations() {
        if (!controller.lastActionWasUndo) {
            val animation = AnimationFactory.getFrameAnimation(state, rules)
            if (animation != null) {
                _animationFlow.emit(animation)
                animationDone.receive()
            }
        }
    }

    private suspend fun runPostActionAnimations(action: GameAction) {
        if (!controller.lastActionWasUndo) {
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
        ).apply {
            this.directionSelected = directionSelected
        }
    }

    fun userSelectedAction(action: GameAction) { actionProvider.userActionSelected(action)}

    fun userSelectedMultipleActions(actions: List<GameAction>, delayEvent: Boolean = true) {
        actionProvider.userMultipleActionsSelected(actions, delayEvent)
    }

    fun notifyAnimationDone() {
        animationScope.launch {
            animationDone.send(true)
            _animationFlow.emit(null)
        }
    }


}
