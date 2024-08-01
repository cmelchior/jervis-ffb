package com.jervis.fumbbl.restapi

import kotlinx.serialization.Serializable

@Serializable
data class Roster(
    val id: Int,
    val name: String
)