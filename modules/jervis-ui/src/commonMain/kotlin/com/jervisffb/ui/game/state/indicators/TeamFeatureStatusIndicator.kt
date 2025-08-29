package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.UiTeamFeature
import com.jervisffb.ui.game.UiTeamFeatureType
import com.jervisffb.ui.game.UiTeamInfoUpdate

/**
 * Show the list of available team "features". These will be shown under the
 * team name as a list of icons. Example of this is e.g., having an Apothecary.
 *
 * TODO Also add support for Inducements (probably in its own status indicator)
 * TODO Figure out how to show both features and inducements. Should they be
 *  mixed, features first-then inducements, alphabetical?
 */
object TeamFeatureStatusIndicator: FieldStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        acc.updateTeamInfo(state.homeTeam) { team, teamInfo ->
            configureTeamFeatures(team, teamInfo)
        }
        acc.updateTeamInfo(state.awayTeam) { team, teamInfo ->
            configureTeamFeatures(team, teamInfo)
        }
    }

    private fun configureTeamFeatures(team: Team, teamInfo: UiTeamInfoUpdate): UiTeamInfoUpdate {
        val availableTeamApothecaries = team.teamApothecaries.count { !it.used }
        return teamInfo.copy(
            featureList = teamInfo.featureList.add(UiTeamFeature(
                name = "Team Apothecary",
                value = availableTeamApothecaries,
                type = UiTeamFeatureType.APOTHECARY,
                used = false
            ))
        )
    }
}
