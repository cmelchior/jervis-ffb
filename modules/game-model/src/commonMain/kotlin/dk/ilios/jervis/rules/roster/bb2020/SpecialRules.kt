package dk.ilios.jervis.rules.roster.bb2020

sealed interface SpecialRules {
    val name: String
}

sealed interface RegionalSpecialRules: SpecialRules
data object BadlandsBrawl: RegionalSpecialRules { override val name: String = "Badlands Brawl" }
data object ElvenKingdomLeague: RegionalSpecialRules { override val name: String = "Elven Kingdom Leage" }
data object HalflingThimbleCup: RegionalSpecialRules { override val name: String = "Halfling Thimble Cup" }
data object LustrianSuperLeague: RegionalSpecialRules { override val name: String = "Lustrian Super Leage" }
data object OldWorldClassic: RegionalSpecialRules { override val name: String = "Old World Classic" }
data object SylvanianSpotlight: RegionalSpecialRules { override val name: String = "Sylvanian Spotlight" }
data object UnderworldChallenge: RegionalSpecialRules { override val name: String = "Underworld Challenge" }
data object WorldsEdgeSuperLeague: RegionalSpecialRules { override val name: String = "Worlds Edge Superleage" }

sealed interface TeamSpecialRules: SpecialRules
data object BriberyAndCorruption: TeamSpecialRules { override val name: String = "Bribery and Corruption" }
data object FavouredOfChaosUndivided: TeamSpecialRules { override val name: String = "Favoured of Chaos Undivided" }
data object FavouredOfKhorne: TeamSpecialRules { override val name: String = "Favoured of Khorne" }
data object FavouredOfNurgle: TeamSpecialRules { override val name: String = "Favoured of Nurgle" }
data object FavouredOfTzeentch: TeamSpecialRules { override val name: String = "Favoured of Tzeentch" }
data object FavouredOfSlaanesh: TeamSpecialRules { override val name: String = "Favoured of Slaanesh" }
data object LowCostLinemen: TeamSpecialRules { override val name: String = "Low Cost Linemen" }
data object MastersOfUndeath: TeamSpecialRules { override val name: String = "Masters of Undeath" }
