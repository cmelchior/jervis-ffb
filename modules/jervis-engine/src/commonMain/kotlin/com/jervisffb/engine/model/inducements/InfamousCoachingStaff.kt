package com.jervisffb.engine.model.inducements

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule

enum class InfamousCoachingStaffType {
    JOSEF_BUGMAN,
    KARI_COLDSTEEL,
    PAPA_SKULLBONES
}


interface InfamousCoachingStaff {
    val type: InfamousCoachingStaffType
    val name: String
    val specialRules: List<PlayerSpecialRule>
    val specialAbilities: List<InfamousCoachAbility>
    val price: Int
    fun isAvailable(team: Team): Boolean {
        return true
    }
}

class JosefBugman: InfamousCoachingStaff {
    override val type: InfamousCoachingStaffType = InfamousCoachingStaffType.JOSEF_BUGMAN
    override val name: String = "Josef Bugman"
    override val price: Int = 100_000
    override val specialRules = listOf(
        PlayerSpecialRule.BUGMANS_XXXXXX,
        PlayerSpecialRule.KEEN_PLAYER
    )
    override val specialAbilities: List<InfamousCoachAbility> = emptyList()
}

// See page 15 in the Deathzone rulebook
class KariColdsteel: InfamousCoachingStaff {
    override val type: InfamousCoachingStaffType = InfamousCoachingStaffType.KARI_COLDSTEEL
    override val name: String = "Kari Coldsteel"
    override val price: Int = 50_000
    override val specialRules = listOf(
        PlayerSpecialRule.IF_YOU_WANT_THE_JOB_DONE,
    )
    override fun isAvailable(team: Team): Boolean {
        return team.roster.specialRules.intersect(
            listOf(
                RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE,
                RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE,
                RegionalSpecialRule.OLD_WORLD_CLASSIC,
                RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE
            )
        ).isNotEmpty()
    }
    override val specialAbilities: List<InfamousCoachAbility> = emptyList()
}

// See page 16 in the Deathzone rulebook
class PapaSkullbones: InfamousCoachingStaff {
    override val type: InfamousCoachingStaffType = InfamousCoachingStaffType.PAPA_SKULLBONES
    override val name: String = "Papa Skullbones"
    override val price: Int = 80_000
    override val specialRules = listOf(PlayerSpecialRule.IF_YOU_WANT_THE_JOB_DONE)
    override val specialAbilities: List<InfamousCoachAbility> = listOf(
        ByThePowerOfTheGoods()
    )

    override fun isAvailable(team: Team): Boolean {
        return team.roster.specialRules.intersect(
            listOf(
                TeamSpecialRule.FAVOURED_OF_KHORNE,
                TeamSpecialRule.FAVOURED_OF_NURGLE,
                TeamSpecialRule.FAVOURED_OF_SLAANESH,
                TeamSpecialRule.FAVOURED_OF_TZEENTCH,
                TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED,
                RegionalSpecialRule.UNDERWORLD_CHALLENGE
            )
        ).isNotEmpty()
    }
}

