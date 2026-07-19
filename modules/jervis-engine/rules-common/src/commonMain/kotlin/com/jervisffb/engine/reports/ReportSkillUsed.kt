package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType

class ReportSkillUsed(
    player: Player,
    skill: Skill<*>,
) : LogEntry() {
    constructor(player: Player, skillType: SkillType) : this(player, player.getSkill(skillType))
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} used ${skill.name}"
}
