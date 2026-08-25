package com.jervisffb.ui.game.mappings

import com.jervisffb.engine.bb2020.procedures.rerolls.BrilliantCoachingReroll2020
import com.jervisffb.engine.bb2020.procedures.rerolls.StandardTeamReroll2020
import com.jervisffb.engine.bb2020.procedures.rerolls.TeamReroll2020
import com.jervisffb.engine.bb2025.procedures.rerolls.BrilliantCoachingReroll
import com.jervisffb.engine.bb2025.procedures.rerolls.ExtraTeamTrainingReroll
import com.jervisffb.engine.bb2025.procedures.rerolls.LeaderTeamReroll
import com.jervisffb.engine.bb2025.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.bb2025.procedures.rerolls.TeamMascotReroll
import com.jervisffb.engine.bb2025.procedures.rerolls.TeamReroll2025
import com.jervisffb.engine.rules.common.rerolls.TeamReroll

// Map all engine team rerolls to UI rerolls
enum class UiTeamReroll {
    BRILLIANT_COACHING,
    LEADER,
    MASCOT,
    EXTRA_TRAINING,
    TEAM;

    companion object : UiMapping<TeamReroll, UiTeamReroll> {
        override fun mapFrom(el: TeamReroll): UiTeamReroll {
            return when (el) {
                is TeamReroll2020 -> {
                    when (el) {
                        is BrilliantCoachingReroll2020 -> BRILLIANT_COACHING
                        is StandardTeamReroll2020 -> TEAM
                    }
                }
                is TeamReroll2025 -> {
                    when (el) {
                        is BrilliantCoachingReroll -> BRILLIANT_COACHING
                        is ExtraTeamTrainingReroll -> EXTRA_TRAINING
                        is LeaderTeamReroll -> LEADER
                        is StandardTeamReroll -> TEAM
                        is TeamMascotReroll -> MASCOT
                    }
                }
                else -> error("Unsupported reroll type: $el")
            }
        }
    }
}
