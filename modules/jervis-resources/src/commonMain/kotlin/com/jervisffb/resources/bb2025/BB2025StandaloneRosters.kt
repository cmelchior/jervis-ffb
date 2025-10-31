package com.jervisffb.resources.bb2025

import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.JervisRosterFile

object BB2025StandaloneRosters {
    val defaultRosters = mapOf(
        "amazon-roster-bb2025.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = AMAZON_TEAM_BB2025,
        ),
        "chaos-dwarf-roster-bb2025.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = CHAOS_DWARF_TEAM_BB2025,
        ),
        "elven-union-roster-bb2025.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = ELVEN_UNION_TEAM_BB2025,
        ),
        "human-roster-bb2025.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = HUMAN_TEAM_BB2025,
        ),
        "khorne-roster-bb2025.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = KHORNE_TEAM_BB2025,
        ),
        "lizardmen-roster-bb2025.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = LIZARDMEN_TEAM_BB2025,
        ),
        "orc-roster-bb2025.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = ORC_TEAM_BB2025,
        ),
        "skaven-roster-bb2025.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = SKAVEN_TEAM_BB2025,
        ),
        "dwarf-roster-bb2025.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = DWARF_TEAM_BB2025,
        )
    )
}
