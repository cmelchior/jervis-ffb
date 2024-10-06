package com.jervisffb.fumbbl.web.api

data class SkillLimits(
    val categories: List<com.jervisffb.fumbbl.web.api.Category>,
    val spp: List<List<Int>>,
)
