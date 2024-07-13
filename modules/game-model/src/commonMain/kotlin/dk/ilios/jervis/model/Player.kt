package dk.ilios.jervis.model

import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.rules.skills.Skill
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
    RIP,

    // Field states
    STANDING,
    PRONE,
    STUNNED


//    MOVING,
//    UNKNOWN,
//    MISSING,
//    FALLING,
//    BLOCKED,
//    BANNED,
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
value class PlayerNo(val number: Int): Comparable<PlayerNo> {
    override fun compareTo(other: PlayerNo): Int {
        return when {
            (number == other.number) -> 0
            (number < other.number) -> -1
            else ->  1
        }
    }
    override fun toString(): String = number.toString()
}


fun Player.isOnHomeTeam(): Boolean {
    return this.team.game.homeTeam == this.team
}

enum class Availability {
    AVAILABLE, // Are available to be activated in this turn
    IS_ACTIVE, // Are currently active
    HAS_ACTIVATED, // Has already activated this turn
    UNAVAILABLE // Unavailable for this turn
}


@JvmInline
@Serializable
value class PlayerId(val value: String)





@Serializable
class Player(val id: PlayerId): Observable<Player>() {
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
    var state: PlayerState = PlayerState.STANDING // by observable(PlayerState.STANDING)
    var isActive: Boolean = false //by observable(false)
    var available: Availability = Availability.AVAILABLE // observable(Availability.AVAILABLE)
    var stunnedThisTurn: Boolean? = null
    var hasTackleZones: Boolean = true
    var name: String = ""
    var number: PlayerNo = PlayerNo(0)
    var position: Position = HumanTeam.positions.first()

    var baseMove: Int = 0
    var moveLeft: Int = 0
    var baseStrenght: Int = 0
    var baseAgility: Int = 0
    var basePassing: Int? = 0
    var baseArmorValue: Int = 0
    val skills = mutableListOf<Skill>()
    val move: Int get() = baseMove
    val strength: Int get() = baseStrenght
    val agility: Int get() = baseAgility
    val passing: Int? get() = basePassing
    val armorValue: Int get() = baseArmorValue
    val ball: Ball?
        get() = if (team.game.ball.state == BallState.CARRIED && team.game.ball.location == location) {
            team.game.ball
        } else {
            null
        }

    fun hasBall(): Boolean = (ball != null)

    // Expose updats to this class as Flow
    @Transient
    val observePlayer = observeState

    override fun toString(): String {
        return "Player(id='${id.value}', name='$name', number=$number, position=$position)"
    }
}