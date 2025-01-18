package com.jervisffb.fumbbl.web.api

import kotlinx.serialization.Serializable

// Typed return result from /coach/search/{term}
@Serializable
data class CoachSearchResult(
    val id: Long,
    val name: String,
)

