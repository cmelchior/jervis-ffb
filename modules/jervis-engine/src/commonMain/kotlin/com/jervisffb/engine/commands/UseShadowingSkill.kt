package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.getSkill
import com.jervisffb.engine.rules.bb2025.skills.Shadowing

/**
 * Only for BB2025. Mark a player as having used shaowing once.
 */
class UseShadowingSkill(private val player: Player) : Command {
    private var originalUsed: Boolean = false

    override fun execute(state: Game) {
        val skill = player.getSkill<Shadowing>()
        skill.usedThisTurn += 1
    }

    override fun undo(state: Game) {
        val skill = player.getSkill<Shadowing>()
        skill.usedThisTurn -= 1
    }
}
