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
import io.ktor.client.utils.EmptyContent.status
import kotlinx.collections.immutable.persistentListOf

/**
 * Class responsible for collecting all changes to create a [UiGameSnapshot].
 * This is primarily used by [FieldActionDecorator] and [FieldStatusIndicator]
 * subclasses.
 */
class UiSnapshotAccumulator(
    private val previousSnapshot: UiGameSnapshot,
    val uiController: UiGameController
) {
    private val playersBuilder = previousSnapshot.players.builder()
    private val squaresBuilder = previousSnapshot.squares.builder()
    private var statusBuilder: UiGameStatusUpdate = UiGameStatusUpdate(uiController.state)
    private val unknownActionsBuilder = persistentListOf<GameAction>().builder()
    private val weather = uiController.state.weather

    private val movesUsedBuilder = previousSnapshot.movesUsed.builder()

    val movesUsed: List<MoveUsed> = movesUsedBuilder
    val squares: Map<FieldCoordinate, UiFieldSquare>
        get() = squaresBuilder
    val stack = uiController.state.stack
    val game = uiController.state
    var awayDogoutOnClickAction: (() -> Unit)? = null
    var homeDogoutOnClickAction: (() -> Unit)? = null
    var dialogInput: UserInputDialog? = null
    var homeTeamInfo = UiTeamInfoUpdate(game.homeTeam)
    var awayTeamInfo = UiTeamInfoUpdate(game.awayTeam)

    // If set, it means we are in the middle of a move action that allows the player
    // to move multiple squares.
    var pathFinder: PathFinder.AllPathsResult? = null

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
        if (square != squaresBuilder[coordinate]) {
            squaresBuilder[coordinate] = square
        }
    }

    fun updateSquare(coordinate: FieldCoordinate, func: (UiFieldSquare) -> UiFieldSquare) {
        val currentSquare = squaresBuilder[coordinate] ?: error("Could not find square for coordinate: $coordinate")
        val newSquare = func(currentSquare)
        // We probably do not need to compare here, since using this method always result in updates (I hope)
        if (currentSquare != newSquare) {
            squaresBuilder[coordinate] = newSquare
        }
    }

    fun updatePlayer(id: PlayerId, func: (UiFieldPlayer) -> UiFieldPlayer) {
        val currentPlayer = playersBuilder[id] ?: error("Could not find player for id: $id")
        val newPlayer = func(currentPlayer)
        // We probably do not need to compare here, since using this method always result in updates (I hope)
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


    fun build(): UiGameSnapshot {
        val freeBalls  = squares.filter { it.value.isBallOnGround }
        return UiGameSnapshot(
            actionOwner = uiController.gameController.getAvailableActions().team,
            game = game,
            squares = squaresBuilder.build(),
            players = playersBuilder.build(),
            freeBalls = freeBalls,
            status = statusBuilder,
            unknownActions = unknownActionsBuilder.build(),
            homeDogoutOnClickAction = homeDogoutOnClickAction,
            awayDogoutOnClickAction = awayDogoutOnClickAction,
            dialogInput = dialogInput,
            movesUsed = movesUsedBuilder.build(),
            weather = weather,
            homeTeamInfo = homeTeamInfo,
            awayTeamInfo = awayTeamInfo,
            pathFinder = pathFinder,
        )
    }

}
