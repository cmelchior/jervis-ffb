package com.jervis.fumbbl.restapi

import kotlinx.serialization.Serializable

@Serializable
data class SkillStatus(
    val maxLimit: Int,
    val status: String,
    val tier: Int,
)
