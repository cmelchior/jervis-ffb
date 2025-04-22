package com.jervisffb.resources

import LIZARDMEN_TEAM
import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.JervisRosterFile

object StandaloneRosters {
    val defaultRosters = mapOf(
        "amazon-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = AMAZON_TEAM,
        ),
        "chaos-dwarf-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = CHAOS_DWARF_TEAM,
        ),
        "elven-union-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = ELVEN_UNION_TEAM,
        ),
        "human-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = HUMAN_TEAM,
        ),
        "khorne-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = KHORNE_TEAM,
        ),
        "lizardmen-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = LIZARDMEN_TEAM,
        ),
        "orc-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = ORC_TEAM,
        ),
        "skaven-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = SKAVEN_TEAM,
        ),
    )
}
