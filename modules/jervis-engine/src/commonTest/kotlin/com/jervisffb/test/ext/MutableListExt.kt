package com.jervisffb.test.ext

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType

fun MutableList<Skill>.addNewSkill(player: Player, type: SkillType) {
    val skill = player.team.game.rules.createSkill(player, type.id())
    this.add(skill)
}

