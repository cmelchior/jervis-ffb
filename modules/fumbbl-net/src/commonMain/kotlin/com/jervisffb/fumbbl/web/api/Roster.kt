package com.jervisffb.fumbbl.web.api

import kotlinx.serialization.Serializable

@Serializable
data class Roster(
    val id: Int,
    val name: String,
)
