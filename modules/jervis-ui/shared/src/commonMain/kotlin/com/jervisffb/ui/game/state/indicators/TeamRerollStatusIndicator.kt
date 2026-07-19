package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_icon_brilliant_coaching_reroll
import com.jervisffb.shared.generated.resources.jervis_icon_leader_reroll
import com.jervisffb.shared.generated.resources.jervis_icon_mascot_reroll
import com.jervisffb.shared.generated.resources.jervis_icon_team_reroll
import com.jervisffb.ui.game.UiReroll
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.UiTeamInfoUpdate
import com.jervisffb.ui.game.mappings.UiTeamReroll

/**
 * Show the list of rerolls available to the team. Rerolls from special sources
 * like Leader should be indicated as such. Rerolls that come back will be
 * shown as "used", while rerolls that are one-time use will be removed once
 * used.
 */
object TeamRerollStatusIndicator: PitchStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        acc.updateTeamInfo(state.homeTeam) { team, teamInfo ->
            configureTeamRerolls(team, teamInfo)
        }
        acc.updateTeamInfo(state.awayTeam) { team, teamInfo ->
            configureTeamRerolls(team, teamInfo)
        }
    }

    private fun configureTeamRerolls(team: Team, teamInfo: UiTeamInfoUpdate): UiTeamInfoUpdate {
        // Define the order in which we want to show Rerolls. Generally, we want to show the
        // one that disappears first at the front of the list (we also want to use it first).
        val order = listOf(
            UiTeamReroll.BRILLIANT_COACHING,
            UiTeamReroll.LEADER,
            UiTeamReroll.MASCOT,
            UiTeamReroll.TEAM,
        )
        val rank = order.withIndex().associate { it.value to it.index }

        // Find all rerolls
        val rerolls = team.rerolls.map { reroll ->
            val uiType = UiTeamReroll.mapFrom(reroll)
            val (title, image) = when (uiType) {
                UiTeamReroll.BRILLIANT_COACHING ->"Brilliant Coaching Reroll" to Res.drawable.jervis_icon_brilliant_coaching_reroll
                UiTeamReroll.LEADER -> "Leader Reroll" to Res.drawable.jervis_icon_leader_reroll
                UiTeamReroll.MASCOT -> "Mascot Reroll" to Res.drawable.jervis_icon_mascot_reroll
                UiTeamReroll.TEAM -> "Team Reroll" to Res.drawable.jervis_icon_team_reroll
                UiTeamReroll.EXTRA_TRAINING -> "Team Reroll (Extra Team Training)" to  Res.drawable.jervis_icon_team_reroll
            }
            UiReroll(title, uiType, image, reroll.rerollUsed, reroll.enabled)
        }
        val reorderedRerolls = rerolls.sortedWith(
            compareBy<UiReroll> { rank[it.type] }.thenBy { !it.isAvailable() }
        )

        return teamInfo.copy(
            rerolls = teamInfo.rerolls.addAll(reorderedRerolls)
        )
    }
}
