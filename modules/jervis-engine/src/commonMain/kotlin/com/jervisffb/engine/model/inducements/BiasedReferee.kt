package com.jervisffb.engine.model.inducements

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import kotlinx.serialization.Serializable

enum class BiasedRefereeType {
    STANDARD,
    RANULF_RED_HOKULI,
    THORON_KORENSSON,
    JORM_THE_OGRE,
    THE_THRUNDLEFOOT_TRIPLETS
}

interface BiasedReferee {
    val type: BiasedRefereeType
    val name: String
    val named: Boolean
    val specialRules: List<PlayerSpecialRule>
    val specialAbilities: List<BiasedRefereeAbility>
    val price: Int
    fun isAvailable(team: Team): Boolean {
        return true
    }
}

/**
 * Standard Biased Referee
 * See page 95 in the rulebook.
 */
@Serializable
class StandardBiasedReferee(
    override val price: Int = 120_000 // 80_000
): BiasedReferee {
    override val type: BiasedRefereeType = BiasedRefereeType.STANDARD
    override val name: String = "Biased Referee"
    override val named: Boolean = false
    override val specialRules = listOf(
        PlayerSpecialRule.I_DID_NOT_SEE_A_THING,
        PlayerSpecialRule.CLOSE_SCRUTINY,
    )
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

/**
 * Interface describing infamous coach abilities that are optional
 * to use
 */
interface BiasedRefereeAbility: InducementEffect {
    // Some cards have special conditions on their triggers
    // Call this method to ensure that the card truly is available.
    // It assumes that the trigger Timing condition has been met
    fun isApplicable(state: Game, rules: Rules): Boolean {
        return true
    }
}
