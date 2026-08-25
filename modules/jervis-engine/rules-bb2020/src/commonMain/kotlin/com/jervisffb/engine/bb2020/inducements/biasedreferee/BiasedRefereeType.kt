package com.jervisffb.engine.bb2020.inducements.biasedreferee

import kotlinx.serialization.Serializable

@Serializable
enum class BiasedRefereeType(override val label: String): com.jervisffb.engine.model.inducements.biasedreferee.BiasedRefereeType {
    STANDARD("Biased Referee"), // BB2020
    RANULF_RED_HOKULI("Ranulf Red Hokuli"),
    THORON_KORENSSON("Thoron Korensson"),
    JORM_THE_OGRE("Jorm the Ogre"),
    THE_THRUNDLEFOOT_TRIPLETS("The Thrundlefoot Triplets")
}
