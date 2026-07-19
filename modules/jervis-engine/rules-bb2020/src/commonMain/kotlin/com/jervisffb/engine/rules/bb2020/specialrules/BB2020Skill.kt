package com.jervisffb.engine.rules.bb2020.specialrules

import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Skill

interface BB2020SpecialRule : Skill<Unit> {
    override val keywords: List<SkillKeyword>
        get() = emptyList()
}
