package com.jervisffb.ui.menu.components

import androidx.compose.ui.graphics.ImageBitmap
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.ui.game.model.ModelRef

data class TeamInfo(
    val teamId: TeamId,
    val teamName: String,
    val version: GameVersion,
    val type: GameType,
    val teamRoster: String,
    val teamValue: Int,
    val rerolls: Int,
    val logo: ImageBitmap,
    val teamData: ModelRef<Team>?, // For now just keep a reference to the original team. Might change later if teams are loaded on the server
)
