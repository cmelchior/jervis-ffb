package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.MortuaryAssistant

class AddMortuaryAssistant(private val team: Team) : Command {
    val assistant = MortuaryAssistant(used = false)
    override fun execute(state: Game) {
        team.mortuaryAssistants.add(assistant)
    }
    override fun undo(state: Game) {
        team.mortuaryAssistants.remove(assistant)
    }
}
