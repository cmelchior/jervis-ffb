package dk.ilios.jervis.ui.viewmodel

import androidx.compose.runtime.snapshots.SnapshotStateList
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Ball
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.FieldSquare
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.ui.ContextMenuOption
import dk.ilios.jervis.ui.model.UiFieldSquare
import dk.ilios.jervis.ui.model.UiPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

data class Square(val x: Int, val y: Int)

enum class FieldDetails(val resource: String, val description: String) {
    NICE("icons/cached/pitches/default/nice.png", "Nice Weather")
}

data class SquareData(val field: FieldSquare, val player: Player?, val ball: Ball?) {}

/**
 * This class collects all the information needed to render the field. This includes all information needed for
 * each single square on the field.
 */
class FieldViewModel(controller: GameController, private val uiActionFactory: UiActionFactory, private val state: Field) {

    val rules = controller.rules
    val game = controller.state
    val aspectRatio: Float = 782f/452f
    val width = rules.fieldWidth.toInt()
    val height = rules.fieldHeight.toInt()

    private val field = MutableStateFlow(FieldDetails.NICE)
    private val highlights = SnapshotStateList<Square>()
    private val _highlights = MutableStateFlow<Square?>(null)
    fun field(): StateFlow<FieldDetails> = field

    /**
     * Expose a flow that determines everything needed to render a single square on the field.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeSquare(x: Int, y: Int): Flow<UiFieldSquare> {
        val flow: Flow<UiFieldSquare> = state[x, y].squareFlow.flatMapLatest { fieldSquare ->
            combine(
                flow = flowOf(fieldSquare),
                flow2 = fieldSquare.player?.observePlayer ?: flowOf<Player?>(null),
                flow3 = fieldSquare.ball?.observeBall ?: flowOf<Ball?>(null),
                flow4 = uiActionFactory.fieldActions,
                transform = { square: FieldSquare, player: Player?, ball: Ball?, userInput: UserInput ->
                    // Determine which action, if any, to take, if this field can be selected
                    var squareAction: (() -> Unit)? = null
                    var contextAction: MutableList<ContextMenuOption> = mutableListOf()
                    var showContextMenu = false

                    val inputs = when(userInput) {
                        is CompositeUserInput -> {
                            userInput.inputs
                        }
                        else -> {
                            listOf(userInput)
                        }
                    }

                    inputs.forEach { userInput ->
                        when (userInput) {
                            is DeselectPlayerInput -> {
                                // Since `deselect` only applies to the active player, check if the player in the square is active.
                                if (fieldSquare.player?.isActive == true) {
                                    squareAction = { uiActionFactory.userSelectedAction(userInput.actions.first()) }
                                }
                            }
                            is SelectFieldLocationInput -> {
                                // Allow square to be selected if an action is available for this square.
                                squareAction = userInput.fieldAction[FieldCoordinate(x, y)]?.let { action: FieldSquareSelected ->
                                    { uiActionFactory.userSelectedAction(action) }
                                }
                            }
                            is SelectPlayerInput -> {
                                // If the player in this square is among the selectable players, enable the option
                                squareAction = square.player?.let {
                                    userInput.actions.firstOrNull { (it as PlayerSelected).player == square.player }
                                        ?.let { playerAction: GameAction ->
                                            { uiActionFactory.userSelectedAction(playerAction) }
                                        }
                                }
                            }
                            is SelectPlayerActionInput -> {
                                if (square.x == userInput.activePlayerLocation.x && square.y == userInput.activePlayerLocation.y) {
                                    contextAction.addAll(
                                        userInput.actions.map {
                                            ContextMenuOption(it.action.name, { this@FieldViewModel.uiActionFactory.userSelectedAction(it) })
                                        }
                                    )
                                    showContextMenu = userInput.actions.isNotEmpty()
                                }
                            }
                            is EndActionInput -> {
                                if (square.player == game.activePlayer) {
                                    contextAction.addAll(
                                        userInput.actions.map {
                                            ContextMenuOption("End action", { this@FieldViewModel.uiActionFactory.userSelectedAction(it) })
                                        }
                                    )
                                }
                            }
                            else -> null /* No action possible for this field */
                        }
                    }

                    UiFieldSquare(
                        square,
                        ball?.state?.let {
                            it != BallState.CARRIED && it != BallState.OUT_OF_BOUNDS
                        } ?: false,
                        player?.hasBall() == true,
                        player?.let { UiPlayer(it, squareAction) },
                        squareAction, // Only allow a Square Action if no player is on the field
                        contextAction,
                        showContextMenu
                    )
                }
            )
        }
        return flow
    }

    fun highlights(): StateFlow<Square?> = _highlights

    fun hoverOver(square: Square) {
        _highlights.value = square
    }
}