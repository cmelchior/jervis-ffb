package dk.ilios.analyzer.fumbbl.model

import kotlinx.serialization.Serializable

// See com.fumbbl.ffb.inducement.*
// There are quite a lot of functionality to port there.
@Serializable
data class InducementSet(
    val inducementArray: List<String>,
    val cardsAvailable: List<String>,
    val cardsActive: List<String>,
    val cardsDeactivated: List<String>,
    val prayers: List<String>
)
