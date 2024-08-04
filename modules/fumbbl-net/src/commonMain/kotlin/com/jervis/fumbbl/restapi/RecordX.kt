package com.jervis.fumbbl.restapi

data class RecordX(
    val cas: Cas,
    val form: String,
    val games: Int,
    val losses: Int,
    val td: Td,
    val ties: Int,
    val wins: Int,
)
