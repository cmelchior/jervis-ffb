package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnum
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnumSerializer

class TurnModeSerializer: FumbblEnumSerializer<TurnMode>(TurnMode::class)

@Serializable(with = TurnModeSerializer::class)
enum class TurnMode(override val id: String, val isCheckForActivePlayers: Boolean = false): FumbblEnum {
    BETWEEN_TURNS("betweenTurns", true),
    BOMB_AWAY_BLITZ("bombAwayBlitz"),
    BOMB_HOME("bombHome"),
    BOMB_HOME_BLITZ("bombHomeBlitz"),
    ILLEGAL_SUBSTITUTION("illegalSubstitution"),
    PASS_BLOCK("passBlock"),
    PERFECT_DEFENCE("perfectDefence"),
    QUICK_SNAP("quickSnap"),
    REGULAR("regular", true),
    SELECT_BLOCK_KIND("selectBlockKind"),
    SELECT_GAZE_TARGET("selectGazeTarget"),
    SWARMING("swarming"),
    TOUCHBACK("touchback"),
    BLITZ("blitz", true),
    BOMB_AWAY("bombAway"),
    DUMP_OFF("dumpOff"),
    END_GAME("endGame"),
    HIGH_KICK("highKick"),
    HIT_AND_RUN("hitAndRun"),
    INTERCEPTION("interception"),
    KICKOFF("kickoff"),
    KICKOFF_RETURN("kickoffReturn"),
    NO_PLAYERS_TO_FIELD("noPlayersToField"),
    RAIDING_PARTY("raidingParty"),
    SAFE_PAIR_OF_HANDS("safePairOfHands"),
    SELECT_BLITZ_TARGET("selectBlitzTarget"),
    SETUP("setup"),
    SOLID_DEFENCE("solidDefence"),
    START_GAME("startGame"),
    WIZARD("wizard");

    fun checkNegatraits(): Boolean {
        return this != KICKOFF_RETURN && this != PASS_BLOCK && !isBombTurn
    }

    val isBombTurn: Boolean
        get() =
            this == BOMB_HOME || this == BOMB_HOME_BLITZ || this == BOMB_AWAY || this == BOMB_AWAY_BLITZ

    companion object {
        fun forName(name: String?): TurnMode? {
            return values().firstOrNull { it.id == name }
        }
    }
}