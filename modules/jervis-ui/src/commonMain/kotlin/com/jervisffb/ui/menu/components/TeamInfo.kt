package com.jervisffb.ui.menu.components

import androidx.compose.ui.graphics.ImageBitmap
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId

data class TeamInfo(
    val teamId: TeamId,
    val teamName: String,
    val teamRoster: String,
    val teamValue: Int,
    val rerolls: Int,
    val logo: ImageBitmap,
    val teamData: Team?, // For now just keep a reference to the original team. Might change later if teams are loaded on the server
)
