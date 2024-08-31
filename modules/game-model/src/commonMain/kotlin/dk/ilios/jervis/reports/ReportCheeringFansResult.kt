package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.DieResult

class ReportCheeringFansResult(
    result: State,
    dieKickingTeam: DieResult,
    cheerLeadersKickingTeam: Int,
    dieReceivingTeam: DieResult,
    cheerLeadersReceivingTeam: Int,
) : LogEntry() {
    enum class State {
        KICKER_WINS,
        RECEIVER_WINS,
        DRAW,
    }

    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String

    init {
        var msg = "Cheering Fans roll-off: [${dieKickingTeam.value} + $cheerLeadersKickingTeam] vs. [${dieReceivingTeam.value} + $cheerLeadersReceivingTeam].\n"
        when (result) {
            State.KICKER_WINS -> msg += "Kicking team wins and gets to roll on the Prayers Of Nuffle table."
            State.RECEIVER_WINS -> msg += "Receiving team wins and gets to roll on the Prayers Of Nuffle table."
            State.DRAW -> "It is a stand-off. Neither team gets to roll on the Prayers of Nuffle table."
        }
        message = msg
    }
}
