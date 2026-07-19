package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.PlagueDoctor

class AddPlagueDoctor(private val team: Team) : Command {
    val doctor = PlagueDoctor(used = false)
    override fun execute(state: Game) {
        team.plagueDoctors.add(doctor)
    }
    override fun undo(state: Game) {
        team.plagueDoctors.remove(doctor)
    }
}
