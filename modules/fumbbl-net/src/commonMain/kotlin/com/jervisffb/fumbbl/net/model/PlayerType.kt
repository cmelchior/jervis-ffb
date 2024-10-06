package com.jervisffb.fumbbl.net.model

import com.jervisffb.fumbbl.net.api.serialization.FumbblEnum
import com.jervisffb.fumbbl.net.api.serialization.FumbblEnumSerializer
import kotlinx.serialization.Serializable

class PlayerTypeSerializer : FumbblEnumSerializer<com.jervisffb.fumbbl.net.model.PlayerType>(com.jervisffb.fumbbl.net.model.PlayerType::class)

@Serializable(with = com.jervisffb.fumbbl.net.model.PlayerTypeSerializer::class)
enum class PlayerType(override val id: String) : FumbblEnum {
    BIG_GUY("Big Guy"),
    INFAMOUS_STAFF("Infamous Staff"),
    IRREGULAR("Irregular"),
    MERCENARY("Mercenary"),
    PLAGUE_RIDDEN("PlagueRidden"),
    RAISED_FROM_DEAD("RaisedFromDead"),
    REGULAR("Regular"),
    RIOTOUS_ROOKIE("RiotousRookie"),
    STAR("Star"),
}
