package com.jervisffb.fumbbl.net.api.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("serverGameTime")
data class ServerCommandGameTime(
    override val netCommandId: String,
    override val commandNr: Int,
    val gameTime: Int,
    val turnTime: Int,
) : ServerCommand
