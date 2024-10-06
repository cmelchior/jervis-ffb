package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.Bribe

class AddBribe(private val team: Team, private val bribe: Bribe) : Command {
    override fun execute(state: Game, controller: GameController) {
        team.bribes.add(bribe)
    }

    override fun undo(state: Game, controller: GameController) {
        team.bribes.remove(bribe)
    }
}
