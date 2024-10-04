package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.PlayerSpecialActionType
import dk.ilios.jervis.rules.PlayerStandardActionType

class ResetAvailableTeamActions(
    private val team: Team,
    private val moveActions: Int,
    private val passActions: Int,
    private val handOffActions: Int,
    private val blockActions: Int,
    private val blitzActions: Int,
    private val foulActions: Int,
    private val specialActions: Map<PlayerSpecialActionType, Int>
) : Command {
    var originalMoveActions = 0
    var originalPassActions = 0
    var originalHandOffActions = 0
    var originalBlockActions = 0
    var originalBlitzActions = 0
    var originalFoulActions = 0
    var originalSpecialActions = emptyMap<PlayerSpecialActionType, Int>()

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalMoveActions = team.turnData.availableStandardActions[PlayerStandardActionType.MOVE]!!
        originalPassActions = team.turnData.availableStandardActions[PlayerStandardActionType.PASS]!!
        originalHandOffActions = team.turnData.availableStandardActions[PlayerStandardActionType.HAND_OFF]!!
        originalBlockActions = team.turnData.availableStandardActions[PlayerStandardActionType.BLOCK]!!
        originalBlitzActions = team.turnData.availableStandardActions[PlayerStandardActionType.BLITZ]!!
        originalFoulActions = team.turnData.availableStandardActions[PlayerStandardActionType.FOUL]!!
        originalSpecialActions = team.turnData.availableSpecialActions.toMap()
        team.turnData.let {
            it.moveActions = moveActions
            it.passActions = passActions
            it.handOffActions = handOffActions
            it.blockActions = blockActions
            it.blitzActions = blitzActions
            it.foulActions = foulActions
            it.availableSpecialActions.clear()
            it.availableSpecialActions.putAll(specialActions)
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
            it.availableSpecialActions.clear()
            it.availableSpecialActions.putAll(originalSpecialActions)
        }
        team.notifyUpdate()
    }
}
