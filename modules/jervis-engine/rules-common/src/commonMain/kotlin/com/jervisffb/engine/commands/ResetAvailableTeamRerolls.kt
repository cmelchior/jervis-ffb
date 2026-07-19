package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

/**
 * Reset the `used` state for all regular team rerolls.
 */
class ResetAvailableTeamRerolls(private val team: Team) : Command {
    private var originalUsed = mutableListOf<Int>() // Track indexes modified

    override fun execute(state: Game) {
        team.rerolls.forEachIndexed { i, reroll ->
            if (reroll.rerollUsed) {
                originalUsed.add(i)
                reroll.rerollUsed = false
            }
        }
    }

    override fun undo(state: Game) {
        originalUsed.forEach { i ->
            team.rerolls[i].rerollUsed = true
        }
    }
}
