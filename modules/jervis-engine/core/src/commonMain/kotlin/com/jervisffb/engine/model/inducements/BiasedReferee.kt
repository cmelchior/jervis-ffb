package com.jervisffb.engine.model.inducements

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import io.ktor.client.request.invoke
import kotlinx.serialization.Serializable

@Serializable
enum class BiasedRefereeType(val description: String) {
    STANDARD("Biased Referee"), // BB2020
    DODGY_LEAGUE_REP("Dodgy League Rep"), // BB2025
    RANULF_RED_HOKULI("Ranulf Red Hokuli"),
    THORON_KORENSSON("Thoron Korensson"),
    JORM_THE_OGRE("Jorm the Ogre"),
    THE_THRUNDLEFOOT_TRIPLETS("The Thrundlefoot Triplets")
}

/**
 * This class represents a single Biased Referee inducement.
 */
interface BiasedReferee {
    val name: String
    val type: BiasedRefereeType
    val specialRules: List<PlayerSpecialRule>
    val specialAbilities: List<BiasedRefereeAbility>
}

/**
 * Standard Biased Referee
 * See page 95 in the BB2020 rulebook.
 */
@Serializable
class StandardBiasedReferee: BiasedReferee {
    override val type: BiasedRefereeType = BiasedRefereeType.STANDARD
    override val name: String = "Biased Referee"
    override val specialRules = listOf(
        PlayerSpecialRule.I_DID_NOT_SEE_A_THING,
        PlayerSpecialRule.CLOSE_SCRUTINY,
    )
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

/**
 * Standard Biased Referee in BB20205.
 * See page XXX in the BB2025 rulebook.
 */
@Serializable
class DodgyLeagueRep: BiasedReferee {
    override val type: BiasedRefereeType = BiasedRefereeType.DODGY_LEAGUE_REP
    override val name: String = BiasedRefereeType.DODGY_LEAGUE_REP.description
    override val specialRules = listOf(
        PlayerSpecialRule.I_DID_NOT_SEE_A_THING,
        PlayerSpecialRule.CLOSE_SCRUTINY,
    )
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}


// Not implemented yet
@Serializable
class RanulfRedHokuli: BiasedReferee {
    override val name: String = BiasedRefereeType.RANULF_RED_HOKULI.description
    override val type: BiasedRefereeType = BiasedRefereeType.RANULF_RED_HOKULI
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

// Not implemented yet
@Serializable
class ThoronKorensson: BiasedReferee {
    override val name: String = BiasedRefereeType.THORON_KORENSSON.description
    override val type: BiasedRefereeType = BiasedRefereeType.THORON_KORENSSON
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

// Not implemented yet
@Serializable
class JormTheOgre: BiasedReferee {
    override val name: String = BiasedRefereeType.JORM_THE_OGRE.description
    override val type: BiasedRefereeType = BiasedRefereeType.JORM_THE_OGRE
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

// Not implemented yet
@Serializable
class TheThrundlefootTriplets: BiasedReferee {
    override val name: String = BiasedRefereeType.THE_THRUNDLEFOOT_TRIPLETS.description
    override val type: BiasedRefereeType = BiasedRefereeType.THE_THRUNDLEFOOT_TRIPLETS
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

/**
 * Interface describing infamous coach abilities that are optional
 * to use.
 *
 * TODO Not sure if this is the correct way to model this.
 */
interface BiasedRefereeAbility: InducementEffect {
    // Some cards have special conditions on their triggers
    // Call this method to ensure that the card truly is available.
    // It assumes that the trigger Timing condition has been met
    fun isApplicable(state: Game, rules: Rules): Boolean {
        return true
    }
}
