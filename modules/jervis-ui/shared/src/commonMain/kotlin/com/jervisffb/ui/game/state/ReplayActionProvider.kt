package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.milliseconds

/**
 * Action provider that replays a Jervis game from a list of recorded [GameAction]s
 *
 * Unlike [ReplayActionProvider] (which is FUMBBL-specific and forward-only), this
 * provider drives the game loop in both directions and supports variable playback
 * speed. It always starts at the very beginning of the game, paused, and feeds the
 * recorded actions into the loop one at a time via [getAction]:
 *  - Playing forward returns the next recorded action.
 *  - Playing backward returns [Undo] (the engine reverses the last applied action).
 *  - Jumping to the start/end runs backward/forward with no delay and animations off.
 *
 * The recorded actions are applied sequentially from the start (never pre-loaded), so
 * if a recorded action is rejected by the rules engine it surfaces at the exact point
 * it fails (the game loop shows a report dialog) and playback is paused here.
 */
class ReplayActionProvider(
    private val actions: List<GameAction>,
    gameController: GameEngineController,
    menuViewModel: MenuViewModel,
    gameSettings: GameSettings,
) : UiActionProviderGroup(), ReplayController {

    companion object {
        val LOG = jervisLogger()
        // Base delay between two actions at 1x speed.
        const val BASE_DELAY_MS = 600L
    }

    override val currentProvider = this

    // A ManualActionProvider held purely to reproduce the normal-game UI decorations
    // (selectable-player outlines, pitch-square colors, move paths, action wheels) during
    // replay. Only the decoration hooks are forwarded to it; recorded actions are still
    // driven by this provider's getAction(). Because we never call its prepareForNextAction,
    // it has no automated/queued action and therefore always draws the decorations.
    private val decorationProvider = ManualActionProvider(
        gameController,
        menuViewModel,
        TeamActionMode.ALL_TEAMS,
        gameSettings,
    )

    override val playback: StateFlow<ReplayPlayback>
        field  = MutableStateFlow(ReplayPlayback(ReplayDirection.PAUSED, 1))

    override val position: StateFlow<Int>
        field = MutableStateFlow(0)

    override val totalActions: Int = actions.size

    // `true` while jumping to the start/end: skips inter-action delays and animations.
    private var skippingActions = false

    private lateinit var uiController: UiGameController
    private lateinit var controller: GameEngineController

    // Tracking used to detect when an emitted action was rejected by the rules engine.
    // A successfully applied action always advances the engine's action index; if it did
    // not advance, the last emitted action was invalid at this point in the replay.
    private var lastEmittedAction: GameAction? = null
    private var indexBeforeLastEmit: GameActionId? = null

    override fun init(controller: UiGameController) {
        uiController = controller
        decorationProvider.init(controller)
    }

    override fun startHandler() {
        // Nothing to do. The game loop drives everything through `getAction`.
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        this.controller = controller
    }

    override suspend fun getAction(id: GameActionId): GameAction {
        while (true) {
            // Detect whether the previously emitted action was rejected by the engine.
            val emitted = lastEmittedAction
            val prevIndex = indexBeforeLastEmit
            lastEmittedAction = null
            indexBeforeLastEmit = null
            if (emitted != null && prevIndex != null && controller.currentActionIndex() == prevIndex) {
                // Roll our position back to the last successfully applied action and pause.
                // The game loop already reports the rejected action to the user.
                when (emitted == Undo) {
                    true -> position.value += 1
                    else -> position.value -= 1
                }
                LOG.w { "Replay action rejected at position ${position.value}: $emitted" }
                pauseInternal()
            }

            val pb = playback.value
            when (pb.direction) {
                ReplayDirection.PAUSED -> {
                    // Suspend until playback is resumed in some direction.
                    playback.first { it.direction != ReplayDirection.PAUSED }
                }
                ReplayDirection.FORWARD -> {
                    if (position.value >= actions.size) {
                        pauseInternal()
                        continue
                    }
                    if (!skippingActions) {
                        val adjustedPlaybackSpeed = (BASE_DELAY_MS / pb.speed).milliseconds
                        delay(adjustedPlaybackSpeed)
                    }
                    val action = actions[position.value]
                    position.value += 1
                    indexBeforeLastEmit = controller.currentActionIndex()
                    lastEmittedAction = action
                    return action
                }
                ReplayDirection.BACKWARD -> {
                    if (position.value <= 0) {
                        pauseInternal()
                        continue
                    }
                    if (!skippingActions) {
                        val adjustedPlaybackSpeed = (BASE_DELAY_MS / pb.speed).milliseconds
                        delay(adjustedPlaybackSpeed)
                    }
                    position.value -= 1
                    indexBeforeLastEmit = controller.currentActionIndex()
                    lastEmittedAction = Undo
                    return Undo
                }
            }
        }
    }

    override fun jumpToStart() = changePlayback(ReplayDirection.BACKWARD, Int.MAX_VALUE, jump = true)
    override fun fastBackward() = changePlayback(ReplayDirection.BACKWARD, nextFastSpeed(ReplayDirection.BACKWARD), jump = false)
    override fun backward() = changePlayback(ReplayDirection.BACKWARD, 1, jump = false)
    override fun pause() = pauseInternal()
    override fun forward() = changePlayback(ReplayDirection.FORWARD, 1, jump = false)
    override fun fastForward() = changePlayback(ReplayDirection.FORWARD, nextFastSpeed(ReplayDirection.FORWARD), jump = false)
    override fun jumpToEnd() = changePlayback(ReplayDirection.FORWARD, Int.MAX_VALUE, jump = true)

    private fun changePlayback(direction: ReplayDirection, speed: Int, jump: Boolean) {
        skippingActions = jump
        playback.value = ReplayPlayback(direction, speed)
        updateAnimationScale()
    }

    private fun pauseInternal() {
        skippingActions = false
        playback.value = ReplayPlayback(ReplayDirection.PAUSED, 0)
        updateAnimationScale()
        uiController.drainAnimationSignals()
    }

    //  Cycle the fast speeds 2 -> 3 -> 4 -> 2, starting at 2
    private fun nextFastSpeed(direction: ReplayDirection): Int {
        val pb = playback.value
        return when (pb.direction == direction && pb.speed >= 2) {
            true -> if (pb.speed >= 4) 2 else pb.speed + 1
            false -> 2
        }
    }

    private fun updateAnimationScale() {
        if (::uiController.isInitialized) {
            uiController.animationSpeedFactor = animationFactorFor(playback.value, skippingActions)
            // Freeze the UI while jumping so the rewind/forward happens "behind the scenes"; the loop
            // renders the target state once when the jump ends (skippingActions flips back to false).
            uiController.suppressUiUpdates = skippingActions
        }
    }

    private fun animationFactorFor(pb: ReplayPlayback, jumping: Boolean): Float = when {
        jumping -> 0f // Disable animations when jumping forward/backwards
        pb.speed <= 1 -> 1f // 1x -> full duration
        pb.speed == 2 -> 0.5f // 2x -> half duration
        else -> 0f // Skip animations when going above 2x
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        // When skipping to either the end or the beginning, we need to reset
        // the replay state to avoid UI bugs when restarting the replay.
        // `pauseInternal()` does this.
        val startOfGame = position.value <= 0
        val endOfGame = position.value >= actions.size
        if (skippingActions && (startOfGame || endOfGame)) {
            pauseInternal()
        }
    }
    override fun updateSharedData(sharedData: LocalPitchDataWrapper) {
        decorationProvider.updateSharedData(sharedData)
    }
    // Forward decoration to the wrapped ManualActionProvider so replay shows the same player
    // outlines, pitch-square colors, move paths and action wheels as a live game.
    override fun decorateAvailableActions(actions: ActionRequest, acc: UiSnapshotAccumulator) {
        decorationProvider.decorateAvailableActions(actions, acc)
    }
    override fun decorateSelectedAction(action: GameAction, acc: UiSnapshotAccumulator) {
        decorationProvider.decorateSelectedAction(action, acc)
        acc.actionWasSelectedWithoutUserInput = true
    }
    override fun userActionSelected(id: GameActionId, action: GameAction) { /* Do nothing */ }
    override fun userMultipleActionsSelected(startingId: GameActionId, actions: List<GameAction>, delayEvent: Boolean) { /* Do nothing */ }
    override fun registerQueuedActionGenerator(generator: QueuedActionsGenerator) { /* Do nothing */ }
    override fun hasQueuedActions(): Boolean = false
}
