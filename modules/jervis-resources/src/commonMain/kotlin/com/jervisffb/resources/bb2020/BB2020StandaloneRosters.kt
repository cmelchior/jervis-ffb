package com.jervisffb.resources.bb2020

import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.JervisRosterFile

object BB2020StandaloneRosters {
    val defaultRosters = mapOf(
        "amazon-roster-bb2020.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = AMAZON_TEAM_BB2020,
        ),
        "chaos-dwarf-roster-bb2020.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = CHAOS_DWARF_TEAM_BB2020,
        ),
        "elven-union-roster-bb2020.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = ELVEN_UNION_TEAM_BB2020,
        ),
        "human-roster-bb2020.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = HUMAN_TEAM_BB2020,
        ),
        "khorne-roster-bb2020.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = KHORNE_TEAM_BB2020,
        ),
        "lizardmen-roster-bb2020.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = LIZARDMEN_TEAM_BB2020,
        ),
        "orc-roster-bb2020.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = ORC_TEAM_BB2020,
        ),
        "skaven-roster-bb2020.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = SKAVEN_TEAM_BB2020,
        ),
    )
}
