package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerTransformation
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.utils.requireGameState

/**
 * Temporarily turn a [Player] into another [Position], e.g. when the "Zap!"
 * spell turns them into a Frog.
 *
 * The player keeps their identity, but the transformation can override stats,
 * skills, keywords and appearance. See [PlayerTransformation] for more
 * information.
 *
 * The player turns back to normal on their own once [expiresAt] is reached, see
 * `getResetPlayerTemporaryModifiersCommands`. Use [EndPlayerTransformation] to
 * do it earlier.
 */
class TransformPlayer(
    private val player: Player,
    private val position: Position,
    private val expiresAt: Duration,
) : Command {
    private lateinit var transformation: PlayerTransformation

    override fun execute(state: Game) {
        requireGameState(!player.isTransformed) {
            "${player.id} is already transformed into a ${player.position.titleSingular}"
        }
        transformation = PlayerTransformation.capture(player, expiresAt)
        transformation.suppressedSkills.forEach { player.removeSkill(it) }
        transformation.suppressedStatModifiers.forEach { player.removeStatModifier(it) }
        player.transformInto(position)
        player.transformation = transformation
    }

    override fun undo(state: Game) {
        player.transformation = null
        transformation.suppressedSkills.forEach { player.addSkill(it) }
        transformation.suppressedStatModifiers.forEach { player.addStatModifier(it) }
        player.restoreFrom(transformation)
    }
}
