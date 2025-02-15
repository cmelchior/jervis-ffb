package com.jervisffb.test

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.engine.serialize.buildTeamFile

/**
 * Converts a [Team] into a [JervisTeamFile], but all UI data will be empty.
 * This is mostly used for testing
 */
fun Team.createTeamFile(): JervisTeamFile {
    return buildTeamFile {
        metadata = JervisMetaData(FILE_FORMAT_VERSION)
        team = this@createTeamFile
        roster = this@createTeamFile.roster
    }
}
