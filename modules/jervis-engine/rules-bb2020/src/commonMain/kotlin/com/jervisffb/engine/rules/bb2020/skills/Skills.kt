package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Skill

sealed interface BB2020Skill : Skill<Unit> {
    override val keywords: List<SkillKeyword>
        get() = emptyList()
}
sealed interface BB2020IntSkill : Skill<Int> {
    override val keywords: List<SkillKeyword>
        get() = emptyList()
}
