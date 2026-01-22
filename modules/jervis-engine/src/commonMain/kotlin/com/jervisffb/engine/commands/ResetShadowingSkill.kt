package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.getSkill
import com.jervisffb.engine.rules.bb2025.skills.Shadowing

/**
 * Only for BB2025. Used to reset shadowing counter at the end of turn.
 */
class ResetShadowingSkill(private val player: Player) : Command {
    private var originalCount = 0

    override fun execute(state: Game) {
        val skill = player.getSkill<Shadowing>()
        originalCount = skill.usedThisTurn
        skill.usedThisTurn = 0
    }

    override fun undo(state: Game) {
        val skill = player.getSkill<Shadowing>()
        skill.usedThisTurn = originalCount
    }
}
