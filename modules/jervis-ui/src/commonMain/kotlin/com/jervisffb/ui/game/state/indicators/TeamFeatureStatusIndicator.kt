package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.modifiers.TeamFeatureType
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
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
        val featureList = mutableListOf<UiTeamFeature>()

        // Team Apothecary
        val availableTeamApothecaries = team.teamApothecaries.count { !it.used }
        if (availableTeamApothecaries > 0) {
            featureList.add(
                UiTeamFeature(
                    name = "Team Apothecary",
                    value = availableTeamApothecaries,
                    type = UiTeamFeatureType.APOTHECARY,
                    used = false
                )
            )
        }

        // Cheering Fans Offensive Assist
        if (team.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST)) {
            featureList.add(
                UiTeamFeature(
                    name = "+1 Offensive Assist on next Block",
                    value = 1,
                    type = UiTeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST,
                    used = false
                )
            )
        }

        // Team Captain can roll to keep rerolls
        if (team.specialRules.contains(TeamSpecialRule.TEAM_CAPTAIN)) {
            val rules = team.game.rules
            val teamCaptainOnField = team.any { it.location.isOnField(rules) && it.specialRules.contains(PlayerSpecialRule.TEAM_CAPTAIN)}
            if (teamCaptainOnField) {
                featureList.add(
                    UiTeamFeature(
                        name = "Team Captain on the Field",
                        value = 1,
                        type = UiTeamFeatureType.TEAM_CAPTAIN,
                        used = false
                    )
                )
            }
        }

        return teamInfo.copy(
            featureList = teamInfo.featureList.addAll(featureList)
        )
    }
}
