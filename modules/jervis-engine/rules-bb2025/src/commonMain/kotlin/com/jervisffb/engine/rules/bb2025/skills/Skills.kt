package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.rules.common.skills.Skill

sealed interface BB2025Skill : Skill<Unit>
sealed interface BB2025IntSkill : Skill<Int>
sealed interface BB2025KeywordSkill : Skill<PlayerKeyword>
