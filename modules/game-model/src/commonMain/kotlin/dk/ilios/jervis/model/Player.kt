package dk.ilios.jervis.model

import dk.ilios.jervis.model.locations.DogOut
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.locations.GiantLocation
import dk.ilios.jervis.model.locations.Location
import dk.ilios.jervis.model.modifiers.StatModifier
import dk.ilios.jervis.model.modifiers.TemporaryEffect
import dk.ilios.jervis.model.modifiers.TemporaryEffectType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.skills.Skill
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import dk.ilios.jervis.utils.sum
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    SERIOUS_HURT,
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
    return this.team.game.homeTeam == this.team
}

fun Player.isOnAwayTeam(): Boolean {
    return this.team.game.awayTeam == this.team
}

enum class Availability {
    AVAILABLE, // Are available to be activated in this turn
    IS_ACTIVE, // Are currently active
    HAS_ACTIVATED, // Has already activated this turn
    UNAVAILABLE, // Unavailable for this turn
}

@JvmInline
@Serializable
value class PlayerId(val value: String)

@Serializable
class Player(
    val id: PlayerId,
    val position: Position,
    // Inject Ranges to avoid needing a Rules reference when accessing characteristics
    // Slightly annoying, but I am not sure if there is a better way?
    @Serializable(with = IntRangeSerializer::class)
    private val moveRange: IntRange,
    @Serializable(with = IntRangeSerializer::class)
    val strengthRange: IntRange,
    @Serializable(with = IntRangeSerializer::class)
    val agilityRange: IntRange,
    @Serializable(with = IntRangeSerializer::class)
    val passingRange: IntRange,
    @Serializable(with = IntRangeSerializer::class)
    val armourRange: IntRange,
) : Observable<Player>() {
    @Transient
    lateinit var team: Team
    var location: Location = DogOut
        set(value) {
            val old = location
            field = value
            if ((old == DogOut && value != DogOut) || old != DogOut && value == DogOut) {
                team.notifyDogoutChange()
            }
        }
    // Shortcut for getting a players coordinates. Only works for players currently on the the field
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
    var baseMove: Int = 0
    val moveModifiers = mutableListOf<StatModifier>()
    var movesLeft: Int = 0
    var rushesLeft: Int = 0
    var baseStrenght: Int = 0
    val strengthModifiers = mutableListOf<StatModifier>()
    var baseAgility: Int = 0
    val agilityModifiers = mutableListOf<StatModifier>()
    var basePassing: Int? = 0
    val passingModifiers = mutableListOf<StatModifier>()
    var baseArmorValue: Int = 0
    val armourModifiers = mutableListOf<StatModifier>()
    // Some effects are hard to put into other buckets, like a player that failed a Blood Lust roll
    // or a player that was added to the pitch through Spot The Sneak. In these cases, we might want
    // to mark the player somehow. This is done through a TemporaryEffect
    val temporaryEffects = mutableListOf<TemporaryEffect>()
    val extraSkills = mutableListOf<Skill>()
    var positionSkills = position.skills.map { it.createSkill() }.toMutableList()
    val skills: List<Skill>
        get() = extraSkills + positionSkills // TODO This probably result in _a lot_ of copying. Find a way to optimize this
    val move: Int
        get() = (baseMove + moveModifiers.sum()).coerceIn(moveRange)
    val strength: Int
        get() = (baseStrenght + strengthModifiers.sum()).coerceIn(strengthRange)
    val agility: Int
        get() = (baseAgility + agilityModifiers.sum()).coerceIn(agilityRange)
    val passing: Int?
        get() {
            // How to handle modifiers to `null`. I believe the start is then treated as 7+, but find reference
            return if (basePassing == null && passingModifiers.isNotEmpty()) {
                (7 + passingModifiers.sum()).coerceIn(passingRange)
            } else if (basePassing != null && passingModifiers.isNotEmpty()) {
                (basePassing!! + passingModifiers.sum()).coerceIn(passingRange)
            } else {
                basePassing
            }
        }
    val armorValue: Int
        get() = (baseArmorValue + armourModifiers.sum()).coerceIn(armourRange)
    var nigglingInjuries: Int = 0
    var missNextGame: Boolean = false
    var starPlayerPoints: Int = 0
    var level: PlayerLevel = PlayerLevel.ROOKIE
    val ball: Ball?
        get() = team.game.balls.firstOrNull { it.carriedBy == this }

    fun addSkill(skill: Skill) {
        extraSkills.add(skill)
    }

    fun removeSkill(skill: Skill) {
        if (!extraSkills.remove(skill)) {
            INVALID_GAME_STATE("Could not remove skill: ${skill.name}")
        }
    }


    fun hasBall(): Boolean = (ball != null)

    // Expose updates to this class as Flow
    @Transient
    val observePlayer = observeState

    override fun toString(): String {
        return "Player(id='${id.value}', name='$name', number=$number, position=$position)"
    }

    inline fun <reified T: Skill> getSkill(): T {
        return skills.filterIsInstance<T>().firstOrNull() ?: INVALID_GAME_STATE("Player does not have the skill ${T::class.simpleName}")
    }

    inline fun <reified T: Skill> getSkillOrNull(): T? {
        return skills.filterIsInstance<T>().firstOrNull()
    }

    /**
     * Returns `true` if the player is still standing on the field
     */
    @Deprecated("Move this Rules instead")
    fun isStanding(rules: Rules): Boolean {
        return rules.isStanding(this)
    }

    fun addStatModifier(modifier: StatModifier) {
        when(modifier.type) {
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
        val success = when(modifier.type) {
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

inline fun <reified T:Skill> Player.hasSkill(): Boolean {
    return this.skills.filterIsInstance<T>().isNotEmpty()
}

// This method assumes the player is on the field
inline fun <reified T:Skill> Player.isSkillAvailable(): Boolean {
    return getSkillOrNull<T>()?.let { skill ->
        if (!hasTackleZones && !skill.workWithoutTackleZones) {
            return@let false
        }
        if ((state == PlayerState.PRONE || state == PlayerState.STUNNED || state == PlayerState.STUNNED_OWN_TURN) && !skill.workWhenProne) {
            return@let false
        }
//        if (state != PlayerState.STANDING && state != PlayerState.PRONE && state == PlayerState.STUNNED) {
//            return@let false
//        }
        return !skill.used
    } ?: false
}
