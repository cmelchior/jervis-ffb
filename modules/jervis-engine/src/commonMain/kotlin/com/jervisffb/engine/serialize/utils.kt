package com.jervisffb.engine.serialize

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster

fun buildTeamFile(function: JervisTeamFileBuilder.() -> Unit): JervisTeamFile {
    val builder = JervisTeamFileBuilder()
    function(builder)
    return builder.build()
}

class JervisTeamFileBuilder {
    var metadata: JervisMetaData? = null
    var history: GameHistory? = null
    var team: Team? = null
    var roster: BB2020Roster? = null
    fun build(): JervisTeamFile {
        return JervisTeamFile(
            metadata!!,
            SerializedTeam.serialize(team!!),
            history,
        )
    }
}
