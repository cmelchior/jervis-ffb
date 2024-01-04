package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnum

@Serializable
enum class SpecialRule(override val id: String): FumbblEnum {
    BADLANDS_BRAWL("Badlands Brawl"),
    BRIBERY_AND_CORRUPTION("Bribery and Corruption"),
    ELVEN_KINGDOMS_LEAGUE("Elven Kingdoms League"),
    FAVOURED_OF_KHORNE("Favoured of Khorne"),
    FAVOURED_OF_NURGLE("Favoured of Nurgle"),
    FAVOURED_OF_SLAANESH("Favoured of Slaanesh"),
    FAVOURED_OF_TZEENTCH("Favoured of Tzeentch"),
    FAVOURED_OF_UNDIVIDED("Favoured of Chaos Undivided"),
    HALFLING_THIMBLE_CUP("Halfling Thimble Cup"),
    LOW_COST_LINEMEN("Low Cost Linemen"),
    LUSTRIAN_SUPERLEAGUE("Lustrian Superleague"),
    MASTERS_OF_UNDEATH("Masters of Undeath"),
    OLD_WORLD_CLASSIC("Old World Classic"),
    SYLVANIAN_SPOTLIGHT("Sylvanian Spotlight"),
    UNDERWORLD_CHALLENGE("Underworld Challenge"),
    WORLDS_EDGE_SUPERLEAGUE("Worlds Edge Superleague");
}