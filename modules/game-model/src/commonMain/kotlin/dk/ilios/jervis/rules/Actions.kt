package dk.ilios.jervis.rules

import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.procedures.DummyProcedure
import dk.ilios.jervis.procedures.MoveAction
import dk.ilios.jervis.utils.INVALID_GAME_STATE

interface Skill



//class Skills {
//
//    object com
//
//}



/**
 * Wrapper representing a players action
 */
data class PlayerAction(
    val name: String,
    val type: PlayerActionType,
    val procedure: Procedure,
    val compulsory: Boolean = false, // Players must choose this action
    val isSpecial: Boolean = false
)

/**
 * Enumerate the
 */
enum class PlayerActionType {
    MOVE,
    PASS,
    HAND_OFF,
    BLOCK,
    BLITZ,
    FOUL,
    SPECIAL
}

abstract class TeamActions {
    abstract operator fun get(type: PlayerActionType): TeamActionDescriptor
    abstract val move: TeamActionDescriptor
    abstract val pass: TeamActionDescriptor
    abstract val handOff: TeamActionDescriptor
    abstract val block: TeamActionDescriptor
    abstract val blitz: TeamActionDescriptor
    abstract val foul: TeamActionDescriptor
}


data class TeamActionDescriptor(
    val availablePrTurn: Int,
    val action: PlayerAction
)

class BB2020TeamActions: TeamActions() {

    private val actions: Map<PlayerActionType, TeamActionDescriptor>

    init {
        actions = mapOf(
            PlayerActionType.MOVE to TeamActionDescriptor(Int.MAX_VALUE, PlayerAction("Move", PlayerActionType.MOVE, MoveAction) ),
//            PlayerActionType.PASS to TeamActionDescriptor(1, PlayerAction("Pass", PlayerActionType.PASS, DummyProcedure) ),
//            PlayerActionType.HAND_OFF to TeamActionDescriptor(1,  PlayerAction("Hand-Off", PlayerActionType.HAND_OFF, DummyProcedure) ),
//            PlayerActionType.BLOCK to TeamActionDescriptor(Int.MAX_VALUE, PlayerAction("Block", PlayerActionType.BLOCK, DummyProcedure)),
//            PlayerActionType.BLITZ to TeamActionDescriptor(1,  PlayerAction("Blitz", PlayerActionType.BLITZ, DummyProcedure) ),
//            PlayerActionType.FOUL to TeamActionDescriptor(1, PlayerAction("Foul", PlayerActionType.FOUL, DummyProcedure) ),
            PlayerActionType.PASS to TeamActionDescriptor(0, PlayerAction("Pass", PlayerActionType.PASS, DummyProcedure) ),
            PlayerActionType.HAND_OFF to TeamActionDescriptor(0,  PlayerAction("Hand-Off", PlayerActionType.HAND_OFF, DummyProcedure) ),
            PlayerActionType.BLOCK to TeamActionDescriptor(0, PlayerAction("Block", PlayerActionType.BLOCK, DummyProcedure)),
            PlayerActionType.BLITZ to TeamActionDescriptor(0,  PlayerAction("Blitz", PlayerActionType.BLITZ, DummyProcedure) ),
            PlayerActionType.FOUL to TeamActionDescriptor(0, PlayerAction("Foul", PlayerActionType.FOUL, DummyProcedure) ),
        )
    }

    override fun get(type: PlayerActionType): TeamActionDescriptor {
        return actions[type] ?: INVALID_GAME_STATE("Actions this type are not configured here: $type")
    }

    override val move: TeamActionDescriptor = get(PlayerActionType.MOVE)
    override val pass: TeamActionDescriptor = get(PlayerActionType.PASS)
    override val handOff: TeamActionDescriptor = get(PlayerActionType.HAND_OFF)
    override val block: TeamActionDescriptor = get(PlayerActionType.BLOCK)
    override val blitz: TeamActionDescriptor = get(PlayerActionType.BLITZ)
    override val foul: TeamActionDescriptor = get(PlayerActionType.FOUL)
}
