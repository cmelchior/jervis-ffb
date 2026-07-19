package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.wizards.Wizard

class AddTeamWizard(private val team: Team, private val wizard: Wizard) : Command {
    override fun execute(state: Game) {
        team.wizards.add(wizard)
    }

    override fun undo(state: Game) {
        team.wizards.remove(wizard)
    }
}
