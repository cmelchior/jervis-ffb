package com.jervisffb.fumbbl.net.model

import com.jervisffb.fumbbl.net.api.serialization.FumbblEnum
import com.jervisffb.fumbbl.net.api.serialization.FumbblEnumSerializer
import kotlinx.serialization.Serializable

class PushbackModeSerializer : FumbblEnumSerializer<PushbackMode>(PushbackMode::class)

@Serializable(with = PushbackModeSerializer::class)
enum class PushbackMode(
    override val id: String,
) : FumbblEnum {
    REGULAR("regular"),
    SIDE_STEP("sideStep"),
    GRAB("grab");
}
