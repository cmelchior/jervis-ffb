package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.Apothecary

class SetApothecaryUsed(
    private val team: Team,
    private val apothecary: Apothecary,
    private val used: Boolean
) : Command {
    private var originalValue: Boolean = false

    override fun execute(state: Game, controller: GameController) {
        originalValue = apothecary.used
        apothecary.used = used
        team.notifyUpdate()
   }

    override fun undo(state: Game, controller: GameController) {
        apothecary.used = originalValue
        team.notifyUpdate()
    }
}
