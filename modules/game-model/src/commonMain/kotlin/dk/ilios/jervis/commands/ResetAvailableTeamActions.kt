package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.PlayerActionType

class ResetAvailableTeamActions(
    private val team: Team,
    private val moveActions: Int,
    private val passActions: Int,
    private val handOffActions: Int,
    private val blockActions: Int,
    private val blitzActions: Int,
    private val foulActions: Int,
) : Command {
    var originalMoveActions = 0
    var originalPassActions = 0
    var originalHandOffActions = 0
    var originalBlockActions = 0
    var originalBlitzActions = 0
    var originalFoulActions = 0

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalMoveActions = team.turnData.availableActions[PlayerActionType.MOVE]!!
        originalPassActions = team.turnData.availableActions[PlayerActionType.PASS]!!
        originalHandOffActions = team.turnData.availableActions[PlayerActionType.HAND_OFF]!!
        originalBlockActions = team.turnData.availableActions[PlayerActionType.BLOCK]!!
        originalBlitzActions = team.turnData.availableActions[PlayerActionType.BLITZ]!!
        originalFoulActions = team.turnData.availableActions[PlayerActionType.FOUL]!!
        team.turnData.let {
            it.moveActions = moveActions
            it.passActions = passActions
            it.handOffActions = handOffActions
            it.blockActions = blockActions
            it.blitzActions = blitzActions
            it.foulActions = foulActions
        }
        team.notifyUpdate()
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        team.turnData.let {
            it.moveActions = originalMoveActions
            it.passActions = originalPassActions
            it.handOffActions = originalHandOffActions
            it.blockActions = originalBlockActions
            it.blitzActions = originalBlitzActions
            it.foulActions = originalFoulActions
        }
        team.notifyUpdate()
    }
}
