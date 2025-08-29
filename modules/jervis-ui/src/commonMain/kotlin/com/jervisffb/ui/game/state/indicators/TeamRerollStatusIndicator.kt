package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2020.skills.BrilliantCoachingReroll
import com.jervisffb.engine.rules.bb2020.skills.LeaderTeamReroll
import com.jervisffb.engine.rules.bb2020.skills.RegularTeamReroll
import com.jervisffb.ui.game.UiReroll
import com.jervisffb.ui.game.UiRerollType
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.UiTeamInfoUpdate

/**
 * Show the list of rerolls available to the team. Rerolls from special sources
 * like Leader should be indicated as such. Rerolls that come back will be
 * shown as "used", while rerolls that are one-time use will be removed once
 * used.
 */
object TeamRerollStatusIndicator: FieldStatusIndicator {
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
        // Define the order in which we want to show Rerolls. Generally we want to show the
        // one that disappears first at the front of the list (we also want to use it first).
        val order = listOf(
            UiRerollType.BRILLIANT_COACHING,
            UiRerollType.LEADER,
            UiRerollType.UNKNOWN,
            UiRerollType.TEAM,
        )
        val rank = order.withIndex().associate { it.value to it.index }

        // Find all rerolls
        val rerolls = team.rerolls.mapNotNull {
            when (it) {
                is BrilliantCoachingReroll -> {
                    if (!it.rerollUsed) {
                        UiReroll("Brilliant Coaching Reroll", UiRerollType.BRILLIANT_COACHING, it.rerollUsed)
                    } else {
                        null
                    }
                }

                is LeaderTeamReroll -> {
                    // We assume the rules will remove it if the player leaves the field
                    UiReroll("Leader Reroll", UiRerollType.LEADER, it.rerollUsed)
                }

                is RegularTeamReroll -> {
                    UiReroll("Team Reroll", UiRerollType.TEAM, it.rerollUsed)
                }
            }
        }
        val reorderedRerolls = rerolls.sortedWith(
            compareBy { rank[it.type] ?: Int.MAX_VALUE } // unknown keys go last
        )
        return teamInfo.copy(
            rerolls = teamInfo.rerolls.addAll(reorderedRerolls)
        )
    }
}
