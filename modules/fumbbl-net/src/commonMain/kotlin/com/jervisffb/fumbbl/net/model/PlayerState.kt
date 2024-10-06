package com.jervisffb.fumbbl.net.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PlayerStateSerializer : KSerializer<PlayerState> {
    private val decoderSerializer: KSerializer<Int> = Int.serializer()
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PlayerState", PrimitiveKind.INT)

    override fun serialize(
        encoder: Encoder,
        value: PlayerState,
    ) {
        encoder.encodeInt(value.id)
    }

    override fun deserialize(decoder: Decoder): PlayerState {
        val value = decoder.decodeSerializableValue(decoderSerializer)
        return PlayerState(value)
    }
}

@Serializable(with = PlayerStateSerializer::class)
class PlayerState(val id: Int) {
    val base: Int
        get() = id and 0xFF

    fun changeBase(pBase: Int): PlayerState {
        val baseMask = if ((pBase > 0 && pBase < _BASE_MASK.size)) _BASE_MASK[pBase] else 0
        return PlayerState(id and baseMask or pBase)
    }

    val isSelectedBlitzTarget: Boolean
        get() = hasBit(_BIT_SELECTED_BLITZ_TARGET)

    fun addSelectedBlitzTarget(): PlayerState {
        if (!hasBit(_BIT_SELECTED_BLITZ_TARGET)) {
            return changeBit(_BIT_SELECTED_BLITZ_TARGET, true)
        }
        return this
    }

    private fun removeSelectedBlitzTarget(): PlayerState {
        if (hasBit(_BIT_SELECTED_BLITZ_TARGET)) {
            return changeBit(_BIT_SELECTED_BLITZ_TARGET, false)
        }
        return this
    }

    val isActive: Boolean
        get() = hasBit(_BIT_ACTIVE)

    fun changeActive(pActive: Boolean): PlayerState {
        return changeBit(_BIT_ACTIVE, pActive)
    }

    val isConfused: Boolean
        get() = hasBit(_BIT_CONFUSED)

    fun changeConfused(pConfused: Boolean): PlayerState {
        return changeBit(_BIT_CONFUSED, pConfused)
    }

    val isRooted: Boolean
        get() = hasBit(_BIT_ROOTED)

    fun changeRooted(pRooted: Boolean): PlayerState {
        return changeBit(_BIT_ROOTED, pRooted)
    }

    val isHypnotized: Boolean
        get() = hasBit(_BIT_HYPNOTIZED)

    fun changeHypnotized(pHypnotized: Boolean): PlayerState {
        return changeBit(_BIT_HYPNOTIZED, pHypnotized)
    }

    fun recoverTacklezones(): PlayerState {
        return changeHypnotized(false).changeConfused(false)
    }

    val isSelectedStabTarget: Boolean
        get() = hasBit(_BIT_SELECTED_STAB_TARGET)

    fun changeSelectedStabTarget(isSelectedStabTarget: Boolean): PlayerState {
        return changeBit(_BIT_SELECTED_STAB_TARGET, isSelectedStabTarget)
    }

    val isSelectedBlockTarget: Boolean
        get() = hasBit(_BIT_SELECTED_BLOCK_TARGET)

    fun changeSelectedBlockTarget(isSelectedBlockTarget: Boolean): PlayerState {
        return changeBit(_BIT_SELECTED_BLOCK_TARGET, isSelectedBlockTarget)
    }

    val isSelectedGazeTarget: Boolean
        get() = hasBit(_BIT_SELECTED_GAZE_TARGET)

    fun changeSelectedGazeTarget(isSelectedBlockTarget: Boolean): PlayerState {
        return changeBit(_BIT_SELECTED_GAZE_TARGET, isSelectedBlockTarget)
    }

    fun removeAllTargetSelections(): PlayerState {
        return changeSelectedGazeTarget(false).removeSelectedBlitzTarget()
    }

    fun hasUsedPro(): Boolean {
        return hasBit(_BIT_USED_PRO)
    }

    fun changeUsedPro(pUsedPro: Boolean): PlayerState {
        return changeBit(_BIT_USED_PRO, pUsedPro)
    }

    val isCasualty: Boolean
        get() = (BADLY_HURT == base || SERIOUS_INJURY == base || isKilled)

    val isKilled: Boolean
        get() = (RIP == base)

    fun canBeSetUpNextDrive(): Boolean {
        return (STANDING == base || MOVING == base || PRONE == base || STUNNED == base || RESERVE == base || FALLING == base || HIT_ON_GROUND == base || BLOCKED == base)
    }

    fun canBeMovedDuringSetup(): Boolean {
        return (STANDING == base || RESERVE == base)
    }

    fun hasTacklezones(): Boolean {
        return ((STANDING == base || MOVING == base || BLOCKED == base) && !isConfused && !isHypnotized)
    }

    val isProneOrStunned: Boolean
        get() = (PRONE == base || STUNNED == base)

    val isStunned: Boolean
        get() = (STUNNED == base)

    val isAbleToMove: Boolean
        get() = ((STANDING == base || MOVING == base || PRONE == base) && this.isActive && !this.isRooted)

    fun canBeBlocked(): Boolean {
        return (STANDING == base || MOVING == base)
    }

    fun canBeFouled(): Boolean {
        return (PRONE == base || STUNNED == base)
    }

    private fun changeBit(
        pMask: Int,
        pBit: Boolean,
    ): PlayerState {
        if (pBit) {
            return PlayerState(id or pMask)
        }
        return PlayerState(id and (0xFFFFF xor pMask))
    }

    private fun hasBit(pMask: Int): Boolean {
        return ((id and pMask) > 0)
    }

    val description: String?
        get() {
            return when (base) {
                0 -> "is unknown"
                1 -> "is standing"
                2 -> "is moving"
                3 -> "is prone"
                4 -> "has been stunned"
                5 -> "has been knocked out"
                6 -> "has been badly hurt"
                7 -> "has been seriously injured"
                8 -> "has been killed"
                9 -> "is in reserve"
                10 -> "is missing the game"
                11 -> "is about to fall down"
                12 -> "is being blocked"
                13 -> "is banned from the game"
                14 -> "is exhausted"
                15 -> "is being dragged"
                16 -> "has been picked up"
                17 -> "was hit while on the ground"
                20 -> "can not be set up"
                21 -> "is in the air"
                else -> null
            }
        }

    val buttonText: String?
        get() {
            return when (base) {
                0 -> "Unknown"
                1 -> "Standing"
                2 -> "Moving"
                3 -> "Prone"
                4 -> "has been stunned"
                5 -> "Knocked Out"
                6 -> "Badly Hurt"
                7 -> "Serious Injury"
                8 -> "Killed"
                9 -> "Reserve"
                10 -> "Missing"
                11 -> "Falling Down"
                12 -> "Blocked"
                13 -> "Banned"
                14 -> "Exhausted"
                15 -> "Being Dragged"
                16 -> "Picked Up"
                17 -> "Hit on the ground"
                20 -> "Can't be set up"
                21 -> "In the air"
                else -> null
            }
        }

    override fun toString(): String = id.toString()

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = 31 * result + this.id
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        // if (javaClass != obj.javaClass) return false
        val other = obj as PlayerState
        return (this.id == other.id)
    }

    @Suppress("standard:property-naming")
    companion object {
        const val UNKNOWN: Int = 0
        const val STANDING: Int = 1
        const val MOVING: Int = 2
        const val PRONE: Int = 3
        const val STUNNED: Int = 4
        const val KNOCKED_OUT: Int = 5
        const val BADLY_HURT: Int = 6
        const val SERIOUS_INJURY: Int = 7
        const val RIP: Int = 8
        const val RESERVE: Int = 9
        const val MISSING: Int = 10
        const val FALLING: Int = 11
        const val BLOCKED: Int = 12
        const val BANNED: Int = 13
        const val EXHAUSTED: Int = 14
        const val BEING_DRAGGED: Int = 15
        const val PICKED_UP: Int = 16
        const val HIT_ON_GROUND: Int = 17
        const val HIT_BY_FIREBALL: Int = 17
        const val HIT_BY_LIGHTNING: Int = 18
        const val HIT_BY_BOMB: Int = 19
        const val SETUP_PREVENTED: Int = 20
        const val IN_THE_AIR: Int = 21
        private const val _BIT_ACTIVE = 256
        private const val _BIT_CONFUSED = 512
        private const val _BIT_ROOTED = 1024
        private const val _BIT_HYPNOTIZED = 2048
        private const val _BIT_SELECTED_STAB_TARGET = 4096
        private const val _BIT_USED_PRO = 8192
        private const val _BIT_SELECTED_BLITZ_TARGET = 16384
        private const val _BIT_SELECTED_BLOCK_TARGET = 32768
        private const val _BIT_SELECTED_GAZE_TARGET = 65536
        private val _BASE_MASK =
            intArrayOf(
                0,
                1048320,
                1048320,
                1048320,
                1048320,
                0,
                0,
                0,
                0,
                0,
                0,
                1048320,
                1048320,
                0,
                1048320,
                1048320,
                1048320,
                1048320,
                1048320,
                1048320,
            )
    }
}
