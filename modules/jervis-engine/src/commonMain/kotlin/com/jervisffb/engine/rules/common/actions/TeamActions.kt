package com.jervisffb.engine.rules.common.actions

import kotlinx.serialization.Serializable

@Serializable
abstract class TeamActions {
    operator fun get(type: ActionType): PlayerAction {
        return when (type) {
            is PlayerStandardActionType -> get(type)
            is PlayerSpecialActionType -> get(type)
        }
    }
    abstract operator fun get(type: PlayerStandardActionType): PlayerAction
    abstract operator fun get(type: PlayerSpecialActionType): PlayerAction

    abstract val move: PlayerAction
    abstract val pass: PlayerAction
    abstract val handOff: PlayerAction
    abstract val block: PlayerAction
    abstract val blitz: PlayerAction
    abstract val foul: PlayerAction
    abstract val specialActions: Set<PlayerAction>
}
