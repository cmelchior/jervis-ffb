package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnum
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnumSerializer

class PlayerActionSerializer: FumbblEnumSerializer<PlayerAction>(PlayerAction::class)

@Serializable(with = PlayerActionSerializer::class)
enum class PlayerAction(
    override val id: String,
    val type: Int,
    val description: String?,
    val delegate: PlayerAction? = null
): FumbblEnum {
    MOVE("move",1,"starts a Move Action"),
    BLOCK("block",2,"starts a Block Action"),
    BLITZ("blitz",3,null),
    BLITZ_MOVE("blitzMove",3,"starts a Blitz Action"),
    BLITZ_SELECT("blitzSelect",3,null),
    HAND_OVER("handOver",5,null),
    HAND_OVER_MOVE("handOverMove",5,"starts a Hand Over Action"),
    PASS("pass",7,null),
    PASS_MOVE("passMove",7,"starts a Pass Action"),
    FOUL("foul",9,null),
    FOUL_MOVE("foulMove",9,"starts a Foul Action"),
    STAND_UP("standUp",11,"stands up"),
    THROW_TEAM_MATE("throwTeamMate",12,null),
    THROW_TEAM_MATE_MOVE("throwTeamMateMove",12,"starts a Throw Team-mate action"),
    REMOVE_CONFUSION("removeConfusion",14,null),
    GAZE("gaze",15,null),
    GAZE_SELECT("gazeSelect",15,null),
    GAZE_MOVE("gazeMove",15,"starts a Gaze action"),
    MULTIPLE_BLOCK("multipleBlock",16,"starts a Block Action"),
    HAIL_MARY_PASS("hailMaryPass",7,null),
    DUMP_OFF("dumpOff",7,null),
    STAND_UP_BLITZ("standUpBlitz",3,"stands up with Blitz"),
    THROW_BOMB("throwBomb",20,"starts a Bomb Action"),
    HAIL_MARY_BOMB("hailMaryBomb",21,null),
    SWOOP("swoop",30,null),
    KICK_TEAM_MATE_MOVE("kickTeamMateMove",31,"starts a Kick Team-mate action"),
    KICK_TEAM_MATE("kickTeamMate",31,null),
    TREACHEROUS("treacherous",32,null),
    WISDOM_OF_THE_WHITE_DWARF("wisdomOfTheWhiteDwarf",33,null),
    THROW_KEG("throwKey",34,"readies a beer keg"),
    RAIDING_PARTY("raidingParty",35,null),
    MAXIMUM_CARNAGE("maximumCarnage",36,null),
    LOOK_INTO_MY_EYES("lookIntoMyEyes",37,"tries to steal the ball"),
    BALEFUL_HEX("balefulHex",38,null),
    ALL_YOU_CAN_EAT("allYouCanEat",39,"starts an All You Can Eat action",THROW_BOMB),
    PUTRID_REGURGITATION_MOVE("putridRegurgitationMove",40,null),
    PUTRID_REGURGITATION_BLITZ("putridRegurgitationBlitz",40,"performs an additional Projectile Vomit attack"),
    PUTRID_REGURGITATION_BLOCK("putridRegurgitationBlock",40,"performs an additional Projectile Vomit attack"),
    KICK_EM_BLOCK("kickEmBlock",41,"targets a downed opponent"),
    KICK_EM_BLITZ("kickEmBlitz",41,"targets a downed opponent");

    // TODO This looks like it should be covered by a Rules module rather than inside the model
    val isMoving: Boolean get() = this == MOVE || this == BLITZ_MOVE || this == HAND_OVER_MOVE || this == PASS_MOVE || this == FOUL_MOVE || this == THROW_TEAM_MATE_MOVE || this == KICK_TEAM_MATE_MOVE || this == GAZE_MOVE || this == PUTRID_REGURGITATION_MOVE || this == KICK_EM_BLITZ
    val isPassing: Boolean get() = this == PASS || this == DUMP_OFF || this == HAND_OVER || this == HAIL_MARY_PASS || this == THROW_BOMB || this == HAIL_MARY_BOMB
    fun allowsFumblerooskie(): Boolean { return isMoving }
    val isBlitzing: Boolean get() = this == BLITZ || this == BLITZ_SELECT || isBlitzMove
    val isGaze: Boolean get() = this == GAZE || this == GAZE_MOVE || this == GAZE_SELECT
    val isBomb: Boolean get() = this == THROW_BOMB || delegate == THROW_BOMB
    val isPutrid: Boolean get() = isPutridBlock || this == PUTRID_REGURGITATION_MOVE
    val isPutridBlock: Boolean get() = this == PUTRID_REGURGITATION_BLITZ || this == PUTRID_REGURGITATION_BLOCK
    val isKickingDowned: Boolean get() = this == KICK_EM_BLITZ || this == KICK_EM_BLOCK
    fun forceLog(): Boolean { return isPutridBlock }
    val isBlitzMove: Boolean get() = this == BLITZ_MOVE || this == PUTRID_REGURGITATION_MOVE || this == KICK_EM_BLITZ
}