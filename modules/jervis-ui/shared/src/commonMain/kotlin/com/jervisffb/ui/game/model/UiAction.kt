package com.jervisffb.ui.game.model

import androidx.compose.runtime.Stable
import com.jervisffb.ui.menu.GameScreenModel

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
 *   val selectedAction = UiAction(PitchSquareSelected(coordinate)) {
 *       actionProvider.userActionSelected(PitchSquareSelected(coordinate))
 *   }
 *   ```
 *
 *   Handlers that do UI work instead of dispatching a single action need a
 *   composite key naming everything they read. Example
 *   [com.jervisffb.ui.game.state.decorators.SelectPlayersDecorator].
 */
@Stable
class UiAction(
    private val key: Any,
    private val block: () -> Unit,
) : () -> Unit {
    override fun invoke() = block()
    override fun equals(other: Any?): Boolean = (other is UiAction && key == other.key)
    override fun hashCode(): Int = key.hashCode()
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
    private val block: (GameScreenModel, UiPitchPlayer) -> Unit,
) : (GameScreenModel, UiPitchPlayer) -> Unit {
    override fun invoke(screenModel: GameScreenModel, player: UiPitchPlayer) = block(screenModel, player)
    override fun equals(other: Any?): Boolean = (other is UiPlayerAction && key == other.key)
    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = "UiPlayerAction($key)"
}
