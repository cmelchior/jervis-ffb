package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.skills.RerollSource

class SetTeamRerollUsed(
    private val team: Team,
    private val source: RerollSource,
    private val markUsed: Boolean = true
) : Command {
    private var original: Boolean = false
    private var originalRerollUsed: Boolean = false

    override fun execute(state: Game,) {
        original = source.rerollUsed
        originalRerollUsed = team.usedRerollThisTurn
        source.rerollUsed = markUsed
        team.usedRerollThisTurn = markUsed
    }

    override fun undo(state: Game) {
        source.rerollUsed = original
        team.usedRerollThisTurn = originalRerollUsed
    }
}
