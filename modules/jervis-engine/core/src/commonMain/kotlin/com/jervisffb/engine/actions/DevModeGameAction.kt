package com.jervisffb.engine.actions

import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.BallId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.StatModifier
import kotlinx.serialization.Serializable

/**
 * Interface for game actions that can modify the game state "out-of-order",
 * i.e., they can do things that you would normally not allow. This can be
 * useful during development or as an admin tool.
 *
 * The [com.jervisffb.engine.GameEngineController] should accept these in
 * [com.jervisffb.engine.GameEngineController.handleAction] and leave access
 * control to other layers.
 */
sealed interface DevModeGameAction: GameAction

@Serializable
data class ChangePlayerBaseStat(
    val playerId: PlayerId,
    val type: StatModifier.Type,
    val modifier: Int
): DevModeGameAction {
    fun getPlayer(state: Game): Player = state.getPlayerById(playerId)
}

@Serializable
data class AddPlayerSkill(
    val playerId: PlayerId,
    val skill: SkillId
): DevModeGameAction {
    fun getPlayer(state: Game): Player = state.getPlayerById(playerId)
}

@Serializable
data class RemovePlayerSkill(
    val playerId: PlayerId,
    val skill: SkillId
): DevModeGameAction {
    fun getPlayer(state: Game): Player = state.getPlayerById(playerId)
}

@Serializable
data class AddPlayerKeyword(
    val playerId: PlayerId,
    val keyword: PlayerKeyword
): DevModeGameAction {
    fun getPlayer(state: Game): Player = state.getPlayerById(playerId)
}

@Serializable
data class RemovePlayerKeyword(
    val playerId: PlayerId,
    val keyword: PlayerKeyword
): DevModeGameAction {
    fun getPlayer(state: Game): Player = state.getPlayerById(playerId)
}

@Serializable
data class SetPlayerState(
    val playerId: PlayerId,
    val state: PlayerState,
    val location: Location,
) : DevModeGameAction {
    constructor(playerId: PlayerId, state: PlayerState, x: Int, y: Int) : this(playerId, state, PitchCoordinate(x, y))
    // The GameController is responsible for the full validation. Here we just validate the most obvious things
    init {
        if (location is PitchCoordinate) {
            if (location.x < 0 || location.y < 0) throw IllegalArgumentException("Invalid pitch coordinate: $location")
        }
        val isDogoutState = (state is PlayerDogoutState)
        if (isDogoutState && location != Dogout) throw IllegalArgumentException("The chosen state is only valid if the location is Dogout: $state")
    }
    fun getPlayer(state: Game): Player = state.getPlayerById(playerId)
}

@Serializable
data class SetBallState(
    val ballId: BallId,
    val x: Int,
    val y: Int,
    val ballState: BallState? = null,
    val carriedBy: PlayerId? = null,
) : DevModeGameAction {
    fun getBall(state: Game): Ball = state.balls.first { it.id == ballId }
    fun getPlayer(state: Game): Player? = carriedBy?.let { state.getPlayerById(it) }
    fun coordinate(): PitchCoordinate = PitchCoordinate(x, y)
}
