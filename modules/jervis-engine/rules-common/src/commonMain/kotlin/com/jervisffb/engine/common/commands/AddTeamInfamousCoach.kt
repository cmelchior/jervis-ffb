package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.infamouscoach.InfamousCoachingStaff

class AddTeamInfamousCoach(private val team: Team, private val staff: InfamousCoachingStaff) : Command {
    override fun execute(state: Game) {
        team.infamousCoachingStaff.add(staff)
    }

    override fun undo(state: Game) {
        team.infamousCoachingStaff.remove(staff)
    }
}
