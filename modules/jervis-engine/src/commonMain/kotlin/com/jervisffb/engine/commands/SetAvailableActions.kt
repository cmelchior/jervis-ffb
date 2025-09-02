package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

class SetAvailableActions(
    private val team: Team,
    private val type: PlayerStandardActionType,
    private val availableActions: Int,
) : Command {
    private var originalActions: Int = 0

    override fun execute(state: Game) {
        originalActions = team.turnData.availableStandardActions[type] ?: INVALID_GAME_STATE("Type has not been configured: $type")
        team.turnData.availableStandardActions[type] = availableActions
    }

    override fun undo(state: Game) {
        team.turnData.availableStandardActions[type] = originalActions
    }

    companion object {
        fun markAsUsed(team: Team, type: PlayerStandardActionType): SetAvailableActions {
            val newValue = when (type) {
                PlayerStandardActionType.MOVE -> team.turnData.moveActions - 1
                PlayerStandardActionType.PASS -> team.turnData.passActions - 1
                PlayerStandardActionType.HAND_OFF -> team.turnData.handOffActions - 1
                PlayerStandardActionType.THROW_TEAM_MATE -> TODO()
                PlayerStandardActionType.BLOCK -> team.turnData.blockActions - 1
                PlayerStandardActionType.BLITZ -> team.turnData.blitzActions - 1
                PlayerStandardActionType.FOUL -> team.turnData.foulActions - 1
                PlayerStandardActionType.SPECIAL -> TODO()
                PlayerStandardActionType.SECURE_THE_BALL -> team.turnData.secureTheBallActions - 1
            }

            return SetAvailableActions(
                team,
                type,
                newValue
            )
        }
    }
}
