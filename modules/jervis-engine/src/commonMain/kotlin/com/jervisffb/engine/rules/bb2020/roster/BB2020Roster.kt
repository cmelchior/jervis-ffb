package com.jervisffb.engine.rules.bb2020.roster

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.roster.Roster
import kotlinx.serialization.Serializable

interface BB2020Roster : Roster {
    val tier: Int
    val specialRules: List<SpecialRules>
    override val positions: List<BB2020Position>
}
