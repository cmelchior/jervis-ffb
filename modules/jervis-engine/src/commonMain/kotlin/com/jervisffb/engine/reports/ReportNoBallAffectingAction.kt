package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

class ReportNoBallAffectingAction(
    player: Player,
    actionType: ActionType,
) : LogEntry() {
    enum class ActionType {
        CATCH,
        PICKUP,
        SECURE_THE_BALL
    }
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        when (actionType) {
            ActionType.CATCH -> append("${player.name} has No Ball so cannot catch the ball")
            ActionType.PICKUP -> append("${player.name} has No Ball so cannot pickup the ball")
            ActionType.SECURE_THE_BALL -> append("${player.name} has No Ball so cannot Secure the Ball")
        }
    }
}
