package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerTransformation
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Turn a transformed [Player] back to normal again, i.e. undo a
 * [TransformPlayer], giving them back their original position and any permanent
 * upgrades that were suppressed while they were transformed.
 *
 * Anything the player picked up while transformed stays with them, as long as it
 * is temporary. E.g., a Frog that was Chomped is still Chomped after turning
 * back into a player.
 */
class EndPlayerTransformation(private val player: Player) : Command {
    private lateinit var transformation: PlayerTransformation
    // The transformed form, so this command can be undone again.
    private lateinit var transformedForm: PlayerTransformation

    override fun execute(state: Game) {
        transformation = player.transformation ?: INVALID_GAME_STATE("${player.id} is not transformed")
        transformedForm = PlayerTransformation.capture(player, transformation.expiresAt)
        player.transformation = null
        transformation.suppressedSkills.forEach { player.addSkill(it) }
        transformation.suppressedStatModifiers.forEach { player.addStatModifier(it) }
        player.restoreFrom(transformation)
    }

    override fun undo(state: Game) {
        transformation.suppressedSkills.forEach { player.removeSkill(it) }
        transformation.suppressedStatModifiers.forEach { player.removeStatModifier(it) }
        player.restoreFrom(transformedForm)
        player.transformation = transformation
    }
}
