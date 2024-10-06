package com.jervisffb.fumbbl.web.api

data class RecordX(
    val cas: com.jervisffb.fumbbl.web.api.Cas,
    val form: String,
    val games: Int,
    val losses: Int,
    val td: com.jervisffb.fumbbl.web.api.Td,
    val ties: Int,
    val wins: Int,
)
