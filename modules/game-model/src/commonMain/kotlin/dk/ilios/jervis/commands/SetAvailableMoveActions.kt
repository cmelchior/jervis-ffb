package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Availability
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.TeamTurnData
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.utils.INVALID_GAME_STATE


class SetAvailableActions(
    private val team: Team,
    private val type: PlayerActionType,
    private val availableActions: Int
): Command {

    private var originalActions: Int = 0

    override fun execute(state: Game, controller: GameController) {
        originalActions = team.turnData.availableActions[type] ?: INVALID_GAME_STATE("Type has not been configured: $type")
        team.turnData.availableActions[type] = availableActions
    }

    override fun undo(state: Game, controller: GameController) {
        team.turnData.availableActions[type] = originalActions
    }
}
