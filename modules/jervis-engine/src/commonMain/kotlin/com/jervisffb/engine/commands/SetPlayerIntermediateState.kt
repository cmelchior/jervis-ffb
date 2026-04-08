package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerIntermediateState

/**
 * Sets the player [com.jervisffb.engine.model.PlayerIntermediateState] in
 * [Player.intermediateState].
 *
 * Since this commonly also affects the tackle zones of the player in
 * [Player.hasTackleZones]. It is also possible to define that change here.
 *
 * Note, since intermediate states are, well, intermediate, this state will be
 * reset whenever [SetPlayerState] sets a new player state. It is unnecessary to
 * do this manually.
 */
class SetPlayerIntermediateState(
    private val player: Player,
    private val state: PlayerIntermediateState?,
    // Also set the players tackle zones as part of setting the state
    // `null` indicate no change.
    // For now, all intermediate states will result in loosing tackles, so we just set it here.
    // If this ever changes, the default value must not change without first updating all callsites.
    private val hasTackleZones: Boolean = false
) : Command {
    private lateinit var originalState: PlayerIntermediateState
    private var originalHasTackleZones: Boolean = false

    override fun execute(state: Game) {
        this.originalHasTackleZones = player.hasTackleZones
        player.apply {
            this.intermediateState = this@SetPlayerIntermediateState.state
            this.hasTackleZones = this@SetPlayerIntermediateState.hasTackleZones
        }
    }

    override fun undo(state: Game) {
        player.apply {
            this.hasTackleZones = originalHasTackleZones
            this.intermediateState = originalState
        }
    }
}
