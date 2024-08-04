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
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.ui.ContextMenuOption
import dk.ilios.jervis.ui.model.UiFieldSquare
import dk.ilios.jervis.ui.model.UiPlayer
import dk.ilios.jervis.utils.safeTryEmit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

enum class FieldDetails(val resource: String, val description: String) {
    NICE("icons/cached/pitches/default/nice.png", "Nice Weather"),
}

/**
 * This class collects all the information needed to render the field. This includes all information needed for
 * each single square on the field.
 */
class FieldViewModel(
    controller: GameController,
    private val uiActionFactory: UiActionFactory,
    private val state: Field,
    private val hoverPlayerChannel: MutableSharedFlow<Player?>,
) {
    val rules = controller.rules
    val game = controller.state
    val aspectRatio: Float = 782f / 452f
    val width = rules.fieldWidth.toInt()
    val height = rules.fieldHeight.toInt()

    private val field = MutableStateFlow(FieldDetails.NICE)
    private val highlights = SnapshotStateList<FieldCoordinate>()
    private val _highlights = MutableStateFlow<FieldCoordinate?>(null)

    fun field(): StateFlow<FieldDetails> = field

    data class PathInfo(
        val path: List<FieldCoordinate>,
        val pathSteps: Map<FieldCoordinate, Int>,
        val target: FieldCoordinate,
        val action: () -> Unit,
    )

    fun observeOverlays(): Flow<PathInfo?> {
        var ignoreUserInput = false
        return combine(_highlights, uiActionFactory.fieldActions) { square, ac ->
            when (ac) {
                is IgnoreUserInput -> ignoreUserInput = true
                is ResumeUserInput -> ignoreUserInput = false
                else -> { // Do nothing
                }
            }
            if (!ignoreUserInput && ac is CompositeUserInput) {
                ac.inputs.firstOrNull { it is SelectMoveActionFieldLocationInput }?.let {
                    val activePlayer: Player? = game.activePlayer
                    if (activePlayer != null && square != null && activePlayer.location.coordinate != square) {
                        val path: List<FieldCoordinate> =
                            rules.pathFinder.calculateShortestPath(
                                game,
                                activePlayer.location.coordinate,
                                square,
                                activePlayer.moveLeft,
                            ).path
                        val pathSteps =
                            path.mapIndexed { index, fieldCoordinate ->
                                fieldCoordinate to (index + 1)
                            }.toMap()
                        val action = {
                            // Only allow a path up to hitting a ball
                            val selectedSquares =
                                path.takeWhile { state[it].ball == null }.map {
                                    FieldSquareSelected(
                                        it,
                                    )
                                }
                            if (selectedSquares.size == 1) {
                                uiActionFactory.userSelectedAction(selectedSquares.first())
                            } else {
                                uiActionFactory.userSelectedMultipleActions(selectedSquares)
                            }
                        }
                        PathInfo(path, pathSteps, path.lastOrNull() ?: activePlayer.location as FieldCoordinate, action)
                    } else {
                        null
                    }
                }
            } else {
                null
            }
        }
    }

    /**
     * Expose a flow that determines everything needed to render a single square on the field.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeSquare(
        x: Int,
        y: Int,
    ): Flow<UiFieldSquare> {
        var ignoreUserInput = false
        val flow: Flow<UiFieldSquare> =
            state[x, y].squareFlow.flatMapLatest { fieldSquare ->
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

                        when (userInput) {
                            is IgnoreUserInput -> ignoreUserInput = true
                            is ResumeUserInput -> ignoreUserInput = false
                            else -> { // do Nothing
                            }
                        }

                        if (!ignoreUserInput) {
                            val inputs =
                                when (userInput) {
                                    is CompositeUserInput -> {
                                        userInput.inputs
                                    }

                                    else -> {
                                        listOf(userInput)
                                    }
                                }

                            inputs.forEach { input ->
                                when (input) {
                                    is DeselectPlayerInput -> {
                                        // Since `deselect` only applies to the active player, check if the player in the square is active.
                                        if (square.player?.isActive == true) {
                                            squareAction = { uiActionFactory.userSelectedAction(input.actions.first()) }
                                        }
                                    }

                                    is SelectFieldLocationInput -> {
                                        // Allow square to be selected if an action is available for this square.
                                        squareAction =
                                            input.fieldAction[FieldCoordinate(x, y)]?.let {
                                                    action: FieldSquareSelected ->
                                                { uiActionFactory.userSelectedAction(action) }
                                            }
                                    }

                                    is SelectPlayerInput -> {
                                        // If the player in this square is among the selectable players, enable the option
                                        squareAction =
                                            square.player?.let {
                                                input.actions.firstOrNull { (it as PlayerSelected).player == square.player }
                                                    ?.let { playerAction: GameAction ->
                                                        { uiActionFactory.userSelectedAction(playerAction) }
                                                    }
                                            }
                                    }

                                    is SelectPlayerActionInput -> {
                                        if (square.x == input.activePlayerLocation.x && square.y == input.activePlayerLocation.y) {
                                            contextAction.addAll(
                                                input.actions.map {
                                                    ContextMenuOption(
                                                        it.action.name,
                                                        { this@FieldViewModel.uiActionFactory.userSelectedAction(it) },
                                                    )
                                                },
                                            )
                                            showContextMenu = input.actions.isNotEmpty()
                                        }
                                    }

                                    is SelectMoveActionFieldLocationInput -> {
                                        // Allow square to be selected if an action is available for this square.
                                        squareAction =
                                            input.fieldAction[FieldCoordinate(x, y)]?.let {
                                                    action: FieldSquareSelected ->
                                                { uiActionFactory.userSelectedAction(action) }
                                            }
                                    }

                                    is EndActionInput -> {
                                        if (square.player?.isActive == true) {
                                            contextAction.addAll(
                                                input.actions.map {
                                                    ContextMenuOption(
                                                        "End action",
                                                        { this@FieldViewModel.uiActionFactory.userSelectedAction(it) },
                                                    )
                                                },
                                            )
                                        }
                                    }

                                    else -> null // No action possible for this field
                                }
                            }
                        }

                        val uiPlayer = player?.let { UiPlayer(it, squareAction) }
                        val uiSquare =
                            UiFieldSquare(
                                square,
                                ball?.state?.let {
                                    it != BallState.CARRIED && it != BallState.OUT_OF_BOUNDS
                                } ?: false,
                                player?.hasBall() == true,
                                uiPlayer,
                                squareAction, // Only allow a Square Action if no player is on the field
                                contextAction,
                                showContextMenu,
                            )
                        uiSquare
                    },
                )
            }
        return flow
    }

    fun highlights(): StateFlow<FieldCoordinate?> = _highlights

    fun hoverOver(square: FieldCoordinate) {
        game.field[square].player.let { player: Player? ->
            hoverPlayerChannel.safeTryEmit(player)
        }
        _highlights.value = square
    }

    // Observe the entire field as one
    fun observeField(): Flow<Map<FieldCoordinate, UiFieldSquare>> {
        var ignoreUserInput = false
        return combine(
            game.gameFlow,
            uiActionFactory.fieldActions,
        ) { game: Game, userInput: UserInput ->

            val result = mutableMapOf<FieldCoordinate, UiFieldSquare>()
            result[FieldCoordinate.UNKNOWN] = UiFieldSquare(FieldSquare(-1, -1))

            // Determine which action, if any, to take, if this field can be selected

            when (userInput) {
                is IgnoreUserInput -> ignoreUserInput = true
                is ResumeUserInput -> ignoreUserInput = false
                else -> { /* do Nothing */ }
            }

            (0 until rules.fieldWidth.toInt()).forEach { x ->
                (0 until rules.fieldHeight.toInt()).forEach { y ->
                    val square = game.field[x, y]
                    var squareAction: (() -> Unit)? = null
                    val contextAction: MutableList<ContextMenuOption> = mutableListOf()
                    var showContextMenu = false
                    if (!ignoreUserInput) {
                        val inputs =
                            when (userInput) {
                                is CompositeUserInput -> {
                                    userInput.inputs
                                }
                                else -> {
                                    listOf(userInput)
                                }
                            }
                        inputs.forEach { input ->
                            when (input) {
                                is DeselectPlayerInput -> {
                                    // Since `deselect` only applies to the active player, check if the player in the square is active.
                                    if (square.player?.isActive == true) {
                                        squareAction = { uiActionFactory.userSelectedAction(input.actions.first()) }
                                    }
                                }

                                is SelectFieldLocationInput -> {
                                    // Allow square to be selected if an action is available for this square.
                                    squareAction =
                                        input.fieldAction[
                                            FieldCoordinate(
                                                x,
                                                y,
                                            ),
                                        ]?.let { action: FieldSquareSelected ->
                                            { uiActionFactory.userSelectedAction(action) }
                                        }
                                }

                                is SelectPlayerInput -> {
                                    // If the player in this square is among the selectable players, enable the option
                                    squareAction =
                                        square.player?.let {
                                            input.actions.firstOrNull { (it as PlayerSelected).player == square.player }
                                                ?.let { playerAction: GameAction ->
                                                    { uiActionFactory.userSelectedAction(playerAction) }
                                                }
                                        }
                                }

                                is SelectPlayerActionInput -> {
                                    if (square.x == input.activePlayerLocation.x && square.y == input.activePlayerLocation.y) {
                                        contextAction.addAll(
                                            input.actions.map {
                                                ContextMenuOption(
                                                    it.action.name,
                                                    { this@FieldViewModel.uiActionFactory.userSelectedAction(it) },
                                                )
                                            },
                                        )
                                        showContextMenu = input.actions.isNotEmpty()
                                    }
                                }

                                is SelectMoveActionFieldLocationInput -> {
                                    // Allow square to be selected if an action is available for this square.
                                    squareAction =
                                        input.fieldAction[
                                            FieldCoordinate(
                                                x,
                                                y,
                                            ),
                                        ]?.let { action: FieldSquareSelected ->
                                            { uiActionFactory.userSelectedAction(action) }
                                        }
                                }

                                is EndActionInput -> {
                                    if (square.player?.isActive == true) {
                                        contextAction.addAll(
                                            input.actions.map {
                                                ContextMenuOption(
                                                    "End action",
                                                    { this@FieldViewModel.uiActionFactory.userSelectedAction(it) },
                                                )
                                            },
                                        )
                                    }
                                }

                                else -> null // No action possible for this field
                            }
                        }
                    }
                    val uiPlayer = square.player?.let { UiPlayer(it, squareAction) }
                    val uiSquare =
                        UiFieldSquare(
                            square,
                            square.ball?.state?.let {
                                it != BallState.CARRIED && it != BallState.OUT_OF_BOUNDS
                            } ?: false,
                            square.player?.hasBall() == true,
                            uiPlayer,
                            squareAction, // Only allow a Square Action if no player is on the field
                            contextAction,
                            showContextMenu,
                        )
                    result[FieldCoordinate(x, y)] = uiSquare
                }
            }
            result
        }
    }
}
