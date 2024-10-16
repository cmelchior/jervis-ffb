package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState

/**
 * Sets the player [PlayerState] in [Player.state].
 * Since this commonly also affects the tackle zones of the player in [Player.hasTackleZones].
 * It is also possible to define that change here.
 */
class SetPlayerState(
    private val player: Player,
    private val state: PlayerState,
    // Also set the players tackle zones as part of setting the state
    // `null` indicate no change
    private val hasTackleZones: Boolean? = null
) : Command {
    private lateinit var originalState: PlayerState
    private var originalHasTackleZones: Boolean = false

    override fun execute(state: Game) {
        this.originalState = player.state
        this.originalHasTackleZones = player.hasTackleZones
        player.apply {
            this.state = this@SetPlayerState.state
            if (this@SetPlayerState.hasTackleZones != null) {
                this.hasTackleZones = this@SetPlayerState.hasTackleZones
            }
            notifyUpdate()
        }
    }

    override fun undo(state: Game) {
        player.apply {
            if (this@SetPlayerState.hasTackleZones != null) {
                this.hasTackleZones = originalHasTackleZones
            }
            this.state = originalState
            notifyUpdate()
        }
    }
}
