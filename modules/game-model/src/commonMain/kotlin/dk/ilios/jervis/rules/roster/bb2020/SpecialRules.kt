package dk.ilios.jervis.rules.roster.bb2020

sealed interface SpecialRules {
    val description: String
}

enum class RegionalSpecialRules(override val description: String) : SpecialRules {
    BADLANDS_BRAWL("Badlands Brawl"),
    ELVEN_KINGDOM_LEAGUE("Elven Kingdom League"),
    HAFLING_THIMBLE_CUP("Hafling Thimble Cup"),
    LUSTRIAN_SUPERLEAGUE("Lustrian Superleague"),
    OLD_WORLD_CLASSIC("Old World Classic"),
    SYLVIAN_SPOTLIGHT("Sylvan Spolight"),
    UNDERWORLD_CHALLENGE("Underworld Challenge"),
    WORLDS_EDGE_SUPERLEAGUE("Worlds Edge Superleague")
}

enum class TeamSpecialRules(override val description: String): SpecialRules {
    BRIBERY_AND_CORRUPTION("Bribery and Corruption"),
    FAVOURED_OF_CHAOS_UNDIVIDED("Favoured of Chaos Undivided"),
    FAVOURED_OF_KHORNE("Favoured of Khorne"),
    FAVOURED_OF_NURGLE("Favoured of Nurgle"),
    FAVOURED_OF_TZEENTCH("Favoured of Tzeentch"),
    FAVOURED_OF_SLAANESH("Favoured of Slaanesh"),
    LOW_COST_LINEMEN("Low Cost Linemen"),
    MASTERS_OF_UNDEATH("Masters of Undeath"),
}
