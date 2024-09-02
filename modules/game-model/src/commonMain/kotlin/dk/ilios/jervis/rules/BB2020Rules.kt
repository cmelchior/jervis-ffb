package dk.ilios.jervis.rules

import kotlinx.serialization.Serializable

@Serializable
object BB2020Rules : Rules {
    override val name: String
        get() = "Blood Bowl 2020 Rules"
}
