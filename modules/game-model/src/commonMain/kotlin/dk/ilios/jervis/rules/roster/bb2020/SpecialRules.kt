package dk.ilios.jervis.rules.roster.bb2020

sealed interface SpecialRules {
    val description: String
}

enum class RegionalSpecialRule(override val description: String) : SpecialRules {
    BADLANDS_BRAWL("Badlands Brawl"),
    ELVEN_KINGDOM_LEAGUE("Elven Kingdom League"),
    HAFLING_THIMBLE_CUP("Hafling Thimble Cup"),
    LUSTRIAN_SUPERLEAGUE("Lustrian Superleague"),
    OLD_WORLD_CLASSIC("Old World Classic"),
    SYLVIAN_SPOTLIGHT("Sylvan Spolight"),
    UNDERWORLD_CHALLENGE("Underworld Challenge"),
    WORLDS_EDGE_SUPERLEAGUE("Worlds Edge Superleague")
}

enum class TeamSpecialRule(override val description: String): SpecialRules {
    BRIBERY_AND_CORRUPTION("Bribery and Corruption"),
    FAVOURED_OF_CHAOS_UNDIVIDED("Favoured of Chaos Undivided"),
    FAVOURED_OF_KHORNE("Favoured of Khorne"),
    FAVOURED_OF_NURGLE("Favoured of Nurgle"),
    FAVOURED_OF_TZEENTCH("Favoured of Tzeentch"),
    FAVOURED_OF_SLAANESH("Favoured of Slaanesh"),
    LOW_COST_LINEMEN("Low Cost Linemen"),
    MASTERS_OF_UNDEATH("Masters of Undeath"),
}

// Special rules that are being applied by procedures, i.e. they do not
// have any "state", just an effect on how the game is played.
enum class PlayerSpecialRule(override val description: String): SpecialRules {
    RIOTOUS_ROOKIE("Riotous Rookie"), // Inducement, see page 91 in the rulebook.
    CLOSE_SCRUTINY("Close Scrutiny"), // Biased Referee Inducement, see page 95 in the rulebook.
    I_DID_NOT_SEE_A_THING("I Didn't See A Thing"), // Biased Referee Inducement, see page 95 in the rulebook

    KEEN_PLAYER("Keen Player"), // Infamous Coaching Staff: Josef Bugman, see page 93 in the rulebook
    BUGMANS_XXXXXX("Bugman's XXXXXX"), // Infamous Coaching Staff: Josef Bugman, see page 93 in the rulebook

    IF_YOU_WANT_THE_JOB_DONE("If You Want The Job Done..."), // Infamous Coaching Staff: Kari Coldsteel, page 15 in Deathzone

    SPOT_THE_SNEAK("Spot the Sneak") // This player was placed on the field using the Dirty Trick Spot the Sneak
}
