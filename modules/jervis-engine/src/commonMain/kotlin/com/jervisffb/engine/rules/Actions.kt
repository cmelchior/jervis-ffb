package com.jervisffb.engine.rules

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.MultipleBlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.ProjectileVomitAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.StabAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FoulAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.handoff.HandOffAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassAction
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.rules.bb2020.procedures.actions.handoff.ThrowTeamMateAction
import kotlinx.serialization.Serializable

//
//object PlayerActionSerializer : KSerializer<PlayerAction> {
//    override val descriptor: SerialDescriptor =
//        buildClassSerialDescriptor("PlayerAction") {
//            val stringDescriptor = String.serializer().descriptor
//            val boolDescriptor = Boolean.serializer().descriptor
//            element("name", stringDescriptor)
//            element("type", boolDescriptor)
//            element("procedure", stringDescriptor)
//            element("compulsory", boolDescriptor)
//            element("isSpecial", boolDescriptor)
//        }
//
//    override fun serialize(
//        encoder: Encoder,
//        value: PlayerAction,
//    ) {
//        val compositeEncoder = encoder.beginStructure(descriptor)
//        compositeEncoder.encodeStringElement(descriptor, 0, value.name)
//        compositeEncoder.encodeSerializableElement(descriptor, 1, PlayerActionType.serializer(), value.type)
//        compositeEncoder.encodeStringElement(descriptor, 2, value.procedure::class.qualifiedName.toString())
//        compositeEncoder.encodeBooleanElement(descriptor, 3, value.compulsory)
//        compositeEncoder.encodeBooleanElement(descriptor, 4, value.isSpecial)
//        compositeEncoder.endStructure(descriptor)
//    }
//
//    override fun deserialize(decoder: Decoder): PlayerAction {
//        val compositeDecoder = decoder.beginStructure(descriptor)
//        var name = ""
//        var type: PlayerActionType? = null
//        var worksDuringBlitz: Boolean = false
//        var procedure: Procedure? = null
//        var compulsory = false
//        var isSpecial = false
//        loop@ while (true) {
//            when (val index = compositeDecoder.decodeElementIndex(descriptor)) {
//                CompositeDecoder.DECODE_DONE -> break@loop
//                0 -> name = compositeDecoder.decodeStringElement(descriptor, index)
//                1 -> type = compositeDecoder.decodeSerializableElement(descriptor, index, PlayerActionType.serializer())
//                2 -> procedure = loadProcedure(compositeDecoder.decodeStringElement(descriptor, index)) // Convert String back to Procedure
//                3 -> compulsory = compositeDecoder.decodeBooleanElement(descriptor, index)
//                4 -> isSpecial = compositeDecoder.decodeBooleanElement(descriptor, index)
//                else -> throw SerializationException("Unknown index $index")
//            }
//        }
//        compositeDecoder.endStructure(descriptor)
//
//
//
//
//
//        return PlayerAction(name, type!!, procedure!!, compulsory, isSpecial)
//    }
//
//    private fun loadProcedure(procedureFQN: String): Procedure {
//        TODO()
////        return Class.forName(procedureFQN).kotlin.objectInstance
//    }
//}



/**
 * Wrapper representing a player action. This can either be a normal or special action.
 */
@Serializable()
data class PlayerAction(
    val name: String,
    val type: ActionType,
    val countsAs: PlayerStandardActionType?,
    // How many times (by different players) can this action be used pr team turn
    val availablePrTurn: Int,
    // if type == BLOCK, this decides if this action also works during the blitz
    val worksDuringBlitz: Boolean,
    val procedure: Procedure,
    val compulsory: Boolean, // If true, players must choose this action
) {
    val isSpecial = (type == PlayerStandardActionType.SPECIAL) || (countsAs == PlayerStandardActionType.SPECIAL)
}

@Serializable
sealed interface ActionType

/**
 * Enumerate the different "main" action types available in Blood Bowl.
 * The special category should only be used for skills that are completely
 * their own, and doesn't replace a skill in on of the other categories.
 */
@Serializable
enum class PlayerStandardActionType: ActionType {
    MOVE,
    PASS,
    HAND_OFF,
    THROW_TEAM_MATE,
    BLOCK,
    BLITZ,
    FOUL,
    SPECIAL
}

// All special actions are tied to skills

/**
 * Enumerate the different special action types possible in Blood Bowl.
 */
@Serializable
enum class PlayerSpecialActionType: ActionType {
    BALL_AND_CHAIN,
    BOMBARDIER,
    BREATHE_FIRE,
    CHAINSAW,
    HYPNOTIC_GAZE,
    KICK_TEAM_MATE,
    MULTIPLE_BLOCK,
    PROJECTILE_VOMIT,
    STAB,
}

@Serializable
enum class BlockType {
    BREATHE_FIRE,
    CHAINSAW,
    MULTIPLE_BLOCK,
    PROJECTILE_VOMIT,
    STAB,
    STANDARD,
    // Multiple Block
    // Ball & Chain (replace all other actions)
    // Bombardier (Its own action, 1 pr. team turn)
    // Chainsaw (Replace block action or block part of Blitz, 1. pr activation)
    // Kick Team-mate (its own action, 1 pr. team turn)
    // Projectile Vomit (Replace block action or block part of Blitz, 1. pr activation)
    // Stab (Replace block action or block part of Blitz, no limit)
    // Hypnotic Gaze (Its own action)
    // Breathe Fire (replace block or block part of blitz, once pr. activation)
}


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

/**
 * Define the standard set of actions that are available in the rules.
 * TODO What if these are modified by skills, events, cards or otherwise?
 */
class BB2020TeamActions : TeamActions() {

    private val actions: Map<PlayerStandardActionType, PlayerAction> = mapOf(
        PlayerStandardActionType.MOVE to PlayerAction(
            name = "Move",
            type = PlayerStandardActionType.MOVE,
            countsAs = null,
            availablePrTurn = Int.MAX_VALUE,
            worksDuringBlitz = false,
            procedure = MoveAction,
            compulsory = false,
        ),

        PlayerStandardActionType.PASS to PlayerAction(
            name = "Pass",
            type = PlayerStandardActionType.PASS,
            countsAs = null,
            availablePrTurn = 1,
            worksDuringBlitz = false,
            procedure = PassAction,
            compulsory = false,
        ),

        PlayerStandardActionType.HAND_OFF to PlayerAction(
            name = "Hand-off",
            type = PlayerStandardActionType.HAND_OFF,
            countsAs = null,
            availablePrTurn = 1,
            worksDuringBlitz = false,
            procedure = HandOffAction,
            compulsory = false,
        ),

        PlayerStandardActionType.THROW_TEAM_MATE to PlayerAction(
            name = "Throw Team-mate",
            type = PlayerStandardActionType.THROW_TEAM_MATE,
            countsAs = null,
            availablePrTurn = 1,
            worksDuringBlitz = false,
            procedure = ThrowTeamMateAction,
            compulsory = false,
        ),

        PlayerStandardActionType.BLOCK to PlayerAction(
            name = "Block",
            type = PlayerStandardActionType.BLOCK,
            countsAs = null,
            availablePrTurn = Int.MAX_VALUE,
            worksDuringBlitz = true,
            procedure = BlockAction,
            compulsory = false,
        ),

        PlayerStandardActionType.BLITZ to PlayerAction(
            name = "Blitz",
            type = PlayerStandardActionType.BLITZ,
            countsAs = null,
            availablePrTurn = 1,
            worksDuringBlitz = true,
            procedure = BlitzAction,
            compulsory = false,
        ),

        PlayerStandardActionType.FOUL to PlayerAction(
            name = "Foul",
            type = PlayerStandardActionType.FOUL,
            countsAs = null,
            availablePrTurn = 1,
            worksDuringBlitz = false,
            procedure = FoulAction,
            compulsory = false,
        ),
    )

    override val specialActions: Set<PlayerAction> = buildSet {
        PlayerSpecialActionType.entries.forEach {
            val action = when (it) {
//                PlayerSpecialActionType.BALL_AND_CHAIN -> TODO()
//                PlayerSpecialActionType.BOMBARDIER -> TODO()
//                PlayerSpecialActionType.BREATHE_FIRE -> TODO()
//                PlayerSpecialActionType.CHAINSAW -> TODO()
//                PlayerSpecialActionType.HYPNOTIC_GAZE -> TODO()
//                PlayerSpecialActionType.KICK_TEAM_MATE -> TODO()
                PlayerSpecialActionType.MULTIPLE_BLOCK -> {
                    PlayerAction(
                        name = "Multiple Block",
                        type = PlayerSpecialActionType.MULTIPLE_BLOCK,
                        countsAs = PlayerStandardActionType.BLOCK,
                        availablePrTurn = Int.MAX_VALUE,
                        procedure = MultipleBlockAction,
                        worksDuringBlitz = false,
                        compulsory = false,
                    )
                }
                PlayerSpecialActionType.PROJECTILE_VOMIT -> {
                    PlayerAction(
                        name = "Projectile Vomit",
                        type = PlayerSpecialActionType.PROJECTILE_VOMIT,
                        countsAs = PlayerStandardActionType.BLOCK,
                        availablePrTurn = Int.MAX_VALUE,
                        procedure = ProjectileVomitAction,
                        worksDuringBlitz = true,
                        compulsory = false,
                    )
                }
                PlayerSpecialActionType.STAB -> {
                    PlayerAction(
                        name = "Stab",
                        type = PlayerSpecialActionType.STAB,
                        countsAs = PlayerStandardActionType.BLOCK,
                        availablePrTurn = Int.MAX_VALUE,
                        procedure = StabAction,
                        worksDuringBlitz = true,
                        compulsory = false,
                    )
                }
                else -> null
            }
            if (action != null) add(action)
        }
    }

    override fun get(type: PlayerStandardActionType): PlayerAction {
        return actions[type] ?: INVALID_GAME_STATE("Actions this type are not configured here: $type")
    }
    override fun get(type: PlayerSpecialActionType): PlayerAction {
        return specialActions.firstOrNull { it.type == type } ?: INVALID_GAME_STATE("Actions this type are not configured here: $type")
    }
    override val move: PlayerAction = get(PlayerStandardActionType.MOVE)
    override val pass: PlayerAction = get(PlayerStandardActionType.PASS)
    override val handOff: PlayerAction = get(PlayerStandardActionType.HAND_OFF)
    override val block: PlayerAction = get(PlayerStandardActionType.BLOCK)
    override val blitz: PlayerAction = get(PlayerStandardActionType.BLITZ)
    override val foul: PlayerAction = get(PlayerStandardActionType.FOUL)
}
