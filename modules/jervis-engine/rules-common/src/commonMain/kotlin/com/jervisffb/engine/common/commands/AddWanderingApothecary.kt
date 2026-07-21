package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.common.inducements.WanderingApothecary

class AddWanderingApothecary(private val team: Team) : Command {
    val apothecary = WanderingApothecary(used = false)
    override fun execute(state: Game) {
        team.wanderingApothecaries.add(apothecary)
    }
    override fun undo(state: Game) {
        team.wanderingApothecaries.remove(apothecary)
    }
}
