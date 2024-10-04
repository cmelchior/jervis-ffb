package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.skills.RegularTeamReroll
import dk.ilios.jervis.rules.skills.TeamReroll

/**
 * Reset the `used` state for all regular team rerolls.
 */
class ResetAvailableTeamRerolls(private val team: Team) : Command {
    private var originalUsed = mutableListOf<Int>() // Track indexes modified

    override fun execute(state: Game, controller: GameController) {
        team.rerolls.forEachIndexed { i, reroll ->
            if (reroll.rerollUsed) {
                originalUsed.add(i)
                reroll.rerollUsed = false
            }
        }
    }

    override fun undo(state: Game, controller: GameController) {
        originalUsed.forEach { i ->
            team.rerolls[i].rerollUsed = true
        }
    }
}
