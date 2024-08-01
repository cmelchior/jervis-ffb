package com.jervis.fumbbl.restapi

import kotlinx.serialization.Serializable

@Serializable
data class Coach(
    val id: Int,
    val name: String
)