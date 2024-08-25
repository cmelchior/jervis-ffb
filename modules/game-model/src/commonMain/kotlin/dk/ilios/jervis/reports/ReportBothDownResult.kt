package dk.ilios.jervis.reports

import dk.ilios.jervis.procedures.actions.block.BothDownContext

class ReportBothDownResult(val context: BothDownContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            val downThroughWrestle = context.attackerUsesWrestle || context.defenderUsesWrestle
            val msg = if (downThroughWrestle) {
                val playerUsingWrestle = if (context.attackerUsesWrestle) context.attacker else context.defender
                val playerBeingWrestled = if (context.attackerUsesWrestle) context.defender else context.attacker
                "${playerUsingWrestle.name} wrestles $playerBeingWrestled to the ground."
            } else {
                val lines = mutableListOf<String>()
                if (!context.attackUsesBlock) {
                    lines.add("${context.attacker.name} is knocked down by Both Down")
                }
                if (!context.defenderUsesBlock) {
                    lines.add("${context.defender.name} is knocked down by Both Down")
                }
                lines.joinToString(separator = "\n")
            }
            return msg
        }
}
