package dk.ilios.analyzer.fumbbl.model

import kotlinx.serialization.Serializable

@Serializable
enum class BlockKind {
    BLOCK, STAB, VOMIT, CHAINSAW
}

@Serializable
data class TrapDoor(val coordinate: FieldCoordinate)

@Serializable
data class DiceDecoration(
    val coordinate: FieldCoordinate,
    val nrOfDice: Int,
    val blockKind: BlockKind
)

@Serializable
data class PlayerMarker(
    val playerId: String,
    val homeText: String?,
    val awayText: String?
)

@Serializable
data class FieldMarker(
    val coordinate: List<Int>,
    val homeText: String?,
    val awayText: String?
)

@Serializable
data class BloodSpot(
    val injury: Int,
    val coordinate: List<Int>
)

@Serializable
data class PlayerData(
    val playerId: String,
    val playerCoordinate: List<Int>,
    val playerState: Int,
    val cards: List<String>,
    val cardEffects: List<String>
)

@Serializable
data class FieldModel(
    val weather: String,
    val ballCoordinate: List<Int>,
    val ballInPlay: Boolean,
    val ballMoving: Boolean,
    val bombCoordinate: List<Int>?,
    val bombMoving: Boolean,
    val bloodspotArray: List<BloodSpot>,
    val pushbackSquareArray: List<FieldCoordinate>,
    val moveSquareArray: List<FieldCoordinate>,
    val trackNumberArray: List<Int>,
    val diceDecorationArray: List<DiceDecoration>,
    val fieldMarkerArray: List<FieldMarker>,
    val playerMarkerArray: List<PlayerMarker>,
    val playerDataArray: List<PlayerData>,
    val trapDoors: List<TrapDoor>
)