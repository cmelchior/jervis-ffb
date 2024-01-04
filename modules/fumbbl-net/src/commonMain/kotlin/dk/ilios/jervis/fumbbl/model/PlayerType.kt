package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnum
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnumSerializer

class PlayerTypeSerializer: FumbblEnumSerializer<PlayerType>(PlayerType::class)

@Serializable(with = PlayerTypeSerializer::class)
enum class PlayerType(override val id: String): FumbblEnum {
    BIG_GUY("Big Guy"),
    INFAMOUS_STAFF("Infamous Staff"),
    IRREGULAR("Irregular"),
    MERCENARY("Mercenary"),
    PLAGUE_RIDDEN("PlagueRidden"),
    RAISED_FROM_DEAD("RaisedFromDead"),
    REGULAR("Regular"),
    RIOTOUS_ROOKIE("RiotousRookie"),
    STAR("Star");
}