package com.jervisffb.engine.bb2025.procedures

import com.jervisffb.engine.bb2025.modifiers.PlayerStatusEffectType2025
import com.jervisffb.engine.bb2025.modifiers.isChomped
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.common.commands.RemovePlayerStatusEffect
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.model.modifiers.OwnedPlayerStatusEffect
import com.jervisffb.engine.rules.common.skills.SkillType

// This player is about to move to a new location.
// - If it is Chomped, it will be removed if no longer adjacent to the Chomper.
// - If it has Chomped others, those will be removed if no longer adjacent to them.
fun getResetChompedStateCommands(
    player: Player,
    nextLocation: Location = Dogout,
    // The Chomper had a state change we know will cause Chomped to be removed.
    forceRemoveChompedByChomper: Boolean = false,
    // If `true`, we just remove Chomped status effects, but do not search for players affecte by the Chomper
    // This is needed when moving all players away from the field, e.g. at end of a drive
    ignoreChomper: Boolean = false
): Command? {
    val state = player.team.game
    val commands = mutableListOf<Command>()

    // Check if `player` is Chomped and if it can be removed
    if (player.isChomped()) {
        player.statusEffects.forEach { statusEffect ->
            if (statusEffect.type == PlayerStatusEffectType2025.CHOMPED) {
                val causedBy = (statusEffect as OwnedPlayerStatusEffect).getCausedBy(state)
                if (!causedBy.location.isAdjacent(state.rules, nextLocation)) {
                    commands.add(RemovePlayerStatusEffect(player, statusEffect))
                }
            }
        }
    }

    // Check if `player` has Chomped other players
    if (!ignoreChomper && player.hasSkill(SkillType.MONSTROUS_MOUTH)) {
        player.team.otherTeam().forEach { opponentPlayer ->
            opponentPlayer.statusEffects.forEach { statusEffect ->
                if (statusEffect is OwnedPlayerStatusEffect) {
                    val causedBy = statusEffect.causedBy
                    if (statusEffect.type == PlayerStatusEffectType2025.CHOMPED && causedBy == player.id) {
                        if (forceRemoveChompedByChomper || !opponentPlayer.location.isAdjacent(state.rules, nextLocation)) {
                            commands.add(RemovePlayerStatusEffect(opponentPlayer, statusEffect))
                        }
                    }
                }
            }
        }
    }

    return if (commands.isNotEmpty()) compositeCommandOf(commands) else null
}
