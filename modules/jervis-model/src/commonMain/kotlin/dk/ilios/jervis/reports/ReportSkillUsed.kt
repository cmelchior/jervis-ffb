package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.skills.Skill

class ReportSkillUsed(
    player: Player,
    skill: Skill,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} used ${skill.name}"
}
