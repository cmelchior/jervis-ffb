package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.bb2020.skills.Skill
import com.jervisffb.engine.rules.bb2020.skills.SpecialActionProvider
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Mark a skill as have being used or not.
 */
class SetSpecialActionSkillUsed(private val player: Player, private val skill: Skill, val used: Boolean) : Command {
    private var originalUsed: Boolean = false

    init {
        if (skill !is SpecialActionProvider) INVALID_GAME_STATE("SpecialActionProvider is required: $skill")
    }

    override fun execute(state: Game) {
        this.originalUsed = (skill as SpecialActionProvider).isSpecialActionUsed
        skill.isSpecialActionUsed = this@SetSpecialActionSkillUsed.used
    }

    override fun undo(state: Game) {
        (skill as SpecialActionProvider).isSpecialActionUsed = originalUsed
    }
}
