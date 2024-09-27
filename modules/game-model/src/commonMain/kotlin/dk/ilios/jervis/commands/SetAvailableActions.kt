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
            return SetAvailableActions(
                team,
                type,
                team.turnData.moveActions - 1
            )
        }
    }
}
