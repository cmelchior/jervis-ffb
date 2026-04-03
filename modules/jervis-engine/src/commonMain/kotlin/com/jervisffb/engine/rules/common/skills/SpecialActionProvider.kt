package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType

/**
 * Interface for skills that provide a special player action.
 *
 * Note, for skills that replace blocks, the procedure being referenced is the Standalone
 * variant. Special skills that can be used as part of a Multiple Block
 * are defined in [com.jervisffb.engine.model.context.BB2020MultipleBlockContext] and will
 * be handled separately there.
 */
interface SpecialActionProvider {
    val specialAction: PlayerSpecialActionType
    var isSpecialActionUsed: Boolean

    /**
     * Returns `true` if this actions is available to use when activating the
     * player. `false` if not.
     */
    fun isActionAvailable(state: Game, rules: Rules): Boolean

    /**
     * Helper method that checks if a player has a skill available _AND_ is standing next to an opponent
     */
    fun isSkillAvailableAndAdjacentToOpponent(player: Player, skill: SkillType, state: Game, rules: Rules): Boolean {
        if (!player.location.isOnField(rules)) return false
        val isSkillAvailable = player.isSkillAvailable(skill)
        val isSkillActionUsed = isSpecialActionUsed
        val actionsAvailable = state.activeTeamOrThrow().turnData.availableSpecialActions.getOrElse(specialAction) { 0 } // Should only be `null` during Charge as no special actions are available there
        val isActionAvailable = (actionsAvailable > 0)
        val isAdjacentToOpponent = player.coordinates.getSurroundingCoordinates(rules, 1)
            .mapNotNull { state.field[it].player }
            .filter { otherPlayer -> otherPlayer.team != player.team }
            .filter { otherPlayer -> rules.isStanding(otherPlayer)}
            .any { otherPlayer -> rules.isMarking(player, otherPlayer)}
        return (isSkillAvailable && !isSkillActionUsed && isActionAvailable && isAdjacentToOpponent)
    }
}
