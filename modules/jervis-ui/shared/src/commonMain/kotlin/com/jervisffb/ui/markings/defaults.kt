package com.jervisffb.ui.markings

import com.jervisffb.engine.rules.common.skills.SkillType

val defaultPlayerMarkings = listOf(
    PlayerMarking("B", skills = listOf(SkillType.BLOCK)),
    PlayerMarking("D", skills = listOf(SkillType.DODGE)),
    PlayerMarking("T", skills = listOf(SkillType.TACKLE)),
    PlayerMarking("G", skills = listOf(SkillType.GUARD)),
    PlayerMarking("Sf", skills = listOf(SkillType.STAND_FIRM)),
    PlayerMarking("M", skills = listOf(SkillType.MIGHTY_BLOW)),
    PlayerMarking("Sg", skills = listOf(SkillType.SNEAKY_GIT)),
    PlayerMarking("Dp", skills = listOf(SkillType.DIRTY_PLAYER)),
)
