package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.common.inducements.PlagueDoctor
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class AddPlagueDoctor(private val team: Team) : Command {
    val doctor = PlagueDoctor(used = false)
    override fun execute(state: Game) {
        team.plagueDoctors.add(doctor)
    }
    override fun undo(state: Game) {
        team.plagueDoctors.remove(doctor)
    }
}
