package com.jervisffb.engine.model

import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.model.modifiers.TemporaryEffect
import com.jervisffb.engine.model.modifiers.TemporaryEffectType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.Skill
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.serialize.PlayerUiData
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlin.jvm.JvmInline

object IntRangeSerializer: KSerializer<IntRange> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = listSerialDescriptor<Int>()
    override fun deserialize(decoder: Decoder): IntRange {
        return decoder.decodeStructure(descriptor) {
            var start = 0
            var endInclusive = 0
            loop@while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> start = decodeIntElement(descriptor, index)
                    1 -> endInclusive = decodeIntElement(descriptor, index)
                    else -> throw IllegalStateException("Unexpected index: $index")
                }
            }
            start..endInclusive
        }
    }

    override fun serialize(encoder: Encoder, value: IntRange) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.first)
            encodeIntElement(descriptor, 1, value.last)
        }
    }
}



// TODO Should we split this into DogoutState and FieldState?
enum class PlayerState {
    // Dogout states
    RESERVE,
    KNOCKED_OUT,
    BADLY_HURT,
    LASTING_INJURY,
    SERIOUSLY_HURT,
    SERIOUS_INJURY,
    DEAD,
    FAINTED, // From Sweltering Heat
    BANNED,

    // Intermediate states
    FALLEN_OVER,
    KNOCKED_DOWN,

    // Field states
    STANDING,
    PRONE,
    STUNNED,
    STUNNED_OWN_TURN,

//    MOVING,
//    UNKNOWN,
//    MISSING,
//    FALLING,
//    BLOCKED,
//    EXHAUSTED,
//    BEING_DRAGGED,
//    PICKED_UP,
//    HIT_ON_GROUND,
//    HIT_BY_FIREBALL,
//    HIT_BY_LIGHTNING,
//    HIT_BY_BOMB,
//    SETUP_PREVENTED
}

@Serializable
@JvmInline
value class PlayerNo(val value: Int) : Comparable<PlayerNo> {
    override fun compareTo(other: PlayerNo): Int {
        return when {
            (value == other.value) -> 0
            (value < other.value) -> -1
            else -> 1
        }
    }

    override fun toString(): String = value.toString()
}

fun Player.isOnHomeTeam(): Boolean {
    return this.team.isHomeTeam()
}

fun Player.isOnAwayTeam(): Boolean {
    return this.team.isAwayTeam()
}

/**
 * Describes the state of a players "availability" during their teams turn.
 * Players should be marked [Availability.AVAILABLE] at the start of a team turn
 * and then moved to other states as appropriate. E.g. they might move directly
 * to [Availability.UNAVAILABLE] if they are stunned.
 */
enum class Availability {
    AVAILABLE, // Are available to be activated in this turn
    IS_ACTIVE, // Are currently active
    HAS_ACTIVATED, // Has already activated this turn
    UNAVAILABLE, // Unavailable for this turn, e.g. because they are stunned.
}

class Player(
    rules: Rules,
    val id: PlayerId,
    val position: Position,
    val icon: PlayerUiData? = null,
    val type: PlayerType
) {
    lateinit var team: Team
    var location: Location = DogOut

    // Shortcut for getting a players coordinates. Only works for players currently on the field
    // taking up a single square.
    val coordinates: FieldCoordinate
        get() {
            return when (val playerLocation = location) {
                DogOut -> INVALID_GAME_STATE("Cannot ask for coordinates when player is in the DogOut")
                is FieldCoordinate -> playerLocation
                is GiantLocation -> INVALID_GAME_STATE("Cannot ask for coordinates for a giant player")
            }
        }
    var facing: PlayerFacing = PlayerFacing.UNKNOWN
    var state: PlayerState = PlayerState.RESERVE
    val isActive: Boolean get() = (team.game.activePlayer == this)
    var available: Availability = Availability.AVAILABLE
    var stunnedThisTurn: Boolean? = null
    var hasTackleZones: Boolean = true
    var isStalling: Boolean = false
    var name: String = ""
    var number: PlayerNo = PlayerNo(0)
    // When updating `baseMove` and `moveModifiers`, `move` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    var baseMove: Int = 0
    val moveModifiers = mutableListOf<StatModifier>()
    var move: Int = 0
    var movesLeft: Int = 0
    var rushesLeft: Int = 0
    // When updating `baseStrength` and `strengthModifiers`, `strength` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    var baseStrength: Int = 0
    val strengthModifiers = mutableListOf<StatModifier>()
    var strength: Int = 0
    // When updating `baseAgility` and `agilityModifiers`, `agility` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    var baseAgility: Int = 0
    val agilityModifiers = mutableListOf<StatModifier>()
    var agility: Int = 0
    // When updating `basePassing` and `passingModifiers`, `passing` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    var basePassing: Int? = null
    val passingModifiers = mutableListOf<StatModifier>()
    var passing: Int? = null
    // When updating `baseArmorValue` and `armourModifiers`, `armorValue` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    var baseArmorValue: Int = 0
    val armourModifiers = mutableListOf<StatModifier>()
    var armorValue: Int = 0
    // Some effects are hard to put into other buckets, like a player that failed a Blood Lust roll
    // or a player that was added to the pitch through Spot The Sneak. In these cases, we might want
    // to mark the player somehow. This is done through a TemporaryEffect
    val temporaryEffects = mutableListOf<TemporaryEffect>()
    val extraSkills = mutableListOf<Skill>()
    var positionSkills = position.skills.mapNotNull {
        // TODO For now, just ignore skills that are not supported
        if (rules.skillSettings.isSkillSupported(it.type)) {
            rules.createSkill(this, it)
        } else {
            null
        }
    }.toMutableList()
    val skills: List<Skill>
        get() = extraSkills + positionSkills // TODO This probably result in _a lot_ of copying. Find a way to optimize this

    var nigglingInjuries: Int = 0
    var missNextGame: Boolean = false
    var starPlayerPoints: Int = 0
    var level: PlayerLevel = PlayerLevel.ROOKIE
    var cost: Int = 0


    val ball: Ball?
        get() = team.game.balls.firstOrNull { it.carriedBy == this }

    // Warning: This method should only be used for skills that do not have values
    fun addSkill(skill: SkillType) {
        val skill = team.game.rules.createSkill(this, skill.id(null))
        extraSkills.add(skill)
    }

    fun addSkill(skill: SkillId) {
        val skill = team.game.rules.createSkill(this, skill)
        extraSkills.add(skill)
    }

    fun addSkill(skill: Skill) {
        if (skill.player != this) {
            throw IllegalArgumentException("Skill $skill is not owned by ${this.id}: ${skill.player.id}")
        }
        extraSkills.add(skill)
    }

    fun removeSkill(skill: Skill) {
        if (!extraSkills.remove(skill)) {
            INVALID_GAME_STATE("Could not remove skill: ${skill.name}")
        }
    }


    fun hasBall(): Boolean = (ball != null)

    override fun toString(): String {
        return "Player(id='${id.value}', name='$name', number=$number, position=$position)"
    }

    inline fun <reified T: Skill> getSkill(): T {
        return skills.filterIsInstance<T>().firstOrNull() ?: INVALID_GAME_STATE("Player does not have the skill ${T::class.simpleName}")
    }

    inline fun <reified T: Skill> getSkillOrNull(): T? {
        return skills.filterIsInstance<T>().firstOrNull()
    }

    fun addStatModifier(modifier: StatModifier) {
        when (modifier.type) {
            StatModifier.Type.AV -> armourModifiers.add(modifier)
            StatModifier.Type.MA -> moveModifiers.add(modifier)
            StatModifier.Type.PA -> passingModifiers.add(modifier)
            StatModifier.Type.AG -> agilityModifiers.add(modifier)
            StatModifier.Type.ST -> strengthModifiers.add(modifier)
        }
    }

    fun removeStatModifier(modifier: StatModifier) {
        // TODO We should start search from the end of array
        // It doesn't matter much, but will ensure that the list
        // stays more consistent across Do/Undo
        val success = when (modifier.type) {
            StatModifier.Type.AV -> armourModifiers.remove(modifier)
            StatModifier.Type.MA -> moveModifiers.remove(modifier)
            StatModifier.Type.PA -> passingModifiers.remove(modifier)
            StatModifier.Type.AG -> agilityModifiers.remove(modifier)
            StatModifier.Type.ST -> strengthModifiers.remove(modifier)
        }
        if (!success) {
            INVALID_GAME_STATE("Could not remove $modifier from $name")
        }
    }

    fun getStatModifiers(): List<StatModifier> {
        return armourModifiers + moveModifiers + passingModifiers + agilityModifiers + strengthModifiers
    }

    fun hasTemporaryEffect(effect: TemporaryEffectType): Boolean {
        return temporaryEffects.any { it.type == effect }
    }
}

inline fun <reified T: Skill> Player.hasSkill(): Boolean {
    return this.skills.filterIsInstance<T>().isNotEmpty()
}

// This method assumes the player is on the field
inline fun <reified T: Skill> Player.isSkillAvailable(): Boolean {
    return getSkillOrNull<T>()?.let { skill ->
        if (!hasTackleZones && !skill.workWithoutTackleZones) {
            return@let false
        }
        // TODO Is a Stunned player considered Prone or are they completely separate?
        if ((state == PlayerState.PRONE || state == PlayerState.STUNNED || state == PlayerState.STUNNED_OWN_TURN) && !skill.workWhenProne) {
            return@let false
        }
        return !skill.used
    } ?: false
}
