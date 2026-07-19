package com.jervisffb.ui.game.mappings

import com.jervisffb.engine.rules.common.procedures.rerolls.BB2020BrilliantCoachingReroll
import com.jervisffb.engine.rules.common.procedures.rerolls.BB2020StandardTeamReroll
import com.jervisffb.engine.rules.common.procedures.rerolls.BB2020TeamReroll
import com.jervisffb.engine.rules.common.procedures.rerolls.BB2025TeamReroll
import com.jervisffb.engine.rules.common.procedures.rerolls.BrilliantCoachingReroll
import com.jervisffb.engine.rules.common.procedures.rerolls.ExtraTeamTrainingReroll
import com.jervisffb.engine.rules.common.procedures.rerolls.LeaderTeamReroll
import com.jervisffb.engine.rules.common.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.rules.common.procedures.rerolls.TeamMascotReroll
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
                is BB2020TeamReroll -> {
                    when (el) {
                        is BB2020BrilliantCoachingReroll -> BRILLIANT_COACHING
                        is BB2020StandardTeamReroll -> TEAM
                    }
                }
                is BB2025TeamReroll -> {
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
