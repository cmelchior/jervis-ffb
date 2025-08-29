package com.jervisffb.ui.game

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.ui.game.viewmodel.ButtonData
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

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

data class UiTeamInfoUpdate(
    val id: TeamId,
    val coachName: String,
    val teamName: String,
    val turn: Int,
    val score: Int,
    val rerolls: PersistentList<UiReroll>,
    val featureList: PersistentList<UiTeamFeature>,
) {
    constructor(team: Team) : this(
        id = team.id,
        coachName = team.coach.name,
        teamName = team.name,
        turn = team.turnMarker,
        score = if (team.isHomeTeam()) team.game.homeScore else team.game.awayScore,
        rerolls = persistentListOf(),
        featureList = persistentListOf()
    )

    companion object {
        val INITIAL = UiTeamInfoUpdate(
            TeamId(""), "", "", 0, 0, persistentListOf(), persistentListOf()
        )
    }
}

// This class contains high-level game information that is primarily used by the top status bar.
data class UiGameStatusUpdate(
    val half: Int = 0,
    val drive: Int = 0,
    val turnMax: Int = 0,
    val centerBadgeText: String = "",
    val centerBadgeAction: (() -> Unit)? = null,
    val badgeSubButtons: PersistentList<ButtonData> = persistentListOf(),
    val actionButtons: PersistentList<ButtonData> = persistentListOf(),
) {
    constructor(game: Game) : this(
        half = game.halfNo,
        drive = game.driveNo,
        turnMax = if (game.halfNo > game.rules.halfsPrGame) game.rules.turnsInExtraTime else game.rules.turnsPrHalf,
    )

    companion object {
        val INITIAL = UiGameStatusUpdate()
    }
}
