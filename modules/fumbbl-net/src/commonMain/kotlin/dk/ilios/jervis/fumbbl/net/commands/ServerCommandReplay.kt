package dk.ilios.jervis.fumbbl.net.commands

import dk.ilios.jervis.fumbbl.model.ReportId
import dk.ilios.jervis.fumbbl.model.change.ModelChange
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
    val modelChangeArray: List<ModelChange>
)

@Serializable
data class ReportList(
    val reports: List<Report>
)

@Serializable
data class Report(
    val reportId: ReportId,
    val teamId: String? = null,
    val dedicatedFans: Int? = null,
    val dedicatedFansRoll: Int? = null,
    val dedicatedFansResult: Int? = null,
    val weather: String? = null,
    val weatherRoll: List<Int> = listOf()
)