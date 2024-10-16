package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.Bribe

class AddBribe(private val team: Team, private val bribe: Bribe) : Command {
    override fun execute(state: Game) {
        team.bribes.add(bribe)
    }

    override fun undo(state: Game) {
        team.bribes.remove(bribe)
    }
}
