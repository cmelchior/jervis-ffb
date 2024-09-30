package dk.ilios.jervis.rules

import kotlinx.serialization.Serializable

@Serializable
object StandardBB2020Rules : BB2020Rules() {
    override val name: String
        get() = "Blood Bowl 2020 Rules"
}

open class BB2020Rules : Rules {
    override val name: String
        get() = "Blood Bowl 2020 Rules"
}
