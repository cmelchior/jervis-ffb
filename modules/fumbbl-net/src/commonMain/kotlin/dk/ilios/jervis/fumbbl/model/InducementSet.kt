package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable

// See com.fumbbl.ffb.inducement.*
// There are quite a lot of functionality to port there.
@Serializable
data class InducementSet(
    val inducementArray: MutableList<Inducement>,
    val cardsAvailable: MutableList<String>,
    val cardsActive: MutableList<String>,
    val cardsDeactivated: MutableList<String>,
    val prayers: MutableList<String>
)

@Serializable
data class Inducement(
    val inducementType: String,
    val value: Int,
    val uses: Int
)
