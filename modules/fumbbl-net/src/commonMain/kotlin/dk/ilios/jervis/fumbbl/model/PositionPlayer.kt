package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable

/**
 * Players that are _available_ on the roster, not actually on the team.
 */
@Serializable
data class RosterPlayer(
    val positionId: String,
    val positionName: String,
    val shorthand: String,
    val displayName: String?,
    val playerType: String?,
    val playerGender: String?,
    val quantity: Int,
    val movement: Int,
    val strength: Int,
    val agility: Int,
    val passing: Int,
    val armour: Int,
    val cost: Int,
    val race: String?,
    val undead: Boolean,
    val thrall: Boolean,
    val teamWithPositionId: String?,
    val nameGenerator: String?,
    val replacesPosition: String?,
    val urlPortrait: String?,
    val urlIconSet: String?,
    val nrOfIcons: Int,
    val skillCategoriesNormal: List<String>,
    val skillCategoriesDouble: List<String>,
    val skillArray: List<String> = listOf(),
    val skillValues: List<String?> = listOf(),
    val skillDisplayValues: List<String?> = listOf()
    // val keywords: List<Any>
)