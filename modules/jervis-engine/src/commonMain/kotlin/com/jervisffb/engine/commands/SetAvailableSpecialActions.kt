package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.PlayerSpecialActionType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

class SetAvailableSpecialActions(
    private val team: Team,
    private val type: PlayerSpecialActionType,
    private val availableActions: Int,
) : Command {
    private var originalActions: Int = 0

    override fun execute(state: Game) {
        originalActions = team.turnData.availableSpecialActions[type] ?: INVALID_GAME_STATE("Type has not been configured: $type")
        team.turnData.availableSpecialActions[type] = availableActions
    }

    override fun undo(state: Game) {
        team.turnData.availableSpecialActions[type] = originalActions
    }

    companion object {
        fun markAsUsed(team: Team, type: PlayerSpecialActionType): SetAvailableSpecialActions {
            return SetAvailableSpecialActions(
                team,
                type,
                team.turnData.availableSpecialActions[type]!! - 1
            )
        }
    }

}
