package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.PlayerSpecialActionType
import dk.ilios.jervis.utils.INVALID_GAME_STATE

class SetAvailableSpecialActions(
    private val team: Team,
    private val type: PlayerSpecialActionType,
    private val availableActions: Int,
) : Command {
    private var originalActions: Int = 0

    override fun execute(state: Game, controller: GameController) {
        originalActions = team.turnData.availableSpecialActions[type] ?: INVALID_GAME_STATE("Type has not been configured: $type")
        team.turnData.availableSpecialActions[type] = availableActions
    }

    override fun undo(state: Game, controller: GameController) {
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
