package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.PlayerStandardActionType
import dk.ilios.jervis.utils.INVALID_GAME_STATE

class SetAvailableActions(
    private val team: Team,
    private val type: PlayerStandardActionType,
    private val availableActions: Int,
) : Command {
    private var originalActions: Int = 0

    override fun execute(state: Game, controller: GameController) {
        originalActions = team.turnData.availableStandardActions[type] ?: INVALID_GAME_STATE("Type has not been configured: $type")
        team.turnData.availableStandardActions[type] = availableActions
    }

    override fun undo(state: Game, controller: GameController) {
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
            }

            return SetAvailableActions(
                team,
                type,
                newValue
            )
        }
    }
}
