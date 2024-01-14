package dk.ilios.jervis.fumbbl.net.commands

import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.change.ModelChange
import dk.ilios.jervis.fumbbl.model.reports.Report
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("serverReplay")
data class ServerCommandReplay(
    override val netCommandId: String,
    override val commandNr: Int,
    val totalNrOfCommands: Int,
    val commandArray: List<ServerCommandModelSync>,
    val lastCommand: Boolean
): ServerCommand

//"netCommandId": "serverAddPlayer",
//"commandNr": 6,
//"teamId": "662452",
//"player": {
//    "playerKind": "rosterPlayer",
//    "playerId": "662452S1",
//    "playerNr": 14,
//    "positionId": "3055",
//    "playerName": "Ugroth Bolgrot",
//    "playerGender": "male",
//    "playerType": "Star",
//    "movement": 5,
//    "strength": 3,
//    "agility": 3,
//    "passing": 0,
//    "armour": 9,
//    "lastingInjuries": [],
//    "recoveringInjury": null,
//    "urlPortrait": "",
//    "urlIconSet": null,
//    "nrOfIcons": 0,
//    "positionIconIndex": 0,
//    "skillArray": [
//    "Chainsaw",
//    "Loner",
//    "Secret Weapon"
//    ],
//    "temporarySkillsMap": {},
//    "temporaryModifiersMap": {},
//    "temporaryPropertiesMap": {},
//    "skillValuesMap": {},
//    "skillDisplayValuesMap": {},
//    "usedSkills": []
//},
//"playerState": 9,
//"sendToBoxReason": null,
//"sendToBoxTurn": 0,
//"sendToBoxHalf": 0
//},

@Serializable
@SerialName("serverAddPlayer")
data class ServerCommandAddPlayer(
    override val netCommandId: String,
    override val commandNr: Int
): ServerCommand

//@Serializable
//@JsonClassDiscriminator("netCommandId")
//sealed interface ReplayCommand {
//    val netCommandId: NetCommandId
//}

//@Serializable
//@SerialName("serverModelSync")
//data class ReplayModelChange(
//    val netCommandId: NetCommandId,
//    val commandNr: Int,
//    val modelChangeList: ModelChangeList,
//    val reportList: ReportList,
//    // val sound: Any?,  // If this could be some specific type, please replace `Any?` with that type
//    val gameTime: Int,
//    val turnTime: Int
//)
//
//@Serializable
//@SerialName("serverAddPlayer")
//data class ReplayAddPlayer(
//    val netCommandId: NetCommandId,
//    val commandNr: Int,
//    // Unclear why this is here
////    val modelChangeList: ModelChangeList,
////    val reportList: ReportList,
////    // val sound: Any?,  // If this could be some specific type, please replace `Any?` with that type
////    val gameTime: Int,
////    val turnTime: Int
//)

@Serializable
data class ModelChangeList(
    val modelChangeArray: List<ModelChange>,
): List<ModelChange> {
    override val size: Int = modelChangeArray.size
    override fun get(index: Int): ModelChange = modelChangeArray[index]
    override fun isEmpty(): Boolean = modelChangeArray.isEmpty()
    override fun iterator(): Iterator<ModelChange> = modelChangeArray.iterator()
    override fun listIterator(): ListIterator<ModelChange> = modelChangeArray.listIterator()
    override fun listIterator(index: Int): ListIterator<ModelChange> = modelChangeArray.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): List<ModelChange> = modelChangeArray.subList(fromIndex, toIndex)
    override fun lastIndexOf(element: ModelChange): Int  = modelChangeArray.lastIndexOf(element)
    override fun indexOf(element: ModelChange): Int = modelChangeArray.indexOf(element)
    override fun containsAll(elements: Collection<ModelChange>): Boolean = modelChangeArray.containsAll(elements)
    override fun contains(element: ModelChange): Boolean = modelChangeArray.contains(element)
    fun contains(changeId: ModelChangeId): Boolean = modelChangeArray.firstOrNull { it.id == changeId } != null
}

@Serializable
data class ReportList(
    val reports: List<Report>
): List<Report> {
    override val size: Int = reports.size
    override fun get(index: Int): Report = reports[index]
    override fun isEmpty(): Boolean = reports.isEmpty()
    override fun iterator(): Iterator<Report> = reports.iterator()
    override fun listIterator(): ListIterator<Report> = reports.listIterator()
    override fun listIterator(index: Int): ListIterator<Report> = reports.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): List<Report> = reports.subList(fromIndex, toIndex)
    override fun lastIndexOf(element: Report): Int  = reports.lastIndexOf(element)
    override fun indexOf(element: Report): Int = reports.indexOf(element)
    override fun containsAll(elements: Collection<Report>): Boolean = reports.containsAll(elements)
    override fun contains(element: Report): Boolean = reports.contains(element)
}

