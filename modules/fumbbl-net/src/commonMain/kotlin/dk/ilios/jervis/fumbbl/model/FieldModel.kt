package dk.ilios.jervis.fumbbl.model

import dk.ilios.jervis.fumbbl.model.change.MoveSquare
import dk.ilios.jervis.fumbbl.model.change.PlayerId
import dk.ilios.jervis.fumbbl.model.change.PushBackSquare
import dk.ilios.jervis.fumbbl.model.change.TargetSelectionState
import dk.ilios.jervis.model.DogOut.coordinate
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
    val blockKind: BlockKind? = null
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
    var weather: String,
    var ballCoordinate: FieldCoordinate?,
    var ballInPlay: Boolean,
    var ballMoving: Boolean,
    var bombCoordinate: FieldCoordinate?,
    var bombMoving: Boolean,
    var targetSelectionState: TargetSelectionState? = null,
    var rangeRuler: RangeRuler? = null,
    val bloodspotArray: MutableList<BloodSpot>,
    val pushbackSquareArray: MutableList<PushBackSquare>,
    val moveSquareArray: MutableList<MoveSquare>,
    val trackNumberArray: MutableList<TrackNumber>,
    val diceDecorationArray: MutableList<DiceDecoration>,
    val fieldMarkerArray: MutableList<FieldMarker>,
    val playerMarkerArray: MutableList<PlayerMarker>,
    val playerDataArray: MutableList<PlayerData>,
    val trapDoors: MutableList<TrapDoor>
) {
    private val coordinateByPlayerId = mutableMapOf<String, FieldCoordinate>()
    private val playerIdByCoordinate = mutableMapOf<FieldCoordinate, MutableList<String>>()
    private val cardsByPlayerId = mutableMapOf<String, MutableSet<String>>()
    private val cardEffectByPlayerId = mutableMapOf<String, MutableSet<String>>()

    private val stateByPlayerId = mutableMapOf<String, PlayerState>()

    fun setPlayerCoordinate(playerId: String, coordinate: FieldCoordinate) {
        coordinateByPlayerId[playerId] = coordinate
    }

    fun getPlayerCoordinate(playerId: String): FieldCoordinate? {
        return coordinateByPlayerId[playerId]
    }

    fun setPlayerState(playerId: String, state: PlayerState) {
        stateByPlayerId[playerId] = state
    }

    fun getPlayerState(playerId: String): PlayerState? {
        return stateByPlayerId[playerId]
    }

    fun removeSkillEnhancements(player: Player, skill: String?) {
        player.removeEnhancement(skill)
    }

    fun addMoveSquare(move: MoveSquare) {
        moveSquareArray.add(move)

    }

    fun removeMoveSquare(move: MoveSquare) {
        moveSquareArray.remove(move)
    }

    fun addTrackNumber(number: TrackNumber) {
        trackNumberArray.add(number)
    }

    fun removeTrackNumber(number: TrackNumber) {
        trackNumberArray.remove(number)
    }

    fun addDiceDecoration(decoration: DiceDecoration) {
        diceDecorationArray.add(decoration)
    }

    fun removeDiceDecoration(decoration: DiceDecoration) {
        diceDecorationArray.remove(decoration)
    }

    fun addPushBackSquare(pushback: PushBackSquare) {
        pushbackSquareArray.add(pushback)
    }

    fun removePushbackSquare(pushback: PushBackSquare) {
        pushbackSquareArray.remove(pushback)
    }

    fun removePlayer(player: PlayerId) {
        if (coordinateByPlayerId.remove(player.id) == null) {
            // Could not find player to remove.
            // It seems that FUMBBL sometimes run into this for some reason
        }
    }

    fun addPlayerMarker(marker: PlayerMarker) {
        playerMarkerArray.add(marker)
    }

    fun removePlayerMarker(marker: PlayerMarker) {
        playerMarkerArray.remove(marker)
    }

    fun addBloodSpot(spot: BloodSpot) {
        bloodspotArray.add(spot)
    }

    fun removeFieldMarker(marker: FieldMarker) {
        fieldMarkerArray.remove(marker)
    }

    fun addPrayerEnhancements(playerId: String, prayer: String) {
        // TODO
    }

    fun removePrayerEnhancement(playerId: String, prayer: String) {
        // TODO
    }

    fun addTrapDoor(trapDoor: TrapDoor) {
        trapDoors.add(trapDoor)
    }

    fun removeTrapDoor(trapDoor: TrapDoor) {
        trapDoors.remove(trapDoor)
    }

    fun addCard(playerId: String, card: String) {
        // TODO They also do other stuff. See Java implementation
        if (!cardsByPlayerId.containsKey(playerId)) {
            cardsByPlayerId[playerId] = mutableSetOf()
        }
        cardsByPlayerId[playerId]!!.add(card)
    }

    fun removeCard(playerId: String, card: String) {
        // TODO They also do other stuff. See Java implementation
        cardsByPlayerId[playerId]?.remove(card)
    }

    fun addCardEffect(playerId: String, cardEffect: String) {
        // TODO They also do other stuff. See Java implementation
        if (!cardEffectByPlayerId.containsKey(playerId)) {
            cardEffectByPlayerId[playerId] = mutableSetOf()
        }
        cardEffectByPlayerId[playerId]!!.add(cardEffect)
    }

    fun removeCardEffect(playerId: String, cardEffect: String) {
        // TODO They also do other stuff. See Java implementation
        cardEffectByPlayerId[playerId]?.remove(cardEffect)
    }


}