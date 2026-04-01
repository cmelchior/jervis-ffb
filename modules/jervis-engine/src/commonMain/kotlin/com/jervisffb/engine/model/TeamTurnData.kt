package com.jervisffb.engine.model

import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType

class TeamTurnData(private val game: Game) {
    var moveActions: Int
        get() = availableStandardActions[PlayerStandardActionType.MOVE]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.MOVE] = value
        }
    var passActions: Int
        get() = availableStandardActions[PlayerStandardActionType.PASS]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.PASS] = value
        }
    var handOffActions: Int
        get() = availableStandardActions[PlayerStandardActionType.HAND_OFF]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.HAND_OFF] = value
        }
    var blockActions: Int
        get() = availableStandardActions[PlayerStandardActionType.BLOCK]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.BLOCK] = value
        }
    var blitzActions: Int
        get() = availableStandardActions[PlayerStandardActionType.BLITZ]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.BLITZ] = value
        }
    var foulActions: Int
        get() = availableStandardActions[PlayerStandardActionType.FOUL]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.FOUL] = value
        }
    var throwTeamMateActions: Int
        get() = availableStandardActions[PlayerStandardActionType.THROW_TEAM_MATE]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.THROW_TEAM_MATE] = value
        }
    var secureTheBallActions: Int
        get() = availableStandardActions[PlayerStandardActionType.SECURE_THE_BALL]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.SECURE_THE_BALL] = value
        }

    val availableStandardActions =
        mutableMapOf(
            PlayerStandardActionType.MOVE to 0,
            PlayerStandardActionType.PASS to 0,
            PlayerStandardActionType.HAND_OFF to 0,
            PlayerStandardActionType.BLOCK to 0,
            PlayerStandardActionType.BLITZ to 0,
            PlayerStandardActionType.FOUL to 0,
            PlayerStandardActionType.THROW_TEAM_MATE to 0,
            PlayerStandardActionType.SECURE_THE_BALL to 0,
        )
    val availableSpecialActions = mutableMapOf<PlayerSpecialActionType, Int>()
    val usedStandardActions = mutableMapOf(
        PlayerStandardActionType.MOVE to 0,
        PlayerStandardActionType.PASS to 0,
        PlayerStandardActionType.HAND_OFF to 0,
        PlayerStandardActionType.BLOCK to 0,
        PlayerStandardActionType.BLITZ to 0,
        PlayerStandardActionType.FOUL to 0,
        PlayerStandardActionType.THROW_TEAM_MATE to 0,
        PlayerStandardActionType.SECURE_THE_BALL to 0,
    )
}
