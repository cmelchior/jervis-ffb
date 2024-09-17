package dk.ilios.jervis.fumbbl.model

import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnum
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnumSerializer
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
