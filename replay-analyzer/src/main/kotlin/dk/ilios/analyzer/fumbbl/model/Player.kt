package dk.ilios.analyzer.fumbbl.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val playerKind: String, // This can either be `zappedPlayer` or `rosterPlayer`
    val playerId: String,
    val playerNr: Int,
    val positionId: String,
    val playerName: String,
    val playerGender: String,
    val playerType: PlayerType,
    val movement: Int,
    val strength: Int,
    val agility: Int,
    val passing: Int,
    val armour: Int,
    val lastingInjuries: List<String>,
    val recoveringInjury: String?,
    val urlPortrait: String?,
    val urlIconSet: String?,
    val nrOfIcons: Int,
    val positionIconIndex: Int,
    val skillArray: List<String>,
    val temporarySkillsMap: Map<String, String>,
    val temporaryModifiersMap: Map<String, String>,
    val temporaryPropertiesMap: Map<String, String>,
    val skillValuesMap: Map<String, String?>,
    val skillDisplayValuesMap: Map<String, String?>,
    val playerStatus: String,
    val usedSkills: List<String>
)