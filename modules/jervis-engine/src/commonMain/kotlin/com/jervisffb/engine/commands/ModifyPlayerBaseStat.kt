package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.StatModifier

/**
 * Modify a Players Base Stat.
 * Note, we allow this command to modify most base stat outside legal values as
 * this allows us more flexibility when changing stats.
 *
 * The total (like [Player.move]) will still be clamped to the valid ranges.
 *
 * Passing will be treated differently as going below 1, removes the ability to
 * pass.
 */
class ModifyPlayerBaseStat(private val player: Player, val stat: StatModifier.Type, val change: Int) : Command {
    val rules = player.team.game.rules
    var originalBaseStat: Int? = 0
    override fun execute(state: Game) {
        player.apply {
            when (stat) {
                StatModifier.Type.AV -> {
                    originalBaseStat = baseArmorValue
                    baseArmorValue += change
                }
                StatModifier.Type.MA -> {
                    originalBaseStat = baseMove
                    baseMove += change
                }
                StatModifier.Type.PA -> {
                    originalBaseStat = basePassing
                    basePassing = if (basePassing == null) {
                        change
                    } else {
                        basePassing!! + change
                    }
                    basePassing?.let { pa ->
                        if (pa <= 0) {
                            basePassing = null
                        }
                    }
                }
                StatModifier.Type.AG -> {
                    originalBaseStat = baseAgility
                    baseAgility += change
                }
                StatModifier.Type.ST -> {
                    originalBaseStat = baseStrength
                    baseStrength += change
                }
            }
            rules.updatePlayerStat(player, stat)
        }
    }

    override fun undo(state: Game) {
        player.apply {
            when (stat) {
                StatModifier.Type.AV -> {
                    baseArmorValue = originalBaseStat!!
                }
                StatModifier.Type.MA -> {
                    baseMove = originalBaseStat!!
                }
                StatModifier.Type.PA -> {
                    basePassing = originalBaseStat
                }
                StatModifier.Type.AG -> {
                    baseAgility = originalBaseStat!!
                }
                StatModifier.Type.ST -> {
                    baseStrength = originalBaseStat!!
                }
            }
            rules.updatePlayerStat(player, stat)
        }
    }
}
