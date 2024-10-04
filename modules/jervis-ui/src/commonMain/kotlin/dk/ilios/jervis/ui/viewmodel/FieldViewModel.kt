package dk.ilios.jervis.ui.viewmodel

import androidx.compose.runtime.snapshots.SnapshotStateList
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.FieldSquare
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.rules.PlayerSpecialActionType
import dk.ilios.jervis.rules.PlayerStandardActionType
import dk.ilios.jervis.ui.ContextMenuOption
import dk.ilios.jervis.ui.model.UiFieldSquare
import dk.ilios.jervis.ui.model.UiPlayer
import dk.ilios.jervis.utils.safeTryEmit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

enum class FieldDetails(val resource: String, val description: String) {
    NICE("icons/cached/pitches/default/nice.png", "Nice Weather"),
}

/**
 * This class collects all the information needed to render the field. This includes all information needed for
 * each single square on the field.
 */
class FieldViewModel(
    val controller: GameController,
    private val uiActionFactory: UiActionFactory,
    private val state: Field,
    private val hoverPlayerChannel: MutableSharedFlow<Player?>,
) {
    val rules = controller.rules
    val game = controller.state
    val aspectRatio: Float = 782f / 452f
    val width = rules.fieldWidth
    val height = rules.fieldHeight

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

        // Calculate any path info data we want to display
        return combine(_highlights, uiActionFactory.fieldActions) { square, userInput: UserInput ->
            when (userInput) {
                is IgnoreUserInput -> ignoreUserInput = true
                is ResumeUserInput -> ignoreUserInput = false
                else -> { // Do nothing
                }
            }

            if (!ignoreUserInput && userInput is CompositeUserInput) {
                userInput.inputs.firstOrNull { it is SelectMoveActionFieldLocationInput }?.let {
                    val activePlayer: Player? = game.activePlayer
                    if (
                        activePlayer != null &&
                        square != null &&
                        activePlayer.coordinates != square &&
                        activePlayer.movesLeft > 0 &&
                        rules.calculateMarks(game, activePlayer.team, activePlayer.coordinates) <= 0
                    ) {
                        val path: List<FieldCoordinate> =
                            rules.pathFinder.calculateShortestPath(
                                game,
                                activePlayer.coordinates,
                                square,
                                activePlayer.movesLeft ,
                            ).path

                        val pathSteps = path
                            .mapIndexed { index, fieldCoordinate -> fieldCoordinate to (index + 1) }
                            .toMap()

                        val action = {

                            // If a path consists of only 1 step, always execute it, since
                            // we assume the user wants to execute it.
                            // If it consists of multiple steps, only move up to the point
                            // where a die roll is required (i.e., pickup, rush, dodge)
                            val selectedSquares = if (path.size == 1) {
                                path.flatMap {
                                    listOf(MoveTypeSelected(MoveType.STANDARD), FieldSquareSelected(it))
                                }
                            } else {
                                // Only check for balls now
                                path.takeWhile { state[it].balls.isEmpty() }.flatMap {
                                    listOf(MoveTypeSelected(MoveType.STANDARD), FieldSquareSelected(it))
                                }
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

    fun highlights(): StateFlow<FieldCoordinate?> = _highlights

    fun hoverOver(square: FieldCoordinate) {
        game.field[square].player.let { player: Player? ->
            hoverPlayerChannel.safeTryEmit(player)
        }
        _highlights.value = square
    }

    fun exitHover() {
        _highlights.value = null
    }

    // Observe the entire field as one.
    // This means we are re-rendering the entire field for every change, which feels a
    // bit like overkill, but making it more granular is going to be painful, and it doesn't
    // look like there is a performance problem doing it.
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
            (0 until rules.fieldWidth).forEach { x ->
                (0 until rules.fieldHeight).forEach { y ->
                    renderSquare(game, x, y, ignoreUserInput, userInput, result)
                }
            }
            result
        }
    }

    private fun renderSquare(
        game: Game,
        x: Int,
        y: Int,
        ignoreUserInput: Boolean,
        userInput: UserInput,
        result: MutableMap<FieldCoordinate, UiFieldSquare>
    ) {
        val square = game.field[x, y]
        var squareAction: (() -> Unit)? = null
        var onMenuHiddenAction: (() -> Unit)? = null
        var requiresRoll = false
        val contextActions: MutableList<ContextMenuOption> = mutableListOf()
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
                            onMenuHiddenAction = squareAction
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
                            ]?.let { action: FieldSquareAction ->
                                { uiActionFactory.userSelectedAction(action.action) }
                            } ?: squareAction
                    }

                    is SelectPlayerInput -> {
                        // If the player in this square is among the selectable players, enable the option
                        squareAction =
                            square.player?.let {
                                input.actions.firstOrNull { (it as PlayerSelected).getPlayer(game) == square.player }
                                    ?.let { playerAction: GameAction ->
                                        { uiActionFactory.userSelectedAction(playerAction) }
                                    }
                            } ?: squareAction
                    }

                    is SelectPlayerActionInput -> {
                        if (square.x == input.activePlayerLocation.x && square.y == input.activePlayerLocation.y) {
                            contextActions.addAll(
                                input.actions.map {
                                    val name = when (it.action) {
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
                                        name,
                                        { this@FieldViewModel.uiActionFactory.userSelectedAction(it) },
                                    )
                                },
                            )
                        }
                    }

                    is SelectPlayerSubActionInput -> {
                        if (square.x == input.activePlayerLocation.x && square.y == input.activePlayerLocation.y) {
                            contextActions.addAll(
                                input.actions.map {
                                    ContextMenuOption(
                                        it.name,
                                        { this@FieldViewModel.uiActionFactory.userSelectedAction(it.action) },
                                    )
                                },
                            )
                        }
                    }

                    is SelectMoveActionFieldLocationInput -> {
                        // Allow square to be selected if an action is available for this square.
                        squareAction =
                            input.fieldAction[FieldCoordinate(x, y)]?.let { action: FieldSquareAction ->
                                { uiActionFactory.userSelectedAction(action.action) }
                            }
                        requiresRoll = input.fieldAction[FieldCoordinate(x, y)]?.requiresRoll ?: false
                    }

                    is EndActionInput -> {
                        if (square.player?.isActive == true) {
                            contextActions.addAll(
                                input.actions.map {
                                    ContextMenuOption(
                                        "End action",
                                        { this@FieldViewModel.uiActionFactory.userSelectedAction(it) },
                                    )
                                },
                            )
                        }
                    }

                    else -> {
                        // No action possible for this field
                    }
                }
            }
        }
        val uiPlayer = square.player?.let { UiPlayer(it, squareAction) }
        val isBallOnGround: Boolean = square.balls.any {
            (it.state != BallState.CARRIED && it.state != BallState.OUT_OF_BOUNDS) &&
                it.location.x == x &&
                it.location.y == y
        }
        val isBallExiting: Boolean = controller.state.balls.any {
            it.state == BallState.OUT_OF_BOUNDS && it.outOfBoundsAt == FieldCoordinate(x, y)
        }

        // Choosing whether or not to showing the context menu is a bit complicated.
        // But we employ the rule that if one of the actions are a "main" action, it means
        // the player was just selected and we should show the context menu up front.
        // Otherwise, it means that the player is in the middle of their action and we should
        // not show the context menu up front. That should be up to the player
        if (contextActions.isNotEmpty() && contextActions.count { it.title == "End action" } == 0) {
            showContextMenu = true
        }

        val uiSquare =
            UiFieldSquare(
                square,
                isBallOnGround,
                isBallExiting,
                square.player?.hasBall() == true,
                uiPlayer,
                squareAction, // Only allow a Square Action if no player is on the field
                onMenuHiddenAction,
                requiresRoll,
                contextActions,
                showContextMenu,
            )
        result[FieldCoordinate(x, y)] = uiSquare
    }
}
