package dk.ilios.jervis.model

import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.skills.Skill
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.JvmInline

// TODO Should we split this into DogoutState and FieldState?
enum class PlayerState {
    // Dogout states
    RESERVE,
    KNOCKED_OUT,
    BADLY_HURT,
    SERIOUS_INJURY,
    DEAD,
    FAINTED, // From Sweltering Heat
    BANNED,

    // Intermediate states
    KNOCKED_DOWN,

    // Field states
    STANDING,
    PRONE,
    STUNNED,

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
value class PlayerNo(val number: Int) : Comparable<PlayerNo> {
    override fun compareTo(other: PlayerNo): Int {
        return when {
            (number == other.number) -> 0
            (number < other.number) -> -1
            else -> 1
        }
    }

    override fun toString(): String = number.toString()
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
    var state: PlayerState = PlayerState.STANDING
    val isActive: Boolean get() = (team.game.activePlayer == this)
    var available: Availability = Availability.AVAILABLE
    var stunnedThisTurn: Boolean? = null
    var hasTackleZones: Boolean = true
    var name: String = ""
    var number: PlayerNo = PlayerNo(0)

    var baseMove: Int = 0
    var movesLeft: Int = 0
    var rushesLeft: Int = 0
    var baseStrenght: Int = 0
    var baseAgility: Int = 0
    var basePassing: Int? = 0
    var baseArmorValue: Int = 0
    val extraSkills = mutableListOf<Skill>()
    var positionSkills = position.skills.map { it.createSkill() }.toMutableList()
    @Transient
    val skills: List<Skill>
        get() = extraSkills + positionSkills // TODO This probably result in _a lot_ of copying. Find a way to optimize this
    val move: Int get() = baseMove
    val strength: Int get() = baseStrenght
    val agility: Int get() = baseAgility
    val passing: Int? get() = basePassing
    val armorValue: Int get() = baseArmorValue
    var nigglingInjuries: Int = 0
    var starPlayerPoints: Int = 0
    var level: PlayerLevel = PlayerLevel.ROOKIE
    val ball: Ball?
        get() =
            if (team.game.ball.carriedBy == this) {
                team.game.ball
            } else {
                null
            }

    fun addSkill(skill: Skill) {
        extraSkills.add(skill)
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
    fun isStanding(rules: Rules): Boolean {
        return state == PlayerState.STANDING && location.isOnField(rules)
    }
}

inline fun <reified T:Skill> Player.hasSkill(): Boolean {
    return this.skills.filterIsInstance<T>().isNotEmpty()
}

// This method assumes the player is on the field
inline fun <reified T:Skill> Player.isSkillAvailable(): Boolean {
    return skills.filterIsInstance<T>().firstOrNull()?.let { skill ->
        if (!hasTackleZones && !skill.workWithoutTackleZones) {
            return@let false
        }
        if ((state == PlayerState.PRONE || state == PlayerState.STUNNED) && !skill.workWhenProne) {
            return@let false
        }
        if (state != PlayerState.STANDING && state != PlayerState.PRONE && state == PlayerState.STUNNED) {
            return@let false
        }
        return !skill.used
    } ?: false
}
