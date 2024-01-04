package dk.ilios.jervis.fumbbl.net

import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnum
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnumSerializer
import kotlinx.serialization.Serializable

class ClientModeSerializer: FumbblEnumSerializer<ClientMode>(ClientMode::class)

@Serializable(with = ClientModeSerializer::class)
enum class ClientMode(override val id: String): FumbblEnum {
    PLAYER("player"),
    REPLAY("replay"),
    SPECTATOR("spectator"),
}