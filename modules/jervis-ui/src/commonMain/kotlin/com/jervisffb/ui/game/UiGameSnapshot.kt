package com.jervisffb.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.fsm.ProcedureStack
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.ui.game.dialogs.UserInputDialog
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.viewmodel.ButtonData


enum class UiTeamFeatureType {
    APOTHECARY,
    BLOODWEISER_KEG,
    UNKNOWN,
}

enum class UiRerollType {
    TEAM,
    LEADER,
    BRILLIANT_COACHING,
    UNKNOWN
}

class UiReroll(
    val name: String,
    val type: UiRerollType,
    val used: Boolean = false
)

class UiTeamFeature(
    val name: String,
    val value: Int,
    val type: UiTeamFeatureType,
    val used: Boolean = false
)

class UiTeamInfoUpdate(
    val id: TeamId,
    var coachName: String,
    var teamName: String,
    var turn: Int,
    var score: Int,
    val rerolls: MutableList<UiReroll>,
    val featureList: MutableList<UiTeamFeature>,
) {
    constructor(team: Team) : this(
        id = team.id,
        coachName = team.coach.name,
        teamName = team.name,
        turn = team.turnMarker,
        score = if (team.isHomeTeam()) team.game.homeScore else team.game.awayScore,
        rerolls = mutableListOf(),
        featureList = mutableListOf()
    )

    companion object {
        val INITIAL = UiTeamInfoUpdate(
            TeamId(""), "", "", 0, 0, mutableListOf(), mutableListOf()
        )
    }
}

// The UiGameStatusUpdate will be updated during an iteration of the game loop, so all fields
// must be mutableState to trigger updates correctly.
class UiGameStatusUpdate(
    half: Int,
    drive: Int,
    turnMax: Int,
    homeTeamInfo: UiTeamInfoUpdate,
    awayTeamInfo: UiTeamInfoUpdate,
    centerBadgeText: String,
    centerBadgeAction: (() -> Unit)?,
    badgeSubButtons: MutableList<ButtonData>,
    actionButtons: MutableList<ButtonData>,
) {

    var half: Int by mutableStateOf(half)
    var drive: Int by mutableStateOf(drive)
    var turnMax: Int by mutableStateOf(turnMax)
    val homeTeamInfo: UiTeamInfoUpdate = homeTeamInfo
    val awayTeamInfo: UiTeamInfoUpdate = awayTeamInfo
    var centerBadgeText: String by mutableStateOf(centerBadgeText)
    var centerBadgeAction: (() -> Unit)? by mutableStateOf(centerBadgeAction)
    val badgeSubButtons: SnapshotStateList<ButtonData> = SnapshotStateList()
    val actionButtons: SnapshotStateList<ButtonData> = SnapshotStateList()

    init {
        badgeSubButtons.addAll(badgeSubButtons)
        actionButtons.addAll(actionButtons)
    }

    constructor(game: Game) : this(
        half = game.halfNo,
        drive = game.driveNo,
        turnMax = if (game.halfNo > game.rules.halfsPrGame) game.rules.turnsInExtraTime else game.rules.turnsPrHalf,
        homeTeamInfo = UiTeamInfoUpdate(game.homeTeam),
        awayTeamInfo = UiTeamInfoUpdate(game.awayTeam),
        centerBadgeText = "",
        centerBadgeAction = null,
        badgeSubButtons = mutableListOf(),
        actionButtons = mutableListOf()
    )

    companion object {
        val INITIAL = UiGameStatusUpdate(
            0, 0, 0,
            UiTeamInfoUpdate.INITIAL,
            UiTeamInfoUpdate.INITIAL,
            "",
            null,
            mutableListOf(),
            mutableListOf()
        )
    }
}

/**
 * Class representing a snapshot of the current UI State as it should be shown for this "frame". This only
 * includes the model rules state, and shouldn't include ephemeral state. Things like hover state
 * should be covered by individual view models.
 *
 * Note, the snapshot is not stable as it references mutable classes. It is only stable
 * for the duration of a single game loop.
 */
class UiGameSnapshot(
    val uiController: UiGameController,
    val game: Game,
    val stack: ProcedureStack,
    var actionsRequest: ActionRequest,
    val uiIndicators: UiGameIndicators,
    val fieldSquares: MutableMap<FieldCoordinate, UiFieldSquare>,
    val gameStatus: UiGameStatusUpdate
) {
    fun clearHoverData() {
        // Clear the hover data, only update squares that actually changed
        fieldSquares.entries.forEach { fieldSquare ->
            val square: UiFieldSquare = fieldSquare.value
            if (square.futureMoveValue != null) {
                square.apply {
                    futureMoveValue = null
                    hoverAction = null
                }
            }
        }
    }

    val homeTeamActions = mutableListOf<ButtonData>()
    val awayTeamActions = mutableListOf<ButtonData>()

    // Attach actions to players found in the dugout
    val dogoutActions: MutableMap<PlayerId, () -> Unit> = mutableMapOf()

    // onClick action for the dugout. `dogoutActions` take precedence over this action
    var homeDogoutOnClickAction: (() -> Unit)? by mutableStateOf(null)
    var awayDogoutOnClickAction: (() -> Unit)? by mutableStateOf(null)

    // If set, it means we are in the middle of a move action that allows the player
    // to move multiple squares.
    var pathFinder: PathFinder.AllPathsResult? = null

    // If set, a dialog should be shown as a first priority
    var dialogInput: UserInputDialog? = null
    val unknownActions: MutableList<GameAction> = mutableListOf()

    init {
        fieldSquares[FieldCoordinate.UNKNOWN] = UiFieldSquare(FieldSquare(-1, -1))
    }
}
