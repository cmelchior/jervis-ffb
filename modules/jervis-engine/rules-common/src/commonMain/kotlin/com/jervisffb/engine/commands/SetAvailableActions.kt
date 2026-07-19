package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

class SetAvailableActions(
    private val team: Team,
    private val type: PlayerStandardActionType,
    private val used: Int,
) : Command {
    private var originalActions: Int = 0
    private var originalUsed: Int = 0

    override fun execute(state: Game) {
        originalActions = team.turnData.availableStandardActions[type] ?: INVALID_GAME_STATE("Type has not been configured: $type")
        originalUsed = team.turnData.usedStandardActions[type] ?: 0
        team.turnData.availableStandardActions[type] = originalActions - used
        team.turnData.usedStandardActions[type] = originalUsed + used
    }

    override fun undo(state: Game) {
        team.turnData.availableStandardActions[type] = originalActions
        team.turnData.usedStandardActions[type] = originalUsed
    }

    companion object {
        fun markAsUsed(team: Team, type: PlayerStandardActionType): SetAvailableActions {
            return SetAvailableActions(
                team = team,
                type = type,
                used = 1
            )
        }
    }
}
