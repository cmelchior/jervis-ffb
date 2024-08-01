package com.jervis.fumbbl.restapi

import kotlinx.serialization.Serializable

@Serializable
data class Stats(
    val AG: Int,
    val AV: Int,
    val MA: Int,
    val PA: Int,
    val ST: Int
)