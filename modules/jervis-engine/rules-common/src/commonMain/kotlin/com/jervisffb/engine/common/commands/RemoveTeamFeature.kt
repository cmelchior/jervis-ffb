package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.modifiers.TeamFeature

class RemoveTeamFeature(private val team: Team, val feature: TeamFeature) : Command {
    override fun execute(state: Game) {
        team.removeFeature(feature)
    }

    override fun undo(state: Game) {
        team.addFeature(feature)
    }
}
