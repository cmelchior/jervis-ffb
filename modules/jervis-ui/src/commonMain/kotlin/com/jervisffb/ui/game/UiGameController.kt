package com.jervisffb.ui.game

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.DevModeGameAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.ActivatePlayer
import com.jervisffb.engine.rules.common.procedures.StartOfDriveSequence
import com.jervisffb.engine.rules.common.procedures.actions.move.StandardMoveStep
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.InvalidActionException
import com.jervisffb.ui.game.animations.AnimationFactory
import com.jervisffb.ui.game.animations.JervisAnimation
import com.jervisffb.ui.game.model.UiFieldPlayer
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.state.actionwheel.AccuracyBB2020WheelController
import com.jervisffb.ui.game.state.actionwheel.AccuracyBB2025PassWheelController
import com.jervisffb.ui.game.state.actionwheel.AccuracyBB2025ThrowTeamMateWheelController
import com.jervisffb.ui.game.state.actionwheel.ArgueTheCallWheelController
import com.jervisffb.ui.game.state.actionwheel.BoneHeadWheelController
import com.jervisffb.ui.game.state.actionwheel.BounceRollWheelController
import com.jervisffb.ui.game.state.actionwheel.BreatheFireWheelController
import com.jervisffb.ui.game.state.actionwheel.CatchWheelController
import com.jervisffb.ui.game.state.actionwheel.DauntlessWheelController
import com.jervisffb.ui.game.state.actionwheel.DeviateRollWheelController
import com.jervisffb.ui.game.state.actionwheel.DodgeWheelController
import com.jervisffb.ui.game.state.actionwheel.FollowUpWheelController
import com.jervisffb.ui.game.state.actionwheel.FoulAppearanceWheelController
import com.jervisffb.ui.game.state.actionwheel.InterceptionWheelController
import com.jervisffb.ui.game.state.actionwheel.JumpUpWheelController
import com.jervisffb.ui.game.state.actionwheel.JumpWheelController
import com.jervisffb.ui.game.state.actionwheel.LandingWheelController
import com.jervisffb.ui.game.state.actionwheel.LeapWheelController
import com.jervisffb.ui.game.state.actionwheel.PickupWheelController
import com.jervisffb.ui.game.state.actionwheel.PogoWheelController
import com.jervisffb.ui.game.state.actionwheel.ProjectileVomitWheelController
import com.jervisffb.ui.game.state.actionwheel.ReallyStupidWheelController
import com.jervisffb.ui.game.state.actionwheel.RushWheelController
import com.jervisffb.ui.game.state.actionwheel.ScatterRollWheelController
import com.jervisffb.ui.game.state.actionwheel.SecureTheBallWheelController
import com.jervisffb.ui.game.state.actionwheel.SelectBlockTypeWheelController
import com.jervisffb.ui.game.state.actionwheel.SelectPlayerActionWheelController
import com.jervisffb.ui.game.state.actionwheel.ShadowingWheelController
import com.jervisffb.ui.game.state.actionwheel.StandardBlockChooseResultOrRerollWheelController
import com.jervisffb.ui.game.state.actionwheel.StandardBlockRollWheelController
import com.jervisffb.ui.game.state.actionwheel.SteadyFootingWheelController
import com.jervisffb.ui.game.state.actionwheel.TakeRootWheelController
import com.jervisffb.ui.game.state.actionwheel.UnchannelledFuryWheelController
import com.jervisffb.ui.game.state.actionwheel.UseApothecaryWheelController
import com.jervisffb.ui.game.state.actionwheel.UseBigHandWheelController
import com.jervisffb.ui.game.state.actionwheel.UseBlockWheelController
import com.jervisffb.ui.game.state.actionwheel.UseDirtyPlayerWheelController
import com.jervisffb.ui.game.state.actionwheel.UseDivingCatchWheelController
import com.jervisffb.ui.game.state.actionwheel.UseDodgeWheelController
import com.jervisffb.ui.game.state.actionwheel.UseEyeGougeWheelController
import com.jervisffb.ui.game.state.actionwheel.UseFendWheelController
import com.jervisffb.ui.game.state.actionwheel.UseGrabWheelController
import com.jervisffb.ui.game.state.actionwheel.UseHitAndRunWheelController
import com.jervisffb.ui.game.state.actionwheel.UseKickWheelController
import com.jervisffb.ui.game.state.actionwheel.UseLeapWheelController
import com.jervisffb.ui.game.state.actionwheel.UseLoneFoulerWheelController
import com.jervisffb.ui.game.state.actionwheel.UseMightyBlowController
import com.jervisffb.ui.game.state.actionwheel.UseSafePairOfHandsWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSafePassWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSidestepWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSneakyGitWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSprintWheelController
import com.jervisffb.ui.game.state.actionwheel.UseStandFirmWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSteadyFootingWheelController
import com.jervisffb.ui.game.state.actionwheel.UseStripBallWheelController
import com.jervisffb.ui.game.state.actionwheel.UseStrongArmWheelController
import com.jervisffb.ui.game.state.actionwheel.UseSureHandsWheelController
import com.jervisffb.ui.game.state.actionwheel.UseTackleWheelController
import com.jervisffb.ui.game.state.actionwheel.UseTauntWheelController
import com.jervisffb.ui.game.state.actionwheel.UseThickSkullWheelController
import com.jervisffb.ui.game.state.actionwheel.UseVeryLongLegsWheelController
import com.jervisffb.ui.game.state.actionwheel.UseWrestleWheelController
import com.jervisffb.ui.game.state.indicators.BallCarriedStatusIndicator
import com.jervisffb.ui.game.state.indicators.BallExitStatusIndicator
import com.jervisffb.ui.game.state.indicators.BallOnGroundStatusIndicator
import com.jervisffb.ui.game.state.indicators.BlockStatusIndicator
import com.jervisffb.ui.game.state.indicators.DirectionArrowStatusIndicator
import com.jervisffb.ui.game.state.indicators.FieldStatusIndicator
import com.jervisffb.ui.game.state.indicators.MoveUsedStatusIndicator
import com.jervisffb.ui.game.state.indicators.TeamFeatureStatusIndicator
import com.jervisffb.ui.game.state.indicators.TeamRerollStatusIndicator
import com.jervisffb.ui.game.state.indicators.TeamSetupsAvailableStatusIndicator
import com.jervisffb.ui.game.view.ActionWheelUiState
import com.jervisffb.ui.game.view.GameStatusMessageFactory
import com.jervisffb.ui.game.view.HideActionWheel
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.utils.FrameRateAverager
import com.jervisffb.utils.jervisLogger
import com.jervisffb.utils.singleThreadDispatcher
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
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
    // Which Teams are controlled through this UI controller.
    // This mostly affects UNDO.
    val uiMode: TeamActionMode,
    val gameController: GameEngineController,
    val actionProvider: UiActionProvider,
    val menuViewModel: MenuViewModel,
    private val preloadedActions: List<GameAction>
) {

    companion object {
        private val LOG = jervisLogger()
    }

    // Reference to the current rules engine state of the game
    // DO NOT modify the state on this end.
    val state: Game = gameController.state
    val rules: Rules = gameController.rules
    val diceGenerator: DiceRollGenerator = UnsafeRandomDiceGenerator() // Used by UI to create random results. Should this be somewhere else?

    // Persistent UI decorations that need to be stored across actions
    val uiDecorations = UiPersistentGameIndicators()
    private val fieldStatusIndicators: List<FieldStatusIndicator> = listOf(
        BallCarriedStatusIndicator,
        BallExitStatusIndicator,
        BallOnGroundStatusIndicator,
        BlockStatusIndicator,
        DirectionArrowStatusIndicator,
        MoveUsedStatusIndicator,
        TeamFeatureStatusIndicator,
        TeamRerollStatusIndicator,
        TeamSetupsAvailableStatusIndicator
    )
    val actionWheelControllers = setOf(
        AccuracyBB2020WheelController,
        AccuracyBB2025PassWheelController,
        AccuracyBB2025ThrowTeamMateWheelController,
        BounceRollWheelController,
        BoneHeadWheelController,
        BreatheFireWheelController,
        CatchWheelController,
        DauntlessWheelController,
        DeviateRollWheelController,
        DodgeWheelController,
        FoulAppearanceWheelController,
        InterceptionWheelController,
        JumpWheelController,
        JumpUpWheelController,
        LandingWheelController,
        LeapWheelController,
        PickupWheelController,
        PogoWheelController,
        ProjectileVomitWheelController,
        ReallyStupidWheelController,
        UnchannelledFuryWheelController,
        RushWheelController,
        SecureTheBallWheelController,
        ShadowingWheelController,
        SteadyFootingWheelController,
        TakeRootWheelController,
        SelectPlayerActionWheelController,
        SelectBlockTypeWheelController,
        ScatterRollWheelController,

        StandardBlockRollWheelController,
        StandardBlockChooseResultOrRerollWheelController,

        FollowUpWheelController,
        UseBigHandWheelController,
        UseBlockWheelController,
        UseDirtyPlayerWheelController,
        UseDivingCatchWheelController,
        UseDodgeWheelController,
        UseEyeGougeWheelController,
        UseFendWheelController,
        UseGrabWheelController,
        UseHitAndRunWheelController,
        UseKickWheelController,
        UseLeapWheelController,
        UseLoneFoulerWheelController,
        UseMightyBlowController,
        UseSafePairOfHandsWheelController,
        UseSafePassWheelController,
        UseSidestepWheelController,
        UseSneakyGitWheelController,
        UseSprintWheelController,
        UseStandFirmWheelController,
        UseSteadyFootingWheelController,
        UseStripBallWheelController,
        UseStrongArmWheelController,
        UseSureHandsWheelController,
        UseTackleWheelController,
        UseTauntWheelController,
        UseThickSkullWheelController,
        UseVeryLongLegsWheelController,
        UseWrestleWheelController,

        UseApothecaryWheelController,
        ArgueTheCallWheelController
    )

    private val animationScope = CoroutineScope(CoroutineName("AnimationScope") + singleThreadDispatcher("AnimationScope"))
    val gameScope = CoroutineScope(
        Job()
            + CoroutineName("GameLoopScope")
            // + singleThreadDispatcher("GameLoopScope")
            // TODO We cannot share mutableStateOf properties across threads. Moving the entire Game Loop to the Main Thread
            //  fixes it for now. But performance might be a problem. We need to find a performant way to run the game loop
            //  in the background and then offload it all to the Main Thread for rendering
            + Dispatchers.Main
    )

    // Storing a reference to a UiGameSnap is generally a bad idea as it becomes invalid when the game loop
    // rolls over, but we only use the replay during setting up the UI. After that, we should have all consumers
    // set up correctly and the `replay` is not used.
    val uiStateFlow: Flow<UiGameSnapshot>
        field = MutableSharedFlow<UiGameSnapshot>(replay = 1, onBufferOverflow = BufferOverflow.SUSPEND)

    // Will trigger an event on this flow, every the Game Controller has handled a Dev Event.
    // It would be nice if this was just part of the normal UI flow, but for now, this should be good enough.
    val devActionHandled: Flow<Unit>
        field = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.SUSPEND)

    // While the Action Wheel is part of the UiState, its lifecycle is slightly different, so it  has
    val uiActionWheelFlow: Flow<List<ActionWheelUiState>>
        field = MutableSharedFlow<List<ActionWheelUiState>>(extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.SUSPEND)
    val uiContextWheelFlow: Flow<List<ActionWheelUiState>>
        field = MutableSharedFlow<List<ActionWheelUiState>>(extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.SUSPEND)
    val gameStatusMessageFactory = GameStatusMessageFactory(menuViewModel, state)

    // `replay` is only used to allow the UI to register itself after the game controller has started
    val animationFlow: Flow<JervisAnimation?>
        field = MutableSharedFlow<JervisAnimation?>(replay = 1, onBufferOverflow = BufferOverflow.SUSPEND)

    // Channel used by the UI to indicate when the animation is done
    val animationDone = Channel<Boolean>(capacity = Channel.RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)

    init {
        actionProvider.init(this)
    }

    // Report an invalid action to the user, it should not crash the app
    private fun reportInvalidAction(ex: InvalidActionException) {
        menuViewModel.showReportIssueDialog(
            title = "Invalid action created",
            body = """
                The UI created an action that was rejected by the rules engine.
                State: ${state.stack.stateToPrettyString()}
                ${ex.message}
            """.trimIndent(),
            error = ex,
            gameState = gameController
        )
        LOG.e { "Invalid action selected: ${ex.message}" }
        menuViewModel.lastActionException = ex
    }

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
        val fpsCounter = FrameRateAverager()

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
            var lastUiState = UiGameSnapshot(
                actionOwner = null,
                game = controller.state,
                squares = persistentMapOf(),
                players = persistentMapOf(),
                freeBalls = emptyMap(),
                gameStatusText = null,
                status = UiGameStatusUpdate.INITIAL,
                unknownActions = persistentListOf(),
                homeDogoutOnClickAction = null,
                awayDogoutOnClickAction = null,
                dialogInput = null,
                movesUsed = persistentListOf(),
                weather = Weather.PERFECT_CONDITIONS,
                homeTeamInfo = UiTeamInfoUpdate.INITIAL,
                awayTeamInfo = UiTeamInfoUpdate.INITIAL,
                pathFinder = null,
            )

            while (!controller.stack.isEmpty()) {

                // Read new model state
                val state = controller.state
                val delta = controller.getDelta()
                val actions = controller.getAvailableActions()
                val (previousNode, currentNode) = controller.previousNode() to controller.currentNode()
                val acc = UiSnapshotAccumulator(
                    uiStateFlow,
                    uiActionWheelFlow,
                    uiContextWheelFlow,
                    lastUiState, this@UiGameController)

                runPreUpdateAnimations(acc, previousNode, currentNode)

                // Log entries from last action should be added after the animation,
                // so we don't accidentally reveal the result too soon.

                // TODO Run Sound Decorators

                // Update UI State based on latest model state
                actionProvider.prepareForNextAction(controller, actions)
                addBaseGameStateChanges(state, actions, delta, acc)
                applyUiIndicators(actions, controller, acc)
                acc.emitAllUpdates()

                // Detect animations and run them after updating the UI, but before making it ready
                // for creating the user actions
                runPostUpdateStateAnimations(state, acc)

                // TODO Just changing the existing uiState might not trigger recomposition correctly
                //  We need an efficient way to copy the old one.
                actionProvider.decorateAvailableActions(actions, acc)
                acc.let {
                    menuViewModel.updateUiState(it.build())
                    it.emitAllUpdates()
                }

                // Wait for the system to produce the next action, this can either be
                // automatically generated or come from the UI. Here we do not care where
                // it comes from.
                val userAction = run {
                    // Until we can figure out how to better treat Dev Commands in the UI, we will
                    // just execute them immediately without trying to update the UI. At least
                    // this should fix the immediate problem of these actions not showing up
                    // in the save file. The downside is that we risk the UI being slightly out
                    // of sync until the next "real" action
                    tailrec suspend fun getNextNonDevAction(): GameAction {
                        val action = actionProvider.getAction()
                        return if (action is DevModeGameAction) {
                            try {
                                gameController.handleAction(action)
                                devActionHandled.emit(Unit)
                            } catch (ex: InvalidActionException) {
                                reportInvalidAction(ex)
                            }
                            getNextNonDevAction()
                        } else {
                            action
                        }
                    }
                    getNextNonDevAction()
                }

                // After an action was selected, run all decorators that modify
                // the UI while the action is being processed.
                actionProvider.decorateSelectedAction(userAction, acc)
                acc.emitAllUpdates()

                // Then run any animations triggered by the action (but before the state is updated)
                runPostActionSelectedAnimations(controller, userAction, acc)

                // Last, send action to the Rules Engine for processing.
                // This will start the next iteration of the game loop.
                // TODO Add error handling here. What to do for invalid actions?
                lastUiState = acc.build()
                try {
                    val actionWheelLocation = actionWheelControllers.firstOrNull { it.nodes.contains(currentNode) }?.getActionWheelCenter(state)
                    gameController.handleAction(userAction)
                    actionProvider.actionHandled(actions.team, userAction)
                    // Now that we know the next node, we can also determine if the Action Wheel
                    // is visible next step, if it isn't, we can hide it immediately.

                    // If Undo'ing actions, this might happen through short-cuts and not the UI.
                    // If this happens while a context menu is open, its state will be left hanging.
                    // In particular `LocalFieldDataWrapper.isContentMenuVisible`. For that reason, we always
                    // reset that state here when the action is Undo or Revert. It also means we do not have
                    // to deal with "back"-animations.
                    val shouldHideActionWheel = checkHideActionWheelImmediately(gameController, actionWheelLocation)
                    if (shouldHideActionWheel) {
                        val isRevertingState = (userAction is Undo || userAction is Revert)
                        acc.addActionWheelEvent(HideActionWheel(hideImmediately = isRevertingState))
                        acc.addContextWheelEvent(HideActionWheel(hideImmediately = isRevertingState))
                        acc.emitActionWheelState()
                    }
                } catch (ex: InvalidActionException) {
                    reportInvalidAction(ex)
                }
            }
        }.invokeOnCompletion {
            if (it != null && it !is CancellationException) {
                throw it
            }
        }
    }

    fun checkHideActionWheelImmediately(gameController: GameEngineController, lastWheelLocation: FieldCoordinate?): Boolean {
        // If both current and previous node had a visible wheel in the same location, we can keep it around
        // Otherwise it should be hidden
        val currentNode = gameController.currentNode()
        val nextActionWheelPosition = actionWheelControllers.firstOrNull { it.nodes.contains(currentNode) }?.getActionWheelCenter(gameController.state)
        return lastWheelLocation != null && lastWheelLocation != nextActionWheelPosition
    }

    private fun applyUiIndicators(actionRequest: ActionRequest, controller: GameEngineController, acc: UiSnapshotAccumulator) {
        val state = controller.state
        val currentNode = controller.currentNode() as ActionNode
        fieldStatusIndicators.forEach { indicator ->
            indicator.decorate(currentNode, state, actionRequest, acc)
        }

        // Set the game status for the current step. For Hotseat games it will always show the message
        // for the active coach. For P2P games, each client will see the message relevant for the respective coach.
        gameStatusMessageFactory.applyMessage(actionProvider, acc)
    }

    // Run animations before we update the UI to represent the state we moved to after applying the last GameAction
    private suspend fun runPreUpdateAnimations(acc: UiSnapshotAccumulator, previousNode: Node?, currentNode: Node?) {
        val currentWheelHandler = actionWheelControllers.firstOrNull { controller ->
            controller.nodes.contains(currentNode)
        }
        if (currentWheelHandler != null) {
            if (currentWheelHandler.onApplyCurrentState(
                    acc,
                    gameController.lastAction,
                    previousNode,
                    currentNode!!
                )
            ) {
                acc.emitActionWheelState()
                animationDone.receive()
            }
        }

        if (!gameController.lastActionWasUndo()) {
            val animation = AnimationFactory.getPreUpdateAnimation(state)
            if (animation != null) {
                animationFlow.emit(animation)
                animationDone.receive()
            }
        }
    }

    private suspend fun runPostUpdateStateAnimations(state: Game, acc: UiSnapshotAccumulator) {
        if (!gameController.lastActionWasUndo()) {
            val animation = AnimationFactory.getFrameAnimation(state, rules)
            if (animation != null) {
                acc.addActionWheelEvent(HideActionWheel(hideImmediately = true))
                acc.emitActionWheelState()
                animationFlow.emit(animation)
                animationDone.receive()
            }
        }
    }

    private suspend fun runPostActionSelectedAnimations(
        engineController: GameEngineController,
        action: GameAction,
        uiState: UiSnapshotAccumulator
    ) {
        if (action != Undo) {
            // Run any animations on Wheel Controllers first, before triggering more custom animations.
            // This is because we want to "finish" rolling dice, before showing the actual result of those dice rolls
            val currentWheelHandler = actionWheelControllers.firstOrNull { controller ->
                controller.nodes.contains(engineController.currentNode())
            }
            if (currentWheelHandler?.onPostActionAnimation(
                    uiState,
                    action
                ) == true) {
                uiState.emitActionWheelState()
                animationDone.receive()
            }
            val animation = AnimationFactory.getPostActionAnimation(state, action)
            if (animation != null) {
                animationFlow.emit(animation)
                animationDone.receive()
            }
        }
    }

    /**
     * Method responsible for updating the UI state based on recent changes in the [Game] model.
     * This includes
     */
    private fun addBaseGameStateChanges(state: Game, actions: ActionRequest, delta: GameDelta, acc: UiSnapshotAccumulator) {

        // Update the persistent UI decorations before starting
        updatePersistentUiDecorations(state, delta, uiDecorations, acc)

        // Re-render the entire field. This feels a bit like overkill, but making it more granular
        // is going to be challenging, and it doesn't look like there is a performance problem doing it.
        (0 until rules.fieldWidth).forEach { x ->
            (0 until rules.fieldHeight).forEach { y ->
                val coordinate = FieldCoordinate(x, y)
                val square= renderSquare(coordinate, state)
                acc.addOrUpdateSquare(coordinate, square)
            }
        }

        // This will reset the player state and the data class should ensure equality is
        // checked correctly using the auto-generated `equals()`
        state.homeTeam.forEach { player ->
            acc.addOrUpdatePlayer(player.id, UiFieldPlayer(player))
        }
        state.awayTeam.forEach { player ->
            acc.addOrUpdatePlayer(player.id, UiFieldPlayer(player))
        }
    }

    private fun updatePersistentUiDecorations(state: Game, delta: GameDelta, uiIndicators: UiPersistentGameIndicators, acc: UiSnapshotAccumulator) {
        if (delta.reversed) {
            uiIndicators.undo(delta.id)
            acc.setMovesUsed(uiIndicators.movesUsed)
            return
        }

        // Clear move markers when an action ends
        if (delta.containsCommand { it is ExitProcedure && it.procedure == ActivatePlayer }) {
            uiIndicators.resetMovesUsed()
            return
        }

        // Clear move markers when starting a drive. This also handles after a touchdown
        if (state.currentProcedureState()?.procedure == StartOfDriveSequence) {
            uiIndicators.resetMovesUsed()
            return
        }

        // Track standing up so we can adjust "Move used" correctly.
        if (delta.containsAction(MoveTypeSelected(MoveType.STAND_UP))) {
            val activePlayer = state.activePlayer!!
            if (activePlayer.move >= rules.moveRequiredForStandingUp && !activePlayer.hasSkill(SkillType.JUMP_UP)) {
                uiIndicators.addMoveUsedToStandUp(rules.moveRequiredForStandingUp)
            } else {
                uiIndicators.addMoveUsedToStandUp(0)
            }
        }

        // Add decoration when moving player
        // TODO Add support JUMP/LEAP
        val normalMoveStep = delta.steps.lastOrNull()?.let {
            it.procedure == StandardMoveStep && it.action is FieldSquareSelected
        } ?: false
        if (normalMoveStep) {
            val start = delta.allCommands()
                .filterIsInstance<SetPlayerLocation>()
                .first {
                    // When a touchdown is triggered, we will see both the player moving into the end zone and
                    //  all players moving into the Dogout. We only care about the first.
                    it.location != DogOut
                }.originalPlayerLocation
            uiIndicators.addMoveUsed(start)
            uiIndicators.registerUndo(
                deltaId = delta.id,
                action = { uiIndicators.removeLastMoveUsed() }
            )
        }
        acc.setMovesUsed(uiIndicators.movesUsed)
    }

    private fun renderSquare(
        coordinate: FieldCoordinate,
        game: Game,
    ): UiFieldSquare {
        val square = game.field[coordinate]
        return UiFieldSquare(
            coordinates = coordinate,
            player = square.player?.id
        )
    }

    fun userSelectedAction(action: GameAction) {
        actionProvider.userActionSelected(action)
    }

    fun notifyAnimationDone() {
        animationScope.launch {
            animationDone.send(true)
            animationFlow.emit(null)
        }.invokeOnCompletion {
            if (it != null && it !is CancellationException) {
                throw it
            }
        }
    }
}
