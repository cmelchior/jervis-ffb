package dk.ilios.jervis.rules

import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.procedures.actions.block.BlockAction
import dk.ilios.jervis.procedures.DummyProcedure
import dk.ilios.jervis.procedures.actions.move.MoveAction
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PlayerActionSerializer : KSerializer<PlayerAction> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("PlayerAction") {
            val stringDescriptor = String.serializer().descriptor
            val boolDescriptor = Boolean.serializer().descriptor
            element("name", stringDescriptor)
            element("type", boolDescriptor)
            element("procedure", stringDescriptor)
            element("compulsory", boolDescriptor)
            element("isSpecial", boolDescriptor)
        }

    override fun serialize(
        encoder: Encoder,
        value: PlayerAction,
    ) {
        val compositeEncoder = encoder.beginStructure(descriptor)
        compositeEncoder.encodeStringElement(descriptor, 0, value.name)
        compositeEncoder.encodeSerializableElement(descriptor, 1, PlayerActionType.serializer(), value.type)
        compositeEncoder.encodeStringElement(descriptor, 2, value.procedure::class.qualifiedName.toString())
        compositeEncoder.encodeBooleanElement(descriptor, 3, value.compulsory)
        compositeEncoder.encodeBooleanElement(descriptor, 4, value.isSpecial)
        compositeEncoder.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): PlayerAction {
        val compositeDecoder = decoder.beginStructure(descriptor)
        var name = ""
        var type: PlayerActionType? = null
        var procedure: Procedure? = null
        var compulsory = false
        var isSpecial = false

        loop@ while (true) {
            when (val index = compositeDecoder.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                0 -> name = compositeDecoder.decodeStringElement(descriptor, index)
                1 -> type = compositeDecoder.decodeSerializableElement(descriptor, index, PlayerActionType.serializer())
                2 -> procedure = loadProcedure(compositeDecoder.decodeStringElement(descriptor, index)) // Convert String back to Procedure
                3 -> compulsory = compositeDecoder.decodeBooleanElement(descriptor, index)
                4 -> isSpecial = compositeDecoder.decodeBooleanElement(descriptor, index)
                else -> throw SerializationException("Unknown index $index")
            }
        }
        compositeDecoder.endStructure(descriptor)
        return PlayerAction(name, type!!, procedure!!, compulsory, isSpecial)
    }

    private fun loadProcedure(procedureFQN: String): Procedure {
        TODO()
//        return Class.forName(procedureFQN).kotlin.objectInstance
    }
}

/**
 * Wrapper representing a players action
 */
@Serializable()
data class PlayerAction(
    val name: String,
    val type: PlayerActionType,
    val procedure: Procedure,
    val compulsory: Boolean = false, // If true, players must choose this action
    val isSpecial: Boolean = false,
)

/**
 * Enumerate the
 */
@Serializable
enum class PlayerActionType {
    MOVE,
    PASS,
    HAND_OFF,
    BLOCK,
    BLITZ,
    FOUL,
    SPECIAL,
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
    val action: PlayerAction,
)

/**
 * Define the standard set of actions that are available in the rules.
 * TODO What if these are modified by skills, events, cards or otherwise?
 */
class BB2020TeamActions : TeamActions() {
    private val actions: Map<PlayerActionType, TeamActionDescriptor>

    init {
        actions =
            mapOf(
                PlayerActionType.MOVE to TeamActionDescriptor(
                    availablePrTurn = Int.MAX_VALUE,
                    action = PlayerAction("Move", PlayerActionType.MOVE, MoveAction)
                ),
                PlayerActionType.PASS to TeamActionDescriptor(
                    availablePrTurn = 1,
                    action = PlayerAction("Pass", PlayerActionType.PASS, DummyProcedure)
                ),
                PlayerActionType.HAND_OFF to TeamActionDescriptor(
                    availablePrTurn = 1,
                    action = PlayerAction("Hand-Off", PlayerActionType.HAND_OFF, DummyProcedure)
                ),
                PlayerActionType.BLOCK to TeamActionDescriptor(
                    availablePrTurn = Int.MAX_VALUE,
                    action = PlayerAction("Block", PlayerActionType.BLOCK, BlockAction)
                ),
                PlayerActionType.BLITZ to TeamActionDescriptor(
                    availablePrTurn = 1,
                    action = PlayerAction("Blitz", PlayerActionType.BLITZ, DummyProcedure)
                ),
                PlayerActionType.FOUL to TeamActionDescriptor(
                    availablePrTurn = 1,
                    action = PlayerAction("Foul", PlayerActionType.FOUL, DummyProcedure)
                ),
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
