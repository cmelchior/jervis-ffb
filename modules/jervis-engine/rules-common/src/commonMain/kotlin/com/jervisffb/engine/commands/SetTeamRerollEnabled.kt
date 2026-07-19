package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.rerolls.TeamReroll

class SetTeamRerollEnabled(
    private val team: Team,
    private val source: TeamReroll,
    private val enabled: Boolean = true
) : Command {
    private var originalEnabled: Boolean = false

    override fun execute(state: Game,) {
        originalEnabled = source.enabled
        source.enabled = enabled
    }

    override fun undo(state: Game) {
        source.enabled = originalEnabled
    }
}
