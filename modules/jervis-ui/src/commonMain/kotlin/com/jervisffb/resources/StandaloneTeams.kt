package com.jervisffb.resources

import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.BB2020Rules
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.BLOODBORN_MARAUDER_LINEMEN
import com.jervisffb.engine.rules.bb2020.roster.BLOODSEEKERS
import com.jervisffb.engine.rules.bb2020.roster.BLOODSPAWN
import com.jervisffb.engine.rules.bb2020.roster.GUTTER_RUNNER
import com.jervisffb.engine.rules.bb2020.roster.HUMAN_BLITZER
import com.jervisffb.engine.rules.bb2020.roster.HUMAN_CATCHER
import com.jervisffb.engine.rules.bb2020.roster.HUMAN_LINEMAN
import com.jervisffb.engine.rules.bb2020.roster.HUMAN_TEAM
import com.jervisffb.engine.rules.bb2020.roster.HUMAN_THROWER
import com.jervisffb.engine.rules.bb2020.roster.KHORNE_TEAM
import com.jervisffb.engine.rules.bb2020.roster.KHORNGORS
import com.jervisffb.engine.rules.bb2020.roster.LIZARDMEN_TEAM
import com.jervisffb.engine.rules.bb2020.roster.OGRE
import com.jervisffb.engine.rules.bb2020.roster.SAURUS_BLOCKERS
import com.jervisffb.engine.rules.bb2020.roster.SKAVEN_BLITZER
import com.jervisffb.engine.rules.bb2020.roster.SKAVEN_LINEMAN
import com.jervisffb.engine.rules.bb2020.roster.SKAVEN_TEAM
import com.jervisffb.engine.rules.bb2020.roster.SKAVEN_THROWER
import com.jervisffb.engine.rules.bb2020.roster.SKINK_RUNNER_LINEMEN
import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.GameHistory
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.engine.serialize.PlayerUiData
import com.jervisffb.engine.serialize.RosterSpriteData
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import com.jervisffb.engine.serialize.TeamSpriteData
import com.jervisffb.engine.teamBuilder

// List of default starter team rosters
// This is primarely usd by Standalone Mode
object StandaloneTeams {
    val defaultTeams = mapOf(
        "human-starter-team.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = HUMAN_TEAM
            team = teamBuilder(BB2020Rules(), HUMAN_TEAM) {
                name = "Human Starter Team #1"
                addPlayer(PlayerId("Hu1"), "Ogre-1", PlayerNo(1), OGRE)
                addPlayer(PlayerId("Hu2"), "Blitzer-2", PlayerNo(2), HUMAN_BLITZER)
                addPlayer(PlayerId("Hu3"), "Blitzer-3", PlayerNo(3), HUMAN_BLITZER)
                addPlayer(PlayerId("Hu4"), "Blitzer-4", PlayerNo(4), HUMAN_BLITZER)
                addPlayer(PlayerId("Hu5"), "Blitzer-5", PlayerNo(5), HUMAN_BLITZER)
                addPlayer(PlayerId("Hu6"), "Thrower-6", PlayerNo(6), HUMAN_THROWER)
                addPlayer(PlayerId("Hu7"), "Catcher-7", PlayerNo(7), HUMAN_CATCHER)
                addPlayer(PlayerId("Hu8"), "Catcher-8", PlayerNo(8), HUMAN_CATCHER)
                addPlayer(PlayerId("Hu9"), "Lineman-9", PlayerNo(9), HUMAN_LINEMAN)
                addPlayer(PlayerId("Hu10"), "Lineman-10", PlayerNo(10), HUMAN_LINEMAN)
                addPlayer(PlayerId("Hu11"), "Lineman-11", PlayerNo(11), HUMAN_LINEMAN)
                reRolls = 3
                apothecaries = 0
                dedicatedFans = 1
                teamValue = 1_000_000
            }
            history = null
            rosterUiData = StandaloneRosters.defaultRosters.values.first { it.roster.id == roster!!.id }.uiData
            val players = team!!.createDefaultUiData(rosterUiData!!)
            teamUiData = TeamSpriteData(
                teamLogo = null,
                players = players.toMap()
            )
        },

        "lizardmen-starter-team.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = LIZARDMEN_TEAM
            team = teamBuilder(BB2020Rules(), LIZARDMEN_TEAM) {
                name = "Lizardmen Starter Team #1"
                addPlayer(PlayerId("Li1"), "Skink-1", PlayerNo(1), SKINK_RUNNER_LINEMEN)
                addPlayer(PlayerId("Li2"), "Skink-2", PlayerNo(2), SKINK_RUNNER_LINEMEN)
                addPlayer(PlayerId("Li3"), "Skink-3", PlayerNo(3), SKINK_RUNNER_LINEMEN)
                addPlayer(PlayerId("Li4"), "Skink-4", PlayerNo(4), SKINK_RUNNER_LINEMEN)
                addPlayer(PlayerId("Li5"), "Skink-5", PlayerNo(5), SKINK_RUNNER_LINEMEN)
                addPlayer(PlayerId("Li6"), "Saurus-6", PlayerNo(6), SAURUS_BLOCKERS)
                addPlayer(PlayerId("Li7"), "Saurus-7", PlayerNo(7), SAURUS_BLOCKERS)
                addPlayer(PlayerId("Li8"), "Saurus-8", PlayerNo(8), SAURUS_BLOCKERS)
                addPlayer(PlayerId("Li9"), "Saurus-9", PlayerNo(9), SAURUS_BLOCKERS)
                addPlayer(PlayerId("Li10"), "Saurus-10", PlayerNo(10), SAURUS_BLOCKERS)
                addPlayer(PlayerId("Li11"), "Saurus-11", PlayerNo(11), SAURUS_BLOCKERS)
                reRolls = 2
                apothecaries = 1
                dedicatedFans = 0
                teamValue = 1_000_000
            }
            history = null
            rosterUiData = StandaloneRosters.defaultRosters.values.first { it.roster.id == roster!!.id }.uiData
            val players = team!!.createDefaultUiData(rosterUiData!!)
            teamUiData = TeamSpriteData(
                teamLogo = null,
                players = players.toMap()
            )
        },

        "skaven-starter-team.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = SKAVEN_TEAM
            team = teamBuilder(BB2020Rules(), SKAVEN_TEAM) {
                name = "Skaven Starter Team #1"
                addPlayer(PlayerId("Sk1"), "Blitzer-1", PlayerNo(1), SKAVEN_BLITZER)
                addPlayer(PlayerId("Sk2"), "Blitzer-2", PlayerNo(2), SKAVEN_BLITZER)
                addPlayer(PlayerId("Sk3"), "GutterRunner-3", PlayerNo(3), GUTTER_RUNNER)
                addPlayer(PlayerId("Sk4"), "GutterRunner-4", PlayerNo(4), GUTTER_RUNNER)
                addPlayer(PlayerId("Sk5"), "GutterRunner-5", PlayerNo(5), GUTTER_RUNNER)
                addPlayer(PlayerId("Sk6"), "Thrower-6", PlayerNo(6), SKAVEN_THROWER)
                addPlayer(PlayerId("Sk7"), "Lineman-7", PlayerNo(7), SKAVEN_LINEMAN)
                addPlayer(PlayerId("Sk8"), "Lineman-8", PlayerNo(8), SKAVEN_LINEMAN)
                addPlayer(PlayerId("Sk9"), "Lineman-9", PlayerNo(9), SKAVEN_LINEMAN)
                addPlayer(PlayerId("Sk10"), "Lineman-10", PlayerNo(10), SKAVEN_LINEMAN)
                addPlayer(PlayerId("Sk11"), "Lineman-11", PlayerNo(11), SKAVEN_LINEMAN)
                reRolls = 3
                apothecaries = 1
                dedicatedFans = 0
                teamValue = 970_000
            }
            history = null
            rosterUiData = StandaloneRosters.defaultRosters.values.first { it.roster.id == roster!!.id }.uiData
            val players = team!!.createDefaultUiData(rosterUiData!!)
            teamUiData = TeamSpriteData(
                teamLogo = null,
                players = players.toMap()
            )
        },

        "khorne-starter-team.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = KHORNE_TEAM
            team = teamBuilder(BB2020Rules(), KHORNE_TEAM) {
                name = "Khorne Starter Team #1"
                addPlayer(PlayerId("Kh1"), "Lineman-1", PlayerNo(1), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh2"), "Lineman-2", PlayerNo(2), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh3"), "Lineman-3", PlayerNo(3), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh4"), "Lineman-4", PlayerNo(4), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh5"), "Lineman-5", PlayerNo(5), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh6"), "Lineman-6", PlayerNo(6), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh7"), "Khorngor-7", PlayerNo(7), KHORNGORS)
                addPlayer(PlayerId("Kh8"), "Khorngor-8", PlayerNo(8), KHORNGORS)
                addPlayer(PlayerId("Kh9"), "Bloodseeker-9", PlayerNo(9), BLOODSEEKERS)
                addPlayer(PlayerId("Kh10"), "Bloodseeker-10", PlayerNo(10), BLOODSEEKERS)
                addPlayer(PlayerId("Kh11"), "Bloodspawn-11", PlayerNo(11), BLOODSPAWN)
                reRolls = 3
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 1_000_000
            }
            history = null
            rosterUiData = StandaloneRosters.defaultRosters.values.first { it.roster.id == roster!!.id }.uiData
            val players = team!!.createDefaultUiData(rosterUiData!!)
            teamUiData = TeamSpriteData(
                teamLogo = null,
                players = players.toMap()
            )
        },
    )

}

private fun buildTeamFile(function: JervisTeamFileBuilder.() -> Unit): JervisTeamFile {
    val builder = JervisTeamFileBuilder()
    function(builder)
    return builder.build()
}

class JervisTeamFileBuilder {
    var metadata: JervisMetaData? = null
    var rosterUiData: RosterSpriteData? = null
    var teamUiData: TeamSpriteData? = null
    var history: GameHistory? = null
    var team: Team? = null
    var roster: BB2020Roster? = null
    fun build(): JervisTeamFile {
        return JervisTeamFile(
            metadata!!,
            roster!!,
            team!!,
            history,
            rosterUiData!!,
            teamUiData!!
        )
    }
}

fun Team.createDefaultUiData(rosterData: RosterSpriteData): Map<PlayerId, PlayerUiData> {
    val usedSprites = mutableMapOf<PositionId, Int>()
    return this.associate {
        val playerSprite = when (val sprite = rosterData.positions[it.position.id]) {
            is SingleSprite -> sprite
            is SpriteSheet -> {
                val index = usedSprites.getOrPut(it.position.id) { 0 }
                usedSprites[it.position.id] = index + 1
                sprite.copy(
                    selectedIndex = index
                )
            }
            null -> TODO("Add fallback sprites")
        }
        Pair(
            it.id, PlayerUiData(
                playerSprite,
                rosterData.portraits[it.position.id]
            )
        )
    }
}
