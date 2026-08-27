package com.jervisffb.engine.bb2020.inducements.biasedreferee

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedReferee
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedRefereeType
import kotlinx.serialization.Serializable

@Serializable
enum class BiasedRefereeType2020(
    override val label: String
): BiasedRefereeType {
    STANDARD("Biased Referee") {
        override fun create(team: Team): BiasedReferee = StandardBiasedReferee(team)
    },
    RANULF_RED_HOKULI("Ranulf Red Hokuli") {
        override fun create(team: Team): BiasedReferee = RanulfRedHokuli(team)
    },
    THORON_KORENSSON("Thoron Korensson") {
        override fun create(team: Team): BiasedReferee = ThoronKorensson(team)
    },
    JORM_THE_OGRE("Jorm the Ogre") {
        override fun create(team: Team): BiasedReferee = JormTheOgre(team)
    },
    THE_THRUNDLEFOOT_TRIPLETS("The Thrundlefoot Triplets") {
        override fun create(team: Team): BiasedReferee = TheThrundlefootTriplets(team)
    }
}
