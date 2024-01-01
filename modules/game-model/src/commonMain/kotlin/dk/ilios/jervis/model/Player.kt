package dk.ilios.jervis.model

import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.rules.skills.Skill
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.jvm.JvmInline
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

abstract class Observable<T> {
    private val _state = MutableSharedFlow<T>(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    protected val observeState: SharedFlow<T> = _state
    protected fun <P> observable(initialValue: P, onChange: ((oldValue: P, newValue: P) -> Unit)? = null): ReadWriteProperty<Any?, P> {
        return object: ObservableProperty<P>(initialValue) {
            override fun afterChange(property: KProperty<*>, oldValue: P, newValue: P) {
                _state.tryEmit(this@Observable as T)
                onChange?.let {
                    onChange(oldValue, newValue)
                }
            }
        }
    }
    public fun notifyUpdate() {
        _state.tryEmit(this as T)
    }
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

class Player: Observable<Player>() {
    lateinit var team: Team
    var location: Location by observable(DogOut) { old, new ->
        if ((old == DogOut && new != DogOut) || old != DogOut && new == DogOut) {
            team.notifyDogoutChange()
        }
    }
    var state: PlayerState by observable(PlayerState.STANDING)
    var isActive: Boolean by observable(false)
    var available: Availability by observable(Availability.AVAILABLE)
    var stunnedThisTurn: Boolean? = null
    var hasTackleZones: Boolean = true
    var name: String by observable("")
    var number: PlayerNo = PlayerNo(0)
    var position: Position = HumanTeam.positions.first()
    var baseMove: Int = 0
    var baseStrenght: Int = 0
    var baseAgility: Int = 0
    var basePassing: Int = 0
    var baseArmorValue: Int = 0
    val skills = mutableListOf<Skill>()
    val move: Int
        get() {
            return baseMove
        }
    val strength: Int
        get() {
            return baseStrenght
        }
    val agility: Int
        get() {
            return baseAgility
        }
    val passing: Int
        get() {
            return basePassing
        }
    val armorValue: Int
        get() {
            return baseArmorValue
        }
    val ball: Ball?
        get() = if (team.game.ball.state == BallState.CARRIED && team.game.ball.location == location) {
            team.game.ball
        } else {
            null
        }

    fun hasBall(): Boolean = (ball != null)

    // Expose updats to this class as Flow
    val observePlayer = observeState

    override fun toString(): String {
        return "Player(name='$name', number=$number, position=$position)"
    }
}