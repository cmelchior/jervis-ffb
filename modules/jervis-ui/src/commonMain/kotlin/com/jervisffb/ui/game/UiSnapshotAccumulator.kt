package com.jervisffb.ui.game

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.ui.game.dialogs.UserInputDialog
import com.jervisffb.ui.game.model.UiFieldPlayer
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.state.decorators.FieldActionDecorator
import com.jervisffb.ui.game.state.indicators.FieldStatusIndicator
import com.jervisffb.ui.game.view.ActionWheelUiState
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.game.view.ContextWheelUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Class responsible for collecting all changes to create a [UiGameSnapshot].
 * This is primarily used by [FieldActionDecorator] and [FieldStatusIndicator]
 * subclasses.
 *
 * This class is mutable, but can send its current state to the UI using either
 * [emitUiState] or [emitActionWheelState].
 */
class UiSnapshotAccumulator(
    private val uiStateFlow: MutableSharedFlow<UiGameSnapshot>,
    private val uiActionWheelFlow: MutableSharedFlow<List<ActionWheelUiState>>,
    private val uiContextWheelFlow: MutableSharedFlow<ContextWheelUiState>,
    previousSnapshot: UiGameSnapshot,
    val uiController: UiGameController
) {
    val gameController = uiController.gameController
    val game = uiController.state

    private val playersBuilder = previousSnapshot.players.builder()
    private var gameStatusText: String? = null
    private var statusBuilder: UiGameStatusUpdate = UiGameStatusUpdate(gameController.getAvailableActions().team?.id, uiController.state)
    private val unknownActionsBuilder = persistentListOf<GameAction>().builder()
    private val weather = uiController.state.weather

    private val movesUsedBuilder = previousSnapshot.movesUsed.builder()

    val movesUsed: List<MoveUsed> = movesUsedBuilder
    val squares: Map<FieldCoordinate, UiFieldSquare>
        field = previousSnapshot.squares.builder()
    val stack = uiController.state.stack
    var awayDogoutOnClickAction: (() -> Unit)? = null
    var homeDogoutOnClickAction: (() -> Unit)? = null
    private val actionWheelEvents = mutableListOf<ActionWheelUiState>()
    private val contextWheelEvents = mutableListOf<ContextWheelUiState>()
    var dialogInput: UserInputDialog? = null
    var homeTeamInfo = UiTeamInfoUpdate(game.homeTeam)
    var awayTeamInfo = UiTeamInfoUpdate(game.awayTeam)

    // If set, it means we are in the middle of a move action that allows the player
    // to move multiple squares.
    var pathFinder: PathFinder.AllPathsResult? = null

    fun addActionWheelEvent(event: ActionWheelUiState) {
        actionWheelEvents.add(event)
        when (event) {
            is ActionWheelUiStateData -> {
                event.lastActionWasUndo = gameController.lastActionWasUndo()
            }
            else -> { /* Do nothing */ }
        }
    }

    fun addContextWheelEvent(event: ContextWheelUiState) {
        contextWheelEvents.add(event)
    }

    fun getAllMoveUsed(): Map<FieldCoordinate, Int> {
        return movesUsed.associate { it.coordinate to it.value }
    }

    fun addUnknownAction(action: GameAction) {
        unknownActionsBuilder.add(action)
    }

    fun updateGameStatus(func: (UiGameStatusUpdate) -> UiGameStatusUpdate) {
        statusBuilder = func(statusBuilder)
    }

    fun addOrUpdateSquare(coordinate: FieldCoordinate, square: UiFieldSquare) {
        if (square != squares[coordinate]) {
            squares[coordinate] = square
        }
    }

    fun updateSquare(coordinate: FieldCoordinate, func: (UiFieldSquare) -> UiFieldSquare) {
        val currentSquare = squares[coordinate] ?: error("Could not find square for coordinate: $coordinate")
        val newSquare = func(currentSquare)
        // We probably do not need to compare here, since using this method always results in updates (I hope)
        if (currentSquare != newSquare) {
            squares[coordinate] = newSquare
        }
    }

    fun updatePlayer(id: PlayerId, func: (UiFieldPlayer) -> UiFieldPlayer) {
        val currentPlayer = playersBuilder[id] ?: error("Could not find player for id: $id")
        val newPlayer = func(currentPlayer)
        // We probably do not need to compare here, since using this method always results in updates (I hope)
        if (currentPlayer != newPlayer) {
            playersBuilder[id] = newPlayer
        }
    }

    fun addOrUpdatePlayer(id: PlayerId, player: UiFieldPlayer) {
        if (player != playersBuilder[id]) {
            playersBuilder[id] = player
        }
    }

    fun updateTeamInfo(team: Team, func: (Team, UiTeamInfoUpdate) -> UiTeamInfoUpdate) {
        when (team.isHomeTeam()) {
            true -> homeTeamInfo = func(team, homeTeamInfo)
            false -> awayTeamInfo = func(team, awayTeamInfo)
        }
    }

    fun setMovesUsed(movesUsed: List<MoveUsed>) {
        movesUsedBuilder.clear()
        movesUsedBuilder.addAll(movesUsed)
    }

    fun setGameStatusText(message: String?) {
        this.gameStatusText = message
    }

    fun build(): UiGameSnapshot {
        val freeBalls  = squares.filter { it.value.isBallOnGround }
        return UiGameSnapshot(
            actionOwner = uiController.gameController.getAvailableActions().team,
            game = game,
            squares = squares.build(),
            players = playersBuilder.build(),
            freeBalls = freeBalls,
            gameStatusText = gameStatusText,
            status = statusBuilder,
            unknownActions = unknownActionsBuilder.build(),
            homeDogoutOnClickAction = homeDogoutOnClickAction,
            awayDogoutOnClickAction = awayDogoutOnClickAction,
            dialogInput = dialogInput,
            // actionWheelVisible = actionWheelVisible,
            movesUsed = movesUsedBuilder.build(),
            weather = weather,
            homeTeamInfo = homeTeamInfo,
            awayTeamInfo = awayTeamInfo,
            pathFinder = pathFinder,
        )
    }

    suspend fun emitUiState() {
        uiStateFlow.emit(build())
    }

    // Send any current accumulated action-wheel events to the UI. The list
    // is cleared after sending it.
    suspend fun emitActionWheelState() {
        if (contextWheelEvents.isNotEmpty()) {
            uiContextWheelFlow.emit(contextWheelEvents.single())
            contextWheelEvents.clear()
        }
        if (actionWheelEvents.isNotEmpty()) {
            uiActionWheelFlow.emit(actionWheelEvents.toList())
            actionWheelEvents.clear()
        }
    }

    suspend fun emitAllUpdates() {
        emitUiState()
        emitActionWheelState()
    }
}
