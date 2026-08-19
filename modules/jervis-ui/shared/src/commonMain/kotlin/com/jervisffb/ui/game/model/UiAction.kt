package com.jervisffb.ui.game.model

import androidx.compose.runtime.Stable
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.utils.jervisLogger

/**
 * Wrapper for all UI actions that generate a [com.jervisffb.engine.actions.GameAction].
 * While we should never get into a position where the UI is outdated compared to the
 * game state, sometimes it happens regardless as the rendering is somewhat asynchronous.
 *
 * If this happens, and we uncritically send a game action to the engine, there is a chance
 * it will reject it and crash (best case). To avoid this, we always use this wrapper that
 * asserts the state of the engine before sending the action.
 *
 * If by some chance we still ignore the stale UI callback, it is likely that the game state
 * has changed in a way that makes the action invalid. In this case, the action will be
 * ignored and the user will be notified.
 */
private class CallbackGuard(
    private val controller: GameEngineController?,
    private val actionId: GameActionId?, // The id game actions are created for.
    private val allowMultipleInvocations: Boolean = false
) {
    private var invoked = false

    fun invoke(delegate: () -> Unit) {

        val controller = controller ?: run {
            LOG.w {
                "Ignoring guarded UI callback without a game controller: " +
                    "expected action ActionId[${this.actionId}]."
            }
            return
        }

        // A callback belongs to one UI snapshot and can only produce one engine
        // action. If pressing things very quickly in the UI, sometimes we can
        // trigger multiple callbacks. This boolean prevents these events from
        // reaching the engine.
        //
        // This is not thread-safe and is assumed to only be called from the UI
        // thread.
        if (invoked && !allowMultipleInvocations) {
            LOG.w { "Callback invoked multiple times for action ActionId[${this@CallbackGuard.actionId}]" }
            return
        }

        invoked = true
        val actionId = this@CallbackGuard.actionId
        val expectedActionId = controller.nextActionIndex()
        if (expectedActionId != actionId) {
            LOG.w {
                val currentNode = controller.stack.stateToPrettyString()
                "Ignoring stale UI callback: expected action ActionId[$actionId], " +
                    "but the current action is ActionId[$expectedActionId]. " +
                    "Current node: $currentNode"
            }
            return
        }

        delegate()
    }

    companion object {
        val LOG = jervisLogger()
    }
}

// Guarded callback with 0 arguments
class GuardedCallback0(
    controller: GameEngineController?,
    private val id: GameActionId?,
    private val delegate: (GameActionId) -> Unit,
) : () -> Unit {
    private val guard = CallbackGuard(controller, id)
    val guardedActionId: GameActionId? get() = id

    constructor(
        controller: GameEngineController,
        delegate: (GameActionId) -> Unit,
    ) : this(controller, controller.nextActionIndex(), delegate)

    constructor(
        acc: UiSnapshotAccumulator,
        delegate: (GameActionId) -> Unit,
    ) : this(acc.gameController, acc.gameController.nextActionIndex(), delegate)

    override fun invoke() {
        val actionId = id ?: GameActionId.NONE
        guard.invoke {
            delegate(actionId)
        }
    }
}

// Guarded callback with 1 arguments
class GuardedCallback1<T>(
    controller: GameEngineController?,
    private val id: GameActionId?,
    private val delegate: (GameActionId, T) -> Unit,
) : (T) -> Unit {

    constructor(
        acc: UiSnapshotAccumulator,
        delegate: (GameActionId, T) -> Unit,
    ) : this(acc.gameController, acc.gameController.nextActionIndex(), delegate)

    private val guard = CallbackGuard(controller, id)
    val guardedActionId: GameActionId? get() = id

    override fun invoke(value: T) {
        val actionId = id ?: GameActionId.NONE
        guard.invoke {
            delegate(actionId, value)
        }
    }
}

// Guarded callback with 2 arguments
class GuardedCallback2<A, B>(
    controller: GameEngineController? = null,
    private val id: GameActionId? = null,
    allowMultipleInvocations: Boolean = false,
    private val delegate: (GameActionId, A, B) -> Unit,
) : (A, B) -> Unit {

    constructor(
        acc: UiSnapshotAccumulator,
        allowMultipleInvocations: Boolean = false,
        delegate: (GameActionId, A, B) -> Unit,
    ) : this(acc.gameController, acc.gameController.nextActionIndex(), allowMultipleInvocations, delegate)

    private val guard = CallbackGuard(controller, id, allowMultipleInvocations)
    val guardedActionId: GameActionId? get() = id

    override fun invoke(first: A, second: B) {
        val actionId = id ?: GameActionId.NONE
        guard.invoke {
            delegate(actionId, first, second)
        }
    }
}

val NoOpGuardedAction = GuardedCallback0(null, null) { /* Do nothing */ }
typealias GuardedAction = GuardedCallback0
typealias GuardedBadgeAction = GuardedCallback1<GameScreenModel>
typealias GuardedPlayerAction = GuardedCallback2<GameScreenModel, UiPitchPlayer>

/**
 * This class represents a click handler that carries its own identity, so two
 * handlers doing the same thing compare equal.
 *
 * Some notes about usage:
 *
 * 1. This class allows Compose to more efficiently skip recomposition when the
 *    handler is the same.
 *
 * 2. This class is only relevant if a lambda is created outside a @Composable
 *    function and then passed in. Lambdas created inside a @Composable function
 *    are automatically memoized based on their captured values by the Compose
 *    Compiler Plugin, so do not need this work-around.
 *
 * 3. The key must uniquely represent the callback’s observable behavior. Two
 *    UiAction instances may compare equal only when retaining either callback
 *    would be behaviorally equivalent.
 *
 * 4. Most keys come for free, because the body is usually "dispatch this
 *   `GameAction`" and the engine's actions are data classes:
 *
 *   ```
 *   val selectedAction = UiAction(PitchSquareSelected(coordinate), GuardedAction(acc) { id ->
 *       actionProvider.userActionSelected(id, PitchSquareSelected(coordinate))
 *   })
 *   ```
 *
 *   Handlers that do UI work instead of dispatching a single action need a
 *   composite key naming everything they read. Example
 *   [com.jervisffb.ui.game.state.decorators.SelectPlayersDecorator].
 */
@Stable
class UiAction(
    private val key: Any,
    private val block: GuardedAction,
) : () -> Unit {
    private val identity = key to block.guardedActionId

    override fun invoke() = block.invoke()
    override fun equals(other: Any?): Boolean = (other is UiAction && identity == other.identity)
    override fun hashCode(): Int = identity.hashCode()
    override fun toString(): String = "UiAction($key)"
}

/**
 * [UiAction] for [UiPitchPlayer.selectedAction], which is handed the screen
 * model and the player that was clicked. See [UiAction] for the rule [key] has
 * to satisfy.
 */
@Stable
class UiPlayerAction(
    private val key: Any,
    private val block: GuardedPlayerAction
) : (GameScreenModel, UiPitchPlayer) -> Unit {
    private val identity = key to block.guardedActionId

    override fun invoke(screenModel: GameScreenModel, player: UiPitchPlayer) = block(screenModel, player)
    override fun equals(other: Any?): Boolean = (other is UiPlayerAction && identity == other.identity)
    override fun hashCode(): Int = identity.hashCode()
    override fun toString(): String = "UiPlayerAction($key)"
}
