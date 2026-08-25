package com.jervisffb.engine.model.inducements.infamouscoach

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule

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
