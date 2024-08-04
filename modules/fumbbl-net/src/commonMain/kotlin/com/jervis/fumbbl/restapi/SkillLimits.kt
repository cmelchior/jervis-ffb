package com.jervis.fumbbl.restapi

data class SkillLimits(
    val categories: List<Category>,
    val spp: List<List<Int>>,
)
