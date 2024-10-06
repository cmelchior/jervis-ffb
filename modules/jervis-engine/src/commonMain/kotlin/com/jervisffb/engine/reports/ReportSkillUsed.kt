package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.bb2020.skills.Skill

class ReportSkillUsed(
    player: Player,
    skill: Skill,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} used ${skill.name}"
}
