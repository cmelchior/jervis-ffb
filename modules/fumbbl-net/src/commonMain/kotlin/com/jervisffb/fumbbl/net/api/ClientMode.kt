package com.jervisffb.fumbbl.net.api

import com.jervisffb.fumbbl.net.api.serialization.FumbblEnum
import com.jervisffb.fumbbl.net.api.serialization.FumbblEnumSerializer
import kotlinx.serialization.Serializable

class ClientModeSerializer : FumbblEnumSerializer<ClientMode>(ClientMode::class)

@Serializable(with = ClientModeSerializer::class)
enum class ClientMode(override val id: String) : FumbblEnum {
    PLAYER("player"),
    REPLAY("replay"),
    SPECTATOR("spectator"),
}
